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

# 启动 (端口 8080)，-o 跳过 Maven 更新检查，-Dmaven.test.skip=true 彻底跳过 test 阶段
mvn spring-boot:run -o -Dmaven.test.skip=true

# MySQL (无密码)
/d/Software/MySQL/MySQL\ Server\ 8.0/bin/mysql -u root

# 停止应用（Windows 强制终止 Java 进程）
taskkill //F //IM java.exe
```

启动失败时依次检查：
1. MySQL 是否运行：`/d/Software/MySQL/MySQL\ Server\ 8.0/bin/mysql -u root -e "SELECT 1"`
2. 端口 8080 是否被占用：`netstat -ano | grep 8080`

数据库 `jwxt` 已创建，JPA `ddl-auto: update` 自动建表。启动后 DataInitializer 自动填充种子数据（仅当 user 表为空时）。

种子账号（密码均为 `123456`）：`admin`(管理员)、`zengzr`/`liutk`/`sunqt`/`zongliang`/`zengdt`/`tangyong`/`zhanyz`(教师)、`wangzhaohan`/`hechao`/`zangyuexiang`/`gaoheng`(学生)。

种子课程（DataInitializer 自动创建，含多时段和周次范围）：
| 课程 | 教师 | 学分 | 时段 | 周次 |
|------|------|------|------|------|
| 机器学习 | 曾增日 | 3.0 | 周一/周三 10:20-12:00、周二 08:20-10:00、周五 10:20-12:00 | 2-9/7-10/2-9/7-10 |
| 数据库原理与应用 | 曾德天 | 3.0 | 周二 10:20-12:00、周四 10:20-12:00 | 2-17/9-16 |
| 操作系统原理与Linux应用 | 曾德天 | 3.5 | 周三 08:20-10:00、周四 08:20-10:00 | 2-11/5-10 |
| 计算机网络 | 宗亮 | 3.0 | 周三 16:30-18:10、周四 14:30-16:10、周五 14:30-16:10 | 2-11/3-10/2-11 |
| 毛泽东思想和中国特色社会主义理论体系概论 | 孙其庭 | 3.0 | 周一 14:30-16:10 | 2-17 |
| 人工智能数学基础 | 宗亮 | 3.0 | 周一 16:30-18:10 | 2-17 |
| 大学体育(四)(羽毛球3-56) | 唐勇 | 1.0 | 周三 14:30-16:10 | 2-17 |

清空数据库重新初始化：`mysql -u root -e "DROP DATABASE IF EXISTS jwxt; CREATE DATABASE jwxt;"`

### application.yml 关键配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `server.port` | 8080 | 应用端口 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/jwxt?...&createDatabaseIfNotExist=true` | 自动建库 |
| `spring.jpa.hibernate.ddl-auto` | `update` | 自动建表/更新表结构 |
| `spring.jpa.show-sql` | `true` | 打印 SQL（开发调试） |
| `spring.thymeleaf.cache` | `false` | 关闭模板缓存，修改即时生效 |
| `jwt.secret` | `jwxt-jwt-secret-key-2026-...` | JWT 签名密钥 |
| `jwt.expiration` | `86400000` | Token 有效期 24 小时 |
| `springdoc.api-docs.path` | `/api-docs` | OpenAPI JSON 文档路径 |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Swagger UI 页面 |

## 局域网共享

校园网通常有 AP 隔离（客户端间无法互访），需要使用**手机热点**构建独立局域网：

### 1. 防火墙开放端口（需管理员权限）

```cmd
netsh advfirewall firewall add rule name="JWXT 8080" dir=in action=allow protocol=tcp localport=8080
```

验证：`netsh advfirewall firewall show rule name="JWXT 8080"`

### 2. 启动应用

```cmd
set JAVA_HOME=D:\Software\JDK17\jdk-17.0.19+10
set PATH=%JAVA_HOME%\bin;D:\Software\Maven\apache-maven-3.9.9\bin;%PATH%
mvn spring-boot:run -o -Dmaven.test.skip=true
```

### 3. 手机热点共享

1. 手机开启个人热点
2. 电脑连上热点 WiFi
3. 查看新 IP：`ipconfig | findstr "IPv4"`（通常为 `192.168.x.x`）
4. 手机（或连同一热点的设备）访问 `http://<新IP>:8080/login`

此方案绕过了校园网 AP 隔离，同一热点下的设备均可访问。

**已知热点 IP**: 192.168.80.108（此 IP 通常不变，防火墙规则已永久写入，无需每次检查）。

## 架构概览

```
浏览器 → Thymeleaf 页面 (/login, /index, /courses...) → fetch → REST API (/api/**) → Service → JPA Repository → MySQL
```

- **页面路由**: `PageController`（`@Controller`）返回 Thymeleaf 模板，传入 `username`/`role` 到 Model
- **页面清单**: `/login` → `login.html`, `/register` → `register.html`, `/index` → `index.html`, `/courses` → `courses.html`, `/schedule` → `schedule.html`, `/grades` → `grades.html`, `/admin/users` → `admin/users.html`, `/admin/dashboard` → `admin/dashboard.html`
- **API 路由**: `@RestController` 类返回 JSON，统一封装为 `Result<T>`（`{code, message, data}`）
- **安全配置**: `config/SecurityConfig.java` — CSRF 已禁用，会话策略 STATELESS（无状态 JWT）
- **角色**: STUDENT / TEACHER / ADMIN（`Role` 枚举），Spring Security 方法级 `@PreAuthorize` + URL 级配置
- **身份注入**: `config/JwtAuthenticationFilter.java` 从 Header（`Authorization: Bearer <token>`）或 Cookie（`jwxt_token`）提取 JWT，构建 `UsernamePasswordAuthenticationToken`（principal=username, credentials=userId, authorities=[ROLE_xxx]）

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
/api/grades/publish/**     → TEACHER
/api/grades/withdraw/**    → TEACHER
/api/admin/**             → ADMIN（含重置密码、删除用户等）
其他所有请求               → authenticated
```

`SecurityConfig` 做 URL 级拦截，`UserController` 类级别 `@PreAuthorize("hasRole('ADMIN')")` 覆盖全部 `/api/admin/**`。部分 Controller 方法上还有额外的 `@PreAuthorize` 注解。

## DTO/VO 层

| 类 | 方向 | 用途 |
|----|------|------|
| `LoginRequest` | 入参 | 登录表单（username, password） |
| `RegisterRequest` | 入参 | 注册表单 |
| `CourseRequest` | 入参 | 课程创建/编辑（含 schedules 列表, location 字符串, startWeek/endWeek, 可选 teacherId） |
| `GradeRequest` | 入参 | 成绩录入/编辑（studentId, courseId, score） |
| `LoginResponse` | 返回值 | 登录成功返回（token, username, role） |
| `CourseVO` | 返回值 | 课程视图（含 teacherName, enrolledCount, enrolled, category, 拼接后的 schedule/location, startWeek/endWeek） |
| `GradeVO` | 返回值 | 成绩视图（含 studentName, courseName） |
| `ScheduleVO` | 返回值 | 课表项（每个 CourseSlot 一条，含 courseName, teacherName, schedule, location, startWeek/endWeek） |
| `EnrollmentPreviewRequest` | 入参 | 预选篮预览请求（courseIds 列表） |
| `EnrollmentPreviewVO` | 返回值 | 预选篮预览结果（totalCredits, selectedCredits, valid, warnings, conflictCourseIds） |
| `StudentDashboardVO` | 返回值 | 学生首页仪表盘（selectedCredits, todaySchedules, latestGrades, tips） |
| `AdminDashboardVO` | 返回值 | 管理员看板（用户/课程/选课/成绩统计 + popularCourses + fullCourses） |
| `CourseSummaryVO` | 返回值 | 课程摘要（courseName, teacherName, enrolledCount, maxStudents） |
| `AcademicProgressVO` | 返回值 | 学业进度总览（totalRequiredCredits, totalCompletedCredits, totalProgressPercent, items） |
| `AcademicProgressItemVO` | 返回值 | 单类别进度（category, categoryName, requiredCredits, completedCredits, progressPercent） |

`Result<T>` 统一包装：`{code: 200, message: "ok", data: T}`。异常由 `GlobalExceptionHandler` 处理，`BusinessException` 用于业务校验失败。

## 核心业务规则

### 选课
- **选课上限**: `enrollmentRepository.countByCourseId()` >= `course.maxStudents` 时拒绝
- **学分上限**: 已选课程总学分 + 新课程学分 <= 30，超出拒绝（`EnrollmentRepository.sumCreditByStudentId()`）
- **时间冲突**: 同一学生选多门课时，遍历两门课的所有 CourseSlot，`schedulesOverlap()` 比对 schedule 字符串是否相同且周次有交集，同时满足则拒绝

### 成绩
- **流程**: 教师保存 → DRAFT（草稿），教师发布 → PUBLISHED（已发布，学生可见），教师撤回 → DRAFT。DRAFT ⇄ PUBLISHED 双向可逆，已发布成绩不可直接修改（需先撤回）
- **GPA**: 标准 4.0 算法，仅计算已发布成绩，`(∑ gpaPoint × credit) / ∑ credit`
- **成绩 roster**: `getCourseGrades()` 返回该课程全部选课学生（Enrollment 列表）。status 为 null 的学生显示"未录入"，status=DRAFT/PUBLISHED 显示对应状态

### 管理员特权
- **强制删课**: 无视选课人数限制，同时清空关联的选课记录、成绩和时段（`CourseService.forceDelete()`）
- **重置密码**: `PUT /api/admin/users/{id}/reset-password`，默认重置为 `123456`
- **删除用户**: `DELETE /api/admin/users/{id}`，级联清理该用户的选课、成绩；若为教师则同时删除其所有课程及关联数据
- **自我保护**: 不能删除自己，不能删除 ADMIN 角色用户

## 课程多时段模型

`Course` 实体不再直接包含 `schedule`/`location` 字段，改为通过 `CourseSlot` 表支持一个课程多个上课时段：

```
Course (1) ──── (N) CourseSlot
```

- **CourseSlot 字段**: `id`, `courseId`, `schedule`, `location`, `startWeek`, `endWeek`
- **周次范围**: `startWeek`/`endWeek` 为 Integer，默认 1/16（全学期），支持同一课程不同时段有不同的周次范围
- **Repository**: `CourseSlotRepository` 提供 `findByCourseId`、`findByCourseIdIn`、`deleteByCourseId`
- **VO 层**: `CourseVO.schedule` 和 `CourseVO.location` 以中文分号（`；`）拼接多个时段的值，前端直接展示
- **课表 API**: `getSchedule()` 每个 slot 单独返回一条 `ScheduleVO`，课表网格中一门课可占据多个格子
- **冲突检测**: `checkScheduleConflict()` 批量加载已选课程的所有 slot 后在内存中比对

## schedule 字段格式

格式为 `周X HH:MM-HH:MM`，例如 `周一 10:20-12:00`、`周三 14:30-16:10`。

每个 `CourseSlot` 附带 `startWeek`/`endWeek` 字段（1-16），课表页面按周过滤：仅显示 `slot.startWeek <= 选中周 <= slot.endWeek` 的时段。选课冲突检测同时考虑 schedule 字符串匹配和周次交集。

前端 `schedule.html` 按"周一至周五"列 × 四个时段行解析到课表网格：

| 时段 | 时间 |
|------|------|
| 第一大节 | 08:20-10:00 |
| 第二大节 | 10:20-12:00 |
| 第三大节 | 14:30-16:10 |
| 第四大节 | 16:30-18:10 |

`parseSchedule()` 函数提取 schedule 字符串的"周X"和开始时间进行网格定位。修改格式需同步改前端。

**课程表单**使用复选框网格选择时段（`courses.html` 中的 `.schedule-picker`），教师勾选 5 天×4 大节的组合，同时通过下拉框选择周次范围（1-16 周），前端收集为 `schedules` 数组 + `startWeek`/`endWeek` 提交，后端遍历创建多条 `CourseSlot` 记录。编辑时已有 schedule 字符串按 `；` 分割回填勾选状态，周次范围从已有 slot 读取。

## 前端静态资源

- **HTMX**: 已本地化，存放在 `static/js/htmx.min.js`（47KB），不再依赖 unpkg CDN
- **共享 JS 函数**: `esc()`（HTML 转义）、`authHeader()`（构造认证头）、`showAlert()`（Toast 通知）定义在 `fragments.html` 的 `scripts` fragment 中，所有页面通过 `<div th:replace="fragments :: scripts">` 引入
- **CSS**: `static/css/style.css`，樱花粉 (#FFB7C5) 配色方案，CSS 变量统一管理
- **页面编辑按钮**: 使用 `data-course` 属性 + `JSON.stringify` 传值，避免内联 onclick 参数蔓延和 XSS 风险

**当前版本 v5.2** — 新增轻量培养方案与学业进度：Course 实体增加 CourseCategory（REQUIRED/ELECTIVE/PE），课程表单支持类别选择，学生首页展示总进度条和必修/选修/体育三条分类进度条（仅统计已发布且>=60分的成绩）。种子课程除羽毛球(体育)外均为必修。学分要求：必修12 + 选修6 + 体育1 = 总计19学分（Service 常量）。

参考 E-R 图：`reference-er/`（OpenTextBC 大学注册模型，5 实体经典设计）。

## 注意

- **Thymeleaf JS 内联**: 任何 `<script>` 中使用 `/*[[${...}]]*/` 或 `[[${...}]]` 表达式时，**必须**加 `th:inline="javascript"`。否则 Thymeleaf 仅替换 `[[...]]` 内部值而不移除 `/* */` 注释包裹，导致 JS 引擎将其当作注释跳过，变量始终取默认值。
- Thymeleaf 3.1+ 不再默认支持 `#request` 表达式对象，导航栏 active 状态通过 JS 根据 `window.location.pathname` 设置，不要使用 `th:classappend` 配合 `#request.requestURI`。
- `Course` 实体已移除 `schedule` 和 `location` 字段，所有时段操作通过 `CourseSlotRepository` 进行。

## 项目约定

- 设计文档位于 `DESIGN.md`（中文），包含 UI 配色方向和技术选型理由
- 前端使用 Thymeleaf + 原生 CSS + Vanilla JS（fetch），无 Node/npm 工具链
- 樱花粉 (#FFB7C5) 配色方案，CSS 变量定义在 `style.css` 中
- 所有 API 返回 `Result<T>` 格式，异常由 `GlobalExceptionHandler` 统一处理
