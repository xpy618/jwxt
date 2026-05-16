# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 构建与运行

JDK 和 Maven 不在 PATH 中，需使用完整路径：

```bash
# 设置环境
export JAVA_HOME="/d/Software/JDK17/jdk-17.0.19+10"
export PATH="$JAVA_HOME/bin:/d/Software/Maven/apache-maven-3.9.9/bin:$PATH"

# 编译
mvn clean compile

# 启动 (端口 8080)
mvn spring-boot:run

# MySQL (无密码)
/d/Software/MySQL/MySQL\ Server\ 8.0/bin/mysql -u root

# 停止应用（Windows 强制终止 Java 进程）
taskkill //F //IM java.exe
```

启动失败时依次检查：
1. MySQL 是否运行：`/d/Software/MySQL/MySQL\ Server\ 8.0/bin/mysql -u root -e "SELECT 1"`
2. 端口 8080 是否被占用：`netstat -ano | grep 8080`

数据库 `jwxt` 已创建，JPA `ddl-auto: update` 自动建表。启动后 DataInitializer 自动填充种子数据（仅当 user 表为空时）。

种子账号（密码均为 `123456`）：`admin`(管理员)、`zengzr`/`liutk`/`sunqt`/`zongliang`/`zengdt`/`tangyong`/`zhanyz`(教师)、`student`/`student2`(学生)。

清空数据库重新初始化：`mysql -u root -e "DROP DATABASE IF EXISTS jwxt; CREATE DATABASE jwxt;"`

## 架构概览

```
浏览器 → Thymeleaf 页面 (/login, /index, /courses...) → fetch → REST API (/api/**) → Service → JPA Repository → MySQL
```

- **页面路由**: `PageController`（`@Controller`）返回 Thymeleaf 模板，传入 `username`/`role` 到 Model
- **API 路由**: `@RestController` 类返回 JSON，统一封装为 `Result<T>`（`{code, message, data}`）
- **角色**: STUDENT / TEACHER / ADMIN（`Role` 枚举），Spring Security 方法级 `@PreAuthorize` + URL 级配置

## 认证流程

双通道 JWT 认证（`JwtAuthenticationFilter`）：

1. **登录**: 前端 fetch POST `/api/auth/login` → 获取 JWT → 同时存入 `localStorage`（`jwxt_token`）和 Cookie（`jwxt_token`）
2. **API 调用**: fetch 请求通过共享函数 `authHeader()`（定义在 `fragments.html`）从 `localStorage` 读取 token 注入 `Authorization: Bearer <token>` 头
3. **页面导航**: 浏览器自动发送 Cookie，Filter 从 Cookie 回退读取 JWT
4. **用户身份**: Controller 中通过 `auth.getCredentials()` 获取当前用户 ID（Long 类型）。`auth.getName()` 是 username（String），`auth.getAuthorities()` 返回 `ROLE_xxx` 格式的角色

密码加密使用 `BCryptPasswordEncoder`。

## 安全授权

```
/api/auth/**              → permitAll
GET /api/courses/**       → permitAll（浏览课程）
POST|PUT|DELETE /api/courses → TEACHER, ADMIN
/api/enrollments/**       → STUDENT
/api/grades/teacher/**    → TEACHER
/api/grades/submit|publish → TEACHER
/api/admin/**             → ADMIN
其他所有请求               → authenticated
```

`SecurityConfig` 做了 URL 级拦截，部分 Controller 方法上还有 `@PreAuthorize` 注解。

## 核心业务规则

- **选课上限**: `enrollmentRepository.countByCourseId()` >= `course.maxStudents` 时拒绝
- **时间冲突**: 同一学生选多门课时，遍历两门课的所有 CourseSlot，若任一 schedule 字符串相同则拒绝
- **成绩流程**: DRAFT → SUBMITTED（教师提交）→ PUBLISHED（教师发布），发布后不可修改
- **GPA**: 标准 4.0 算法，仅计算已发布成绩，`(∑ gpaPoint × credit) / ∑ credit`

## 课程多时段模型

`Course` 实体不再直接包含 `schedule`/`location` 字段，改为通过 `CourseSlot` 表支持一个课程多个上课时段：

```
Course (1) ──── (N) CourseSlot
```

- **CourseSlot 字段**: `id`, `courseId`, `schedule`, `location`
- **Repository**: `CourseSlotRepository` 提供 `findByCourseId`、`findByCourseIdIn`、`deleteByCourseId`
- **VO 层**: `CourseVO.schedule` 和 `CourseVO.location` 以中文分号（`；`）拼接多个时段的值，前端直接展示
- **课表 API**: `getSchedule()` 每个 slot 单独返回一条 `ScheduleVO`，课表网格中一门课可占据多个格子
- **冲突检测**: `checkScheduleConflict()` 批量加载已选课程的所有 slot 后在内存中比对

## schedule 字段格式

格式为 `周X HH:MM-HH:MM`，例如 `周一 10:20-12:00`、`周三 14:30-16:10`。

前端 `schedule.html` 按"周一至周五"列 × 四个时段行解析到课表网格：

| 时段 | 时间 |
|------|------|
| 第一大节 | 08:20-10:00 |
| 第二大节 | 10:20-12:00 |
| 第三大节 | 14:30-16:10 |
| 第四大节 | 16:30-18:10 |

`parseSchedule()` 函数提取 schedule 字符串的"周X"和开始时间进行网格定位。修改格式需同步改前端。

## 前端静态资源

- **HTMX**: 已本地化，存放在 `static/js/htmx.min.js`（47KB），不再依赖 unpkg CDN
- **共享 JS 函数**: `esc()`（HTML 转义）、`authHeader()`（构造认证头）、`showAlert()`（Toast 通知）定义在 `fragments.html` 的 `scripts` fragment 中，所有页面通过 `<div th:replace="fragments :: scripts">` 引入
- **CSS**: `static/css/style.css`，樱花粉 (#FFB7C5) 配色方案，CSS 变量统一管理
- **页面编辑按钮**: 使用 `data-course` 属性 + `JSON.stringify` 传值，避免内联 onclick 参数蔓延和 XSS 风险

## 当前状态

**已完成**: 全部后端（实体/Repository/Service/Controller/安全配置/数据初始化），pom.xml，application.yml，通用组件（Result/异常处理）

**前端已完成**: `style.css`（樱花主题）、`login.html`、`register.html`、`fragments.html`（导航栏 + 共享 JS）、`index.html`（首页仪表盘）、`courses.html`（课程浏览/管理）、`schedule.html`（学生课表）、`grades.html`（成绩查询/管理）、`admin/users.html`（用户管理）

**无测试**。

## 注意

- Thymeleaf 3.1+ 不再默认支持 `#request` 表达式对象，导航栏 active 状态通过 JS 根据 `window.location.pathname` 设置，不要使用 `th:classappend` 配合 `#request.requestURI`。
- `Course` 实体已移除 `schedule` 和 `location` 字段，所有时段操作通过 `CourseSlotRepository` 进行。

## 项目约定

- 设计文档位于 `DESIGN.md`（中文），包含 UI 配色方向和技术选型理由
- 前端使用 Thymeleaf + 原生 CSS + Vanilla JS（fetch），无 Node/npm 工具链
- 樱花粉 (#FFB7C5) 配色方案，CSS 变量定义在 `style.css` 中
- 所有 API 返回 `Result<T>` 格式，异常由 `GlobalExceptionHandler` 统一处理
