package com.lcz.cloud_note.web;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.stereotype.Component;

/**
 * 数据备份组件(面向H2嵌入库)
 * 1. 每日自动备份: 应用启动时若当天还没有备份文件, 在线执行 H2 SCRIPT TO 生成完整SQL快照
 * 2. 手动备份: 后台"立即备份"按钮调用, 生成带时间戳的备份文件
 * 备份目录: <cloudnote.data>/backup, 自动清理只保留最新15份
 * 恢复方法: 停止服务 → 移走 cloud_note.mv.db → RunScript 执行备份.sql → 启动
 * 说明: MySQL环境请使用 mysqldump, 本组件对非H2直接跳过
 */
@Component("dataBackup")
public class DataBackup {
	//备份文件保留份数
	private static final int KEEP = 15;

	@Resource
	private DataSource dataSource;

	/**
	 * 执行一次备份
	 * @param daily true=每日备份(同一天不重复); false=手动备份(带时分秒, 总是生成)
	 * @return 备份文件绝对路径; 非H2或失败返回null(失败抛异常由调用方处理)
	 */
	public String backup(boolean daily) throws Exception {
		Connection conn = dataSource.getConnection();
		try {
			String product = conn.getMetaData().getDatabaseProductName();
			if (product == null || !product.toUpperCase().contains("H2")) {
				return null; //非H2(如MySQL)请使用mysqldump
			}
			String dataDir = System.getProperty("cloudnote.data");
			if (dataDir == null || dataDir.trim().isEmpty()) {
				dataDir = "CloudNoteData";
			}
			File backupDir = new File(dataDir, "backup");
			if (!backupDir.exists()) {
				backupDir.mkdirs();
			}
			SimpleDateFormat fmt = daily
					? new SimpleDateFormat("yyyyMMdd")
					: new SimpleDateFormat("yyyyMMdd_HHmmss");
			String name = daily
					? "cloud_note_" + fmt.format(new Date()) + ".sql"
					: "cloud_note_backup_" + fmt.format(new Date()) + ".sql";
			File target = new File(backupDir, name);
			if (daily && target.exists()) {
				return target.getAbsolutePath(); //今天已备份过
			}
			//路径中的反斜杠转为正斜杠, 避免SQL转义问题; 单引号翻倍防御
			String path = target.getAbsolutePath().replace('\\', '/').replace("'", "''");
			Statement st = conn.createStatement();
			try {
				st.execute("SCRIPT TO '" + path + "'");
			} finally {
				st.close();
			}
			cleanupOld(backupDir);
			return target.getAbsolutePath();
		} finally {
			conn.close();
		}
	}

	/** 只保留最新的KEEP份备份 */
	private void cleanupOld(File backupDir) {
		File[] files = backupDir.listFiles(new java.io.FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".sql");
			}
		});
		if (files == null || files.length <= KEEP) {
			return;
		}
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File a, File b) {
				return b.getName().compareTo(a.getName());
			}
		});
		for (int i = KEEP; i < files.length; i++) {
			files[i].delete();
		}
	}
}
