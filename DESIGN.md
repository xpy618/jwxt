# 教务系统设计文档

## 项目定位

轻量化教务管理系统，目标并发用户数：5 人。适合小型培训班、课题组使用。

## 技术架构

| 层级 | 技术 | 说明 |
|------|------|------|
| 后端框架 | Spring Boot 3.x | 单体应用 |
| 安全认证 | Spring Security + JWT | 无状态认证，角色级别拦截 |
| ORM | Spring Data JPA (Hibernate) | 轻量 ORM，自动建表 |
| 数据库 | MySQL 8.0 | 本机 D:\Software\MySQL\MySQL Server 8.0 |
| 构建工具 | Maven | ✅ Apache Maven 3.9.9，D:\Software\Maven\ |
| JDK | 17+ | ✅ OpenJDK 17.0.19 Temurin，D:\Software\JDK17\ |
| API 文档 | SpringDoc (OpenAPI 3.0) | 自动生成 |
| 前端 | Thymeleaf + HTMX + 原生 CSS | 零构建工具，服务端渲染为主 |

### 为何不用前后端分离？

目标 5 人并发，不需要 SPA 的复杂性。Thymeleaf 模板引擎 + HTMX 做局部刷新，既能保持页面交互流畅，又避免了 Node/npm/Webpack 等前端工具链。CSS 使用原生写法（配合 frontend-design skill 做高质量设计）。

## 核心模块（三期规划）

### 一期：基础骨架 + 认证
- Spring Boot 项目初始化
- Spring Security + JWT 登录认证
- 三种角色：学生、教师、管理员
- 用户注册（管理员审批或邀请码制）
- 统一返回格式、全局异常处理

### 二期：课程 + 选课
- 课程 CRUD（教师/管理员操作）
- 学生选课 / 退课
- 选课人数上限控制
- 上课时间冲突检测
- 我的课表查询

### 三期：成绩管理
- 教师录入成绩
- 成绩审核与发布
- 学生查分
- GPA 计算
- 成绩单导出

## 数据库设计（核心表）

```
user            — 用户表（id, username, password, role, name, ...）
course          — 课程表（id, name, teacher_id, max_students, semester, ...）
enrollment      — 选课表（id, student_id, course_id, enrolled_at）
grade           — 成绩表（id, student_id, course_id, score, gpa_point, status）
```

## UI 设计方向

- 配色：樱花粉 (#FFB7C5) + 浅色系（白/米白/淡粉）
- 风格：圆角卡片、柔和阴影、可爱图标
- 动画：樱花飘落粒子效果（参考现有 pomodoro.html）
- 字体：系统默认中文字体，圆润亲和
- 使用 frontend-design skill 确保设计质量，避免通用 AI 美学

## 项目结构

```
jwxt/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/jwxt/
│   │   │   ├── JwxtApplication.java
│   │   │   ├── config/        — Security、CORS 配置
│   │   │   ├── controller/    — REST 接口
│   │   │   ├── service/       — 业务逻辑
│   │   │   ├── repository/    — JPA Repository
│   │   │   ├── entity/        — 实体类
│   │   │   ├── dto/           — 数据传输对象
│   │   │   └── common/        — 统一返回、异常处理、工具类
│   │   └── resources/
│   │       ├── application.yml
│   │       └── templates/     — Thymeleaf 模板
│   └── test/
└── DESIGN.md                  — 本文件
```

## 环境依赖清单

| 软件 | 当前状态 | 安装方式 |
|------|---------|---------|
| JDK 17+ | ✅ OpenJDK 17.0.19 Temurin | D:\Software\JDK17\jdk-17.0.19+10\ |
| Maven 3.9+ | ✅ Apache Maven 3.9.9 | D:\Software\Maven\apache-maven-3.9.9\ |
| MySQL 8.0 | ✅ D:\Software\MySQL\ | 已有，需确认服务已启动 |
| Node.js | ✅ v24.15.0 | 已安装 |

## 已有 Skills

| Skill | 用途 |
|-------|------|
| frontend-design | 高质量前端 UI 设计（樱花可爱风格） |
| init | 项目初始化后生成 CLAUDE.md |
| review | 代码审查 |
| security-review | 安全审查 |
| simplify | 代码质量检查 |
