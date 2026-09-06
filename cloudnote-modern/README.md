# cloudnote-modern —— 云笔记现代化架构（Spring Boot 3 + Vue 3）

按《云笔记架构更新步骤》从旧版（Spring MVC 3.2 + jQuery + H2）迁移的**前后端分离**版本：

- `backend/`：Spring Boot 3.5 + MyBatis + MySQL 8/9 + JWT（端口 **8081**）
- `frontend-web/`：Vue 3 + Vite + Element Plus + Pinia + axios（开发端口 **5173**，`/api` 代理到 8081）
- 数据库沿用旧版 `cn_*` 表结构（MySQL 库名 `cloudnote`），保证数据/语义可直接迁移；
  旧系统（8080 端口部署）与新系统可**并存运行**，互不影响。

## 一、后端

```bash
# 1) 建库(一次性)
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS cloudnote DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
mysql -uroot -p cloudnote < <仓库根>/cloud_note.sql
#    (cloud_note.sql 含表结构与种子: admin/admin123、demo/123456)

# 2) 配置(如需修改连接/端口/备份)
#    backend/src/main/resources/application.yml
#     - spring.datasource.url/username/password
#     - server.port (默认 8081)
#     - backup.mysqldump (本机 mysqldump 路径, 用于"立即备份")

# 3) 构建并运行(需 JDK 17+ 与 Maven 3.8+)
cd backend
mvn clean package -DskipTests
java -jar target/cloudnote-backend-0.1.0-SNAPSHOT.jar
```

接口前缀：`/api/...`（认证类 `/api/auth`、公开 `/api/public`、登录 `/api/...`、管理员 `/api/admin`）。
登录返回 `token`，之后请求头带 `Authorization: Bearer <token>`。

## 二、前端

```bash
cd frontend-web
npm install            # 首次(网络受限时参考仓库根《部署指南》配置 npm 代理)
npm run dev            # 开发: http://localhost:5173  (/api 自动代理到 8081)
npm run build          # 生产构建, 产物在 dist/ 可交给 Nginx/任意静态服务器
```

页面与角色：

| 页面 | 路由 | 说明 |
|---|---|---|
| 分享广场 | `/shares` | 游客可浏览(全文分页搜索/详情)，登录后可收藏 |
| 社区活动 | `/activities`、`/activities/:id` | 游客可浏览投稿，登录后可投稿/顶踩/收藏 |
| 我的笔记 | `/notes` | 登录用户：笔记本 CRUD、笔记编辑/分享、收藏/回收站/活动面板 |
| 后台管理 | `/admin` | 仅管理员：用户/管理员/分享/活动管理、数据备份 |
| 登录/注册 | `/login`、`/register` | 管理员与用户同入口，按角色跳转 |

## 三、功能覆盖对照（与旧系统一致）

| 旧功能 | 新接口/页面 | 状态 |
|---|---|---|
| 注册/登录/改密/停用拦截 | `/api/auth/*` + 登录页 | ✅（旧 MD5 密码自动迁移 BCrypt） |
| 游客浏览分享与活动 | `/api/public/*` + 分享广场/活动页 | ✅ |
| 笔记本/笔记/回收站/收藏/活动投稿 | `/api/notebooks` `/api/notes` + 我的笔记 | ✅ |
| 分享我的笔记/收藏分享 | `/api/shares` + 工作台/广场 | ✅ |
| 活动投稿/顶踩/收藏投稿 | `/api/activities` + 活动详情 | ✅ |
| 管理员：用户/管理员/分享/活动/备份 | `/api/admin/*` + 后台页 | ✅ |
| 数据备份 | `/api/admin/system/backup`(mysqldump) | ✅ |

## 四、常见问题

- 端口：旧系统 8080、新后端 8081、前端 dev 5173；请勿同时起两个后端占用 8081。
- 依赖下载：仓库内 `.npmrc` 不入库；网络受限时按《部署指南》配代理后 `npm install`。
- 数据库：不要用 H2 脚本初始化 MySQL；MySQL 环境直接导入仓库根 `cloud_note.sql`。
- JWT 密钥：`application.yml` 的 `jwt.secret` 请在生产环境通过环境变量覆盖。
