package com.lcz.cloud_note.web;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化器
 * 面向普通用户打包部署: 使用 H2 嵌入式文件数据库, 首次启动时若 cn_user 表不存在,
 * 自动执行 classpath:sql/cloud_note_h2.sql 完成建表并导入演示数据。
 */
@Component
public class DbInitializer implements ApplicationListener<ContextRefreshedEvent> {

	@Resource
	private DataSource dataSource;
	@Resource
	private DataBackup dataBackup;

	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			Connection conn = dataSource.getConnection();
			try {
				if (tableExists(conn, "CN_USER")) {
					//已有数据: 对旧版数据库做增量升级(游客/管理员功能新增的字段与种子数据)
					upgradeExistingDb(conn);
				} else {
					executeInitScript(conn);
				}
			} finally {
				conn.close();
			}
			//每日自动备份(当天已有备份则跳过; 非H2自动忽略)
			try {
				dataBackup.backup(true);
			} catch (Exception e) {
				System.err.println("[cloud_note] 每日备份失败(不影响启动): " + e.getMessage());
			}
		} catch (Exception e) {
			throw new RuntimeException("数据库初始化失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 旧库增量升级(主要面向H2嵌入库):
	 * 1. cn_user补充 角色/状态/注册时间 字段
	 * 2. cn_share补充 分享状态 字段
	 * 3. 存量数据补齐默认值, 并预置系统管理员账号 admin / admin123
	 * 说明: MySQL库请手工执行仓库根目录 cloud_note.sql 完成升级, 本方法跳过MySQL
	 */
	private void upgradeExistingDb(Connection conn) throws Exception {
		String product = conn.getMetaData().getDatabaseProductName();
		if (product == null || !product.toUpperCase().contains("H2")) {
			return; // 非H2(如MySQL)由开发人员手工升级脚本
		}
		Statement st = conn.createStatement();
		try {
			st.execute("ALTER TABLE cn_user ADD COLUMN IF NOT EXISTS cn_user_role VARCHAR(20) DEFAULT 'user'");
			st.execute("ALTER TABLE cn_user ADD COLUMN IF NOT EXISTS cn_user_status VARCHAR(20) DEFAULT 'normal'");
			st.execute("ALTER TABLE cn_user ADD COLUMN IF NOT EXISTS cn_user_create_time BIGINT");
			st.execute("ALTER TABLE cn_share ADD COLUMN IF NOT EXISTS cn_share_status VARCHAR(20) DEFAULT 'normal'");
			//存量数据补齐
			st.execute("UPDATE cn_user SET cn_user_role='user' WHERE cn_user_role IS NULL");
			st.execute("UPDATE cn_user SET cn_user_status='normal' WHERE cn_user_status IS NULL");
			st.execute("UPDATE cn_share SET cn_share_status='normal' WHERE cn_share_status IS NULL");
			//预置系统管理员(不存在时才插入), 密码为 admin123 的 MD5+Base64
			st.execute("INSERT INTO cn_user(cn_user_id,cn_user_name,cn_user_password,cn_user_token,cn_user_nick,cn_user_role,cn_user_status,cn_user_create_time) "
					+ "SELECT 'a1d8c7f62b5d4e1f9c0e8a7b6c5d4e3f','admin','AZICOnu9cyUFFvBp3xi1AA==',NULL,'系统管理员','admin','normal'," 
					+ System.currentTimeMillis() + " "
					+ "WHERE NOT EXISTS (SELECT 1 FROM cn_user WHERE cn_user_name='admin')");
			//演示用户补上普通用户角色
			st.execute("UPDATE cn_user SET cn_user_role='user', cn_user_status='normal' WHERE cn_user_name='demo' AND cn_user_role IS NULL");
		} finally {
			st.close();
		}
	}

	private boolean tableExists(Connection conn, String tableName) throws Exception {
		Statement st = conn.createStatement();
		try {
			ResultSet rs = st.executeQuery(
					"SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + tableName + "'");
			boolean exists = false;
			if (rs.next() && rs.getInt(1) > 0) {
				exists = true;
			}
			rs.close();
			return exists;
		} finally {
			st.close();
		}
	}

	private void executeInitScript(Connection conn) throws Exception {
		InputStream in = getClass().getClassLoader().getResourceAsStream("sql/cloud_note_h2.sql");
		if (in == null) {
			throw new IllegalStateException("找不到初始化脚本 sql/cloud_note_h2.sql");
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		StringBuilder sql = new StringBuilder();
		String line;
		Statement st = conn.createStatement();
		try {
			while ((line = reader.readLine()) != null) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("--")) {
					continue; // 跳过空行与注释
				}
				sql.append(line).append("\n");
				if (trimmed.endsWith(";")) {
					String statement = sql.toString().trim();
					sql.setLength(0);
					if (!statement.isEmpty()) {
						// 去掉结尾分号后执行
						st.execute(statement.substring(0, statement.length() - 1));
					}
				}
			}
			// 处理最后一条没有分号的语句(防御)
			if (sql.length() > 0) {
				String statement = sql.toString().trim();
				if (!statement.isEmpty()) {
					if (statement.endsWith(";")) {
						statement = statement.substring(0, statement.length() - 1);
					}
					st.execute(statement);
				}
			}
		} finally {
			st.close();
			reader.close();
		}
	}
}
