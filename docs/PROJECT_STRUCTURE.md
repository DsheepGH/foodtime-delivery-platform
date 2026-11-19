# FoodTime 项目结构与模块说明

## 顶层目录

| 路径 | 作用 |
| --- | --- |
| `pom.xml` | Maven 父工程，统一管理模块、依赖版本和构建配置 |
| `README.md` | 项目介绍、技术栈和启动说明 |
| `docker-compose.yml` | 本地 MySQL、Redis 开发环境 |
| `.gitignore` | 排除构建产物、IDE 文件、日志和本地运行时 |
| `foodtime-common/` | 跨模块复用的通用代码 |
| `foodtime-pojo/` | 数据模型层 |
| `foodtime-server/` | Web 服务、业务逻辑、数据访问和任务调度 |
| `frontend/admin/` | 运营管理端静态资源 |
| `deploy/` | Nginx 部署示例 |
| `sql/` | 数据库结构和演示数据 |
| `docs/` | 项目设计说明 |

## foodtime-common

该模块只包含与具体业务无关、被其他模块复用的代码。

| Java 包 | 作用 |
| --- | --- |
| `constant` | JWT、密码、状态、消息提示等常量 |
| `context` | 基于 ThreadLocal 的当前登录用户上下文 |
| `enumeration` | 自动填充操作类型等枚举 |
| `exception` | 统一业务异常体系 |
| `json` | Jackson 日期与序列化配置 |
| `properties` | JWT、阿里云 OSS、微信配置属性 |
| `result` | 统一响应 `Result` 和分页结果 `PageResult` |
| `utils` | JWT、HTTP、OSS、微信支付等工具类 |

## foodtime-pojo

该模块负责接口和业务层之间的数据结构。

| Java 包 | 作用 |
| --- | --- |
| `entity` | 与数据库表对应的实体，如员工、菜品、套餐、订单 |
| `dto` | 前端请求参数对象，如登录、新增菜品、订单查询 |
| `vo` | 返回给前端的展示对象，如营业额、订单统计、用户信息 |

## foodtime-server

该模块是应用启动模块，负责 Web 接口、业务编排、持久化、缓存和实时消息。

| Java 包 | 作用 |
| --- | --- |
| `com.foodtime` | `FoodTimeApplication` 启动类、事务、缓存和定时任务开关 |
| `annotation` | 自定义注解，如公共字段自动填充 |
| `aspect` | 自动填充 AOP 切面 |
| `config` | Redis、OSS、WebMvc、WebSocket 和库存初始化配置 |
| `controller.admin` | 运营管理端接口，统一挂载 `/admin` |
| `controller.user` | 用户端接口，统一挂载 `/user` |
| `controller.notify` | 微信支付回调接口 |
| `filter` | 请求上下文和登录信息处理 |
| `handler` | 全局异常处理和统一错误响应 |
| `interceptor` | 管理端、用户端 JWT 鉴权拦截器 |
| `mapper` | MyBatis Mapper 接口 |
| `service` | 业务接口 |
| `service.impl` | 业务实现、事务边界、缓存读写和库存处理 |
| `task` | 超时订单、WebSocket 等定时任务 |
| `utils` | 库存、Token 等模块内工具 |
| `websocket` | WebSocket 会话管理与消息推送 |

`src/main/resources` 中：

| 路径 | 作用 |
| --- | --- |
| `application.yml` | 主配置：端口、数据库、Redis、JWT、第三方服务 |
| `application-dev.yml` | 本地开发配置 |
| `mapper/` | MyBatis XML SQL 映射 |
| `lua/` | 库存扣减和归还脚本 |
| `template/` | 运营数据 Excel 报表模板 |

## frontend/admin

运营管理端构建产物，不需要 Node.js 即可由 Nginx 直接托管。

| 目录/文件 | 作用 |
| --- | --- |
| `index.html` | 管理端入口页面 |
| `css/` | 页面样式与第三方组件样式 |
| `js/` | 路由、页面逻辑和第三方依赖 |
| `fonts/` | Element UI 字体资源 |
| `img/` | FoodTime Logo、图标、空状态图等 |
| `media/` | 来单提醒和催单音频 |
| `manifest.json` | PWA 元信息 |
| `service-worker.js` | PWA 静态资源缓存 |
| `favicon.ico` | 浏览器页签图标 |

## deploy

| 文件 | 作用 |
| --- | --- |
| `nginx.conf` | 管理端静态资源、`/api/`、`/user/`、`/ws/` 反向代理示例 |
| `mime.types` | Nginx MIME 类型映射 |

## sql

| 文件 | 作用 |
| --- | --- |
| `foodtime_delivery.sql` | 创建 `foodtime_delivery` 数据库、表结构、管理员和演示商品数据 |

管理员演示账号为 `admin / 123456`，数据库保存的是密码的 MD5 值。

## 请求链路

```text
浏览器
  -> Nginx :80
  -> frontend/admin 静态资源
  -> /api/ 反向代理到 Spring Boot :8080/admin/
  -> Controller
  -> Service
  -> Mapper
  -> MySQL
```

Redis 用于缓存、营业状态和库存；WebSocket 用于订单提醒；Excel 模板用于经营报表导出。

## 本地运行文件

以下内容保留在本机以保证开发便利，但不会提交到 Git：

- `nginx-1.20.2/`：本地 Nginx 运行时。
- `.idea/`：IDEA 工程配置。
- `logs/`：本地运行日志。
- `**/target/`：Maven 构建产物。

部署到其他机器时，可使用 `deploy/nginx.conf` 安装并配置 Nginx。
