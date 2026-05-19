# jwxt 优化方案

> 基于 OpenTextBC 大学注册数据模型的对照分析

## 实体映射对照

| OpenTextBC | jwxt | 关系 |
|------------|------|------|
| Student | User (role=STUDENT) | jwxt 统一 User 表 |
| Staff | User (role=TEACHER) | jwxt 统一 User 表 |
| Course | Course | 直接对应 |
| —— | CourseSlot | jwxt 多出，排课专用 |
| Enrollment | Enrollment | 直接对应 |
| Assignment | —— | jwxt 不需要 |
| —— | Grade | 与 Enrollment 1:1，可合并 |

## 设计取舍

**jwxt 更优的地方：**

- 统一 User 表 + role 区分，比 OpenTextBC 分 Student/Staff 两张表更精简，避免字段重复
- CourseSlot 排课实体是实用补充，OpenTextBC 缺少排课维度
- Grade 的 DRAFT/PUBLISHED 状态流转比 OpenTextBC 的单一 FinalGrade 更完整

**OpenTextBC 值得借鉴的地方：**

- FinalGrade 放在 Enrollment 表中，查询不 JOIN
- 180 学分总量限制防止学生超负荷选课
- Course 的 StaffNo 可空，课程可先创建后分配教师

## 优化项

### 1. 合并 Grade → Enrollment（推荐）

Enrollment 和 Grade 是 1:1 关系，两表的 studentId/courseId 完全重复。将 score、gpaPoint、status、publishedAt 移入 Enrollment，删除 Grade 相关文件。

```
Enrollment 新增字段:
  score        FLOAT
  gpa_point    FLOAT
  status       VARCHAR  — DRAFT / PUBLISHED
  published_at DATETIME
```

收益：删 1 实体 + 1 表 + 1 Repository + 1 Service，成绩查询少一次 JOIN。

### 2. 总学分选课上限

当前只检查单课 maxStudents，无学生角度的学分负荷限制。在选课时增加校验：

```
已选课程总学分 + 新课程学分 <= 30
```

收益：防止不理性选课，符合真实教务需求。

### 3. teacherId 可空

OpenTextBC 的 StaffNo 在 Course 中可为 null（0..1），课程可先创建后分配教师。jwxt 当前 teacherId NOT NULL 限制了管理灵活性。

收益：支持批量导入课程后逐个分配教师。

## 优先级

| # | 优化 | 影响范围 | 优先级 |
|---|------|----------|--------|
| 1 | 合并 Grade → Enrollment | 实体/表/Repository/Service/前端 | ⭐⭐⭐ |
| 2 | 总学分上限 | Service 一处校验 | ⭐⭐ |
| 3 | teacherId 可空 | 实体 + 前端 | ⭐ |
