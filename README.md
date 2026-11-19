# FoodTime Delivery Platform

FoodTime Delivery Platform 是一套面向餐饮门店的外卖与订单管理平台，包含 Spring Boot 多模块后端和可直接部署的运营管理端。项目覆盖员工、菜品、套餐、订单、购物车、地址簿、经营统计与订单通知等完整业务链路。

## 功能概览

- 员工账号：登录鉴权、分页查询、新增、编辑、启用与禁用。
- 商品管理：分类、菜品、口味、套餐及图片上传。
- 门店运营：营业状态、订单接单、拒单、取消、派送与完成。
- 用户交易：微信登录、购物车、地址簿、下单、支付与订单跟踪。
- 实时通知：WebSocket 来单提醒与客户催单。
- 数据看板：营业额、订单、用户、热销商品统计与 Excel 报表导出。
- 可靠性设计：Redis 缓存一致性处理、Lua 原子扣减库存、下单防重复提交。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 2.7、Spring MVC、Spring Cache |
| 数据访问 | MyBatis、MySQL 8、Druid、PageHelper |
| 中间件 | Redis、WebSocket |
| 鉴权 | JWT、自定义管理端/用户端拦截器 |
| 文档与报表 | Knife4j / Swagger、Apache POI |
| 前端 | Vue 2 管理端静态构建产物 |

## 项目结构

```text
foodtime-delivery-platform/
├── foodtime-common/       # 通用结果、常量、异常、工具和配置属性
├── foodtime-pojo/         # Entity、DTO、VO
├── foodtime-server/       # Controller、Service、Mapper、任务与 WebSocket
├── frontend/admin/        # 运营管理端构建产物
├── deploy/nginx.conf      # 管理端静态资源与 API 反向代理示例
├── sql/                   # 数据库初始化脚本与演示数据
├── docs/                  # 项目说明文档
├── docker-compose.yml     # MySQL 与 Redis 本地开发环境
└── pom.xml                # Maven 父工程
```

更详细的设计说明见 [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md)。

## 快速启动

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8+
- Redis 6+
- Nginx 1.20+（用于运行管理端并代理 API）
- Docker（可选，用于快速启动 MySQL 与 Redis）

### 2. 启动基础设施

使用 Docker Compose：

```bash
docker compose up -d
```

也可以使用本机 MySQL 与 Redis，然后导入数据库：

```bash
mysql -u root -p < sql/foodtime_delivery.sql
```

默认数据库名为 `foodtime_delivery`，本地默认连接为：

```text
MySQL: localhost:3306 / root / 123456
Redis: localhost:6379 / 123456
```

### 3. 启动后端

```bash
mvn clean package -DskipTests
java -jar foodtime-server/target/foodtime-server-1.0-SNAPSHOT.jar
```

服务默认监听 `http://localhost:8080`，接口文档地址为 `http://localhost:8080/doc.html`。

### 4. 启动管理端

将 `frontend/admin` 配置为 Nginx 静态站点根目录，并参考 `deploy/nginx.conf` 配置 `/api/`、`/user/` 和 `/ws/` 反向代理。修改配置中的 `root` 路径后启动 Nginx，访问 `http://localhost`。

注意：`http://localhost` 是管理端页面，`http://localhost:8080` 只提供接口，不应直接访问 8080 的根路径。

演示管理员账号：

```text
用户名：admin
密码：123456
```

## 环境变量

本地启动前需设置以下环境变量；表中的值是 Docker Compose 示例环境使用的值：

| 变量 | 说明 | 示例值 |
| --- | --- | --- |
| `FOODTIME_DB_HOST` | MySQL 地址 | `localhost` |
| `FOODTIME_DB_PORT` | MySQL 端口 | `3306` |
| `FOODTIME_DB_NAME` | 数据库名 | `foodtime_delivery` |
| `FOODTIME_DB_USERNAME` | 数据库用户 | `root` |
| `FOODTIME_DB_PASSWORD` | 数据库密码 | `123456` |
| `FOODTIME_REDIS_HOST` | Redis 地址 | `localhost` |
| `FOODTIME_REDIS_PASSWORD` | Redis 密码 | `123456` |
| `FOODTIME_ALIOSS_*` | 阿里云 OSS 配置 | 按实际服务填写 |
| `FOODTIME_WECHAT_*` | 微信登录/支付配置 | 按实际服务填写 |
| `FOODTIME_BAIDU_AK` | 百度地图 AK | 按实际服务填写 |

仓库不包含真实密钥。部署前请替换 JWT 密钥，并按需配置 OSS、微信和地图服务。

## 常见访问问题

- 页面标题正常、但登录提示“密码错误”：确认数据库中的管理员密码是 `123456` 的 MD5 值；初始化 SQL 已包含正确值。
- 打开 `http://localhost:8080/` 显示 404 或 `No mapping for GET /`：这是正常的，8080 只承载 API。
- 管理端无法加载：确认 Nginx 正在运行，且 `/api/` 已代理到 `127.0.0.1:8080/admin/`。

## 核心模块

- `controller/admin`：运营端接口，统一挂载在 `/admin`。
- `controller/user`：用户侧接口，统一挂载在 `/user`。
- `service/impl`：业务编排、缓存与事务边界。
- `mapper`：MyBatis 数据访问接口，SQL 位于 `foodtime-server/src/main/resources/mapper`。
- `task`：超时订单处理与定时任务。
- `websocket`：订单提醒和催单消息推送。

## 说明

演示图片使用公开图片地址，仅用于界面展示。项目不提交任何真实数据库密码、云服务密钥、微信商户配置或本地 IDE 文件。
