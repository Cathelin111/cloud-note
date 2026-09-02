-- =============================================
-- 云笔记 H2 数据库初始化脚本 (H2 1.4.x, MODE=MySQL)
-- 首次启动时由 DbInitializer 自动执行(仅当表不存在时)
-- =============================================

DROP TABLE IF EXISTS cn_note_activity;
DROP TABLE IF EXISTS cn_activity_status;
DROP TABLE IF EXISTS cn_activity;
DROP TABLE IF EXISTS cn_share;
DROP TABLE IF EXISTS cn_note;
DROP TABLE IF EXISTS cn_notebook;
DROP TABLE IF EXISTS cn_notebook_type;
DROP TABLE IF EXISTS cn_note_status;
DROP TABLE IF EXISTS cn_note_type;
DROP TABLE IF EXISTS cn_user;

-- 用户表
-- cn_user_role:  user=普通用户  admin=系统管理员
-- cn_user_status: normal=正常   disabled=停用
CREATE TABLE cn_user (
  cn_user_id VARCHAR(100) NOT NULL,
  cn_user_name VARCHAR(100),
  cn_user_password VARCHAR(100),
  cn_user_token VARCHAR(100),
  cn_user_nick VARCHAR(100),
  cn_user_role VARCHAR(20) DEFAULT 'user',
  cn_user_status VARCHAR(20) DEFAULT 'normal',
  cn_user_create_time BIGINT,
  PRIMARY KEY (cn_user_id)
);

-- 活动表
CREATE TABLE cn_activity (
  cn_activity_id VARCHAR(100) NOT NULL,
  cn_activity_title VARCHAR(500),
  cn_activity_body CLOB,
  cn_activity_end_time BIGINT,
  PRIMARY KEY (cn_activity_id)
);

-- 活动状态表
CREATE TABLE cn_activity_status (
  cn_activity_status_id VARCHAR(100) NOT NULL,
  cn_activity_id VARCHAR(100),
  cn_activity_status_code VARCHAR(500),
  cn_activity_status_name VARCHAR(500),
  PRIMARY KEY (cn_activity_status_id)
);

-- 笔记本类型字典
CREATE TABLE cn_notebook_type (
  cn_notebook_type_id VARCHAR(100) NOT NULL,
  cn_notebook_type_code VARCHAR(100),
  cn_notebook_type_name VARCHAR(500),
  cn_notebook_type_desc CLOB,
  PRIMARY KEY (cn_notebook_type_id)
);

-- 笔记本表
CREATE TABLE cn_notebook (
  cn_notebook_id VARCHAR(100) NOT NULL,
  cn_user_id VARCHAR(100),
  cn_notebook_type_id VARCHAR(100),
  cn_notebook_name VARCHAR(500),
  cn_notebook_desc CLOB,
  cn_notebook_createtime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (cn_notebook_id)
);

-- 笔记状态字典
CREATE TABLE cn_note_status (
  cn_note_status_id VARCHAR(100) NOT NULL,
  cn_note_status_code VARCHAR(100),
  cn_note_status_name VARCHAR(500),
  PRIMARY KEY (cn_note_status_id)
);

-- 笔记类型字典
CREATE TABLE cn_note_type (
  cn_note_type_id VARCHAR(100) NOT NULL,
  cn_note_type_code VARCHAR(100),
  cn_note_type_name VARCHAR(500),
  cn_note_type_desc CLOB,
  PRIMARY KEY (cn_note_type_id)
);

-- 笔记表
CREATE TABLE cn_note (
  cn_note_id VARCHAR(100) NOT NULL,
  cn_notebook_id VARCHAR(100),
  cn_user_id VARCHAR(100),
  cn_note_status_id VARCHAR(100),
  cn_note_type_id VARCHAR(100),
  cn_note_title VARCHAR(500),
  cn_note_body CLOB,
  cn_note_create_time BIGINT,
  cn_note_last_modify_time BIGINT,
  PRIMARY KEY (cn_note_id)
);

-- 分享表
-- cn_share_status: normal=公开可见  disabled=已被管理员下架
CREATE TABLE cn_share (
  cn_share_id VARCHAR(100) NOT NULL,
  cn_share_title VARCHAR(500),
  cn_share_body CLOB,
  cn_note_id VARCHAR(100),
  cn_share_status VARCHAR(20) DEFAULT 'normal',
  PRIMARY KEY (cn_share_id)
);

-- 活动投稿表
CREATE TABLE cn_note_activity (
  cn_note_activity_id VARCHAR(100) NOT NULL,
  cn_activity_id VARCHAR(100),
  cn_note_id VARCHAR(100),
  cn_note_activity_up INT DEFAULT 0,
  cn_note_activity_down INT DEFAULT 0,
  cn_note_activity_title VARCHAR(500),
  cn_note_activity_body CLOB,
  PRIMARY KEY (cn_note_activity_id)
);

-- ============ 种子数据 ============

INSERT INTO cn_activity (cn_activity_id, cn_activity_title, cn_activity_body, cn_activity_end_time) VALUES
 ('1', 'Java', 'Java技术征文', NULL),
 ('2', '.net', '.net技术征文', NULL),
 ('3', 'C++', 'C++技术征文', NULL),
 ('4', 'IOS', 'IOS技术征文', NULL),
 ('5', 'Andriod', 'Android技术征文', NULL),
 ('6', '网络营销', '网络营销技术征文', NULL),
 ('7', '嵌入式', '嵌入式技术征文', NULL),
 ('8', 'PHP', 'PHP技术征文', NULL),
 ('9', 'UID', 'UID技术征文', NULL),
 ('10', '测试', '测试技术征文', NULL),
 ('11', '大数据', '大数据技术征文', NULL);

INSERT INTO cn_activity_status (cn_activity_status_id, cn_activity_id, cn_activity_status_code, cn_activity_status_name) VALUES
 ('1', '1', 'normal', 'normal');

INSERT INTO cn_notebook_type (cn_notebook_type_id, cn_notebook_type_code, cn_notebook_type_name, cn_notebook_type_desc) VALUES
 ('1', 'favorites', 'favorites', '收藏'),
 ('2', 'recycle', 'recycle', '回收站'),
 ('3', 'action', 'action', '活动'),
 ('4', 'push', 'push', '推送'),
 ('5', 'normal', 'normal', '正常');

INSERT INTO cn_note_status (cn_note_status_id, cn_note_status_code, cn_note_status_name) VALUES
 ('1', 'normal', 'normal');

INSERT INTO cn_note_type (cn_note_type_id, cn_note_type_code, cn_note_type_name, cn_note_type_desc) VALUES
 ('1', 'normal', 'normal', NULL);

-- 系统管理员 admin / admin123 (密码为 NoteUtil.md5("admin123"))
INSERT INTO cn_user (cn_user_id, cn_user_name, cn_user_password, cn_user_token, cn_user_nick, cn_user_role, cn_user_status, cn_user_create_time) VALUES
 ('a1d8c7f62b5d4e1f9c0e8a7b6c5d4e3f', 'admin', 'AZICOnu9cyUFFvBp3xi1AA==', NULL, '系统管理员', 'admin', 'normal', 1525688810279);

-- 演示用户 demo / 123456 (密码为 NoteUtil.md5("123456")), 普通用户角色
INSERT INTO cn_user (cn_user_id, cn_user_name, cn_user_password, cn_user_token, cn_user_nick, cn_user_role, cn_user_status, cn_user_create_time) VALUES
 ('48595f52-b22c-4485-9244-f4004255b972', 'demo', '4QrcOUm6Wau+VuBX8g+IPg==', NULL, '演示用户', 'user', 'normal', 1525688810279);

INSERT INTO cn_notebook (cn_notebook_id, cn_user_id, cn_notebook_type_id, cn_notebook_name, cn_notebook_desc, cn_notebook_createtime) VALUES
 ('623c1074d04641f78a04afc4ed64e684', '48595f52-b22c-4485-9244-f4004255b972', '5', '测试新建的笔记本', NULL, '2018-05-07 18:26:09'),
 ('b9844bfbe5704048bbd9be8354a2a00d', '48595f52-b22c-4485-9244-f4004255b972', '5', '测试笔记本2', NULL, '2018-05-07 18:28:43');

INSERT INTO cn_note (cn_note_id, cn_notebook_id, cn_user_id, cn_note_status_id, cn_note_type_id, cn_note_title, cn_note_body, cn_note_create_time, cn_note_last_modify_time) VALUES
 ('0e086e15000e4d3385afef193c18bb89', '623c1074d04641f78a04afc4ed64e684', '48595f52-b22c-4485-9244-f4004255b972', '1', '1', '测试建立的笔记', '', 1525688810279, 1525688810279),
 ('3c9f2d1a7e004d1e9a2b5c8d7e6f5a4b', 'b9844bfbe5704048bbd9be8354a2a00d', '48595f52-b22c-4485-9244-f4004255b972', '1', '1', 'Spring MVC 快速入门', '<p>Spring MVC 基于 MVC 设计模式, 通过 DispatcherServlet 统一分发请求, 是 Java Web 开发的经典框架。</p>', 1525690000000, 1525690000000),
 ('8a4b7c2d5e6f4a1b9c3d0e2f5a6b7c8d', 'b9844bfbe5704048bbd9be8354a2a00d', '48595f52-b22c-4485-9244-f4004255b972', '1', '1', 'MyBatis 动态 SQL 笔记', '<p>MyBatis 提供 if、foreach、where 等标签, 让我们可以在 XML 中灵活编写动态查询语句。</p>', 1525691000000, 1525691000000);

INSERT INTO cn_share (cn_share_id, cn_share_title, cn_share_body, cn_note_id) VALUES
 ('b09fa6064e3a4cf3a144d1279f8717aa', '测试建立的笔记', '', '0e086e15000e4d3385afef193c18bb89'),
 ('d1e2f3a4b5c64d7e8f9a0b1c2d3e4f5a', 'Spring MVC 快速入门', '<p>Spring MVC 基于 MVC 设计模式, 通过 DispatcherServlet 统一分发请求, 是 Java Web 开发的经典框架。</p>', '3c9f2d1a7e004d1e9a2b5c8d7e6f5a4b'),
 ('e5d4c3b2a1f04e9d8c7b6a5f4e3d2c1b', 'MyBatis 动态 SQL 笔记', '<p>MyBatis 提供 if、foreach、where 等标签, 让我们可以在 XML 中灵活编写动态查询语句。</p>', '8a4b7c2d5e6f4a1b9c3d0e2f5a6b7c8d');
