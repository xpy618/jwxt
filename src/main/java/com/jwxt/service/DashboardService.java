package com.jwxt.service;

import com.jwxt.dto.*;
import com.jwxt.entity.*;
import com.jwxt.repository.CourseRepository;
import com.jwxt.repository.CourseSlotRepository;
import com.jwxt.repository.EnrollmentRepository;
import com.jwxt.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final CourseSlotRepository slotRepository;
    private final UserRepository userRepository;

    private static final Map<DayOfWeek, String> DAY_TO_CHINESE = Map.of(
            DayOfWeek.MONDAY, "周一",
            DayOfWeek.TUESDAY, "周二",
            DayOfWeek.WEDNESDAY, "周三",
            DayOfWeek.THURSDAY, "周四",
            DayOfWeek.FRIDAY, "周五",
            DayOfWeek.SATURDAY, "周六",
            DayOfWeek.SUNDAY, "周日"
    );

    public DashboardService(EnrollmentRepository enrollmentRepository,
                           CourseRepository courseRepository,
                           CourseSlotRepository slotRepository,
                           UserRepository userRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    public StudentDashboardVO getStudentDashboard(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<Long> courseIds = enrollments.stream().map(Enrollment::getCourseId).toList();

        StudentDashboardVO vo = new StudentDashboardVO();
        vo.setCreditLimit(30.0);

        if (courseIds.isEmpty()) {
            vo.setSelectedCredits(0.0);
            vo.setSelectedCourseCount(0);
            vo.setTodaySchedules(List.of());
            vo.setLatestGrades(List.of());
            vo.setTips(List.of("你还没有选课，去课程浏览看看吧"));
            return vo;
        }

        Map<Long, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        // credits & count
        float totalCredits = 0;
        for (Enrollment e : enrollments) {
            Course c = courseMap.get(e.getCourseId());
            if (c != null) totalCredits += c.getCredit();
        }
        vo.setSelectedCredits((double) totalCredits);
        vo.setSelectedCourseCount(enrollments.size());

        // today's schedule
        String todayPrefix = DAY_TO_CHINESE.getOrDefault(LocalDate.now().getDayOfWeek(), "");
        int currentWeek = getCurrentWeek();
        vo.setTodaySchedules(buildTodaySchedules(courseIds, courseMap, todayPrefix, currentWeek));

        // latest grades (published, limit 3)
        vo.setLatestGrades(buildLatestGrades(enrollments, courseMap));

        // tips
        vo.setTips(buildTips(totalCredits, vo.getTodaySchedules(), enrollments, courseMap));

        return vo;
    }

    private List<ScheduleVO> buildTodaySchedules(List<Long> courseIds, Map<Long, Course> courseMap,
                                                  String todayPrefix, int currentWeek) {
        if (todayPrefix.isEmpty()) return List.of();

        List<CourseSlot> allSlots = slotRepository.findByCourseIdIn(courseIds);
        Set<Long> teacherIds = courseMap.values().stream()
                .map(Course::getTeacherId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> teacherNameMap = userRepository.findAllById(teacherIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        List<ScheduleVO> result = new ArrayList<>();
        for (CourseSlot slot : allSlots) {
            if (!slot.getSchedule().startsWith(todayPrefix)) continue;
            int sw = slot.getStartWeek() != null ? slot.getStartWeek() : 1;
            int ew = slot.getEndWeek() != null ? slot.getEndWeek() : 16;
            if (currentWeek < sw || currentWeek > ew) continue;

            Course course = courseMap.get(slot.getCourseId());
            if (course == null) continue;

            ScheduleVO vo = new ScheduleVO();
            vo.setCourseId(course.getId());
            vo.setCourseName(course.getName());
            vo.setTeacherName(teacherNameMap.getOrDefault(course.getTeacherId(), "未知"));
            vo.setSchedule(slot.getSchedule());
            vo.setLocation(slot.getLocation());
            vo.setCredit(course.getCredit());
            vo.setStartWeek(sw);
            vo.setEndWeek(ew);
            result.add(vo);
        }
        return result;
    }

    private List<GradeVO> buildLatestGrades(List<Enrollment> enrollments, Map<Long, Course> courseMap) {
        return enrollments.stream()
                .filter(e -> e.getStatus() == GradeStatus.PUBLISHED)
                .sorted((a, b) -> {
                    if (a.getPublishedAt() == null && b.getPublishedAt() == null) return 0;
                    if (a.getPublishedAt() == null) return 1;
                    if (b.getPublishedAt() == null) return -1;
                    return b.getPublishedAt().compareTo(a.getPublishedAt());
                })
                .limit(3)
                .map(e -> {
                    GradeVO vo = new GradeVO();
                    vo.setStudentId(e.getStudentId());
                    vo.setCourseId(e.getCourseId());
                    vo.setScore(e.getScore());
                    vo.setGpaPoint(e.getGpaPoint());
                    vo.setStatus(e.getStatus().name());
                    Course c = courseMap.get(e.getCourseId());
                    if (c != null) {
                        vo.setCourseName(c.getName());
                        vo.setCredit(c.getCredit());
                    }
                    return vo;
                })
                .toList();
    }

    private List<String> buildTips(float totalCredits, List<ScheduleVO> todaySchedules,
                                   List<Enrollment> enrollments, Map<Long, Course> courseMap) {
        List<String> tips = new ArrayList<>();

        if (totalCredits < 30) {
            tips.add("你当前已选 " + totalCredits + " 学分，还可选择 " + (30 - totalCredits) + " 学分");
        } else {
            tips.add("你已达到 30 学分上限");
        }

        if (todaySchedules.isEmpty()) {
            tips.add("今天没有课程安排，享受轻松的一天吧");
        } else {
            tips.add("今天共有 " + todaySchedules.size() + " 节课");
        }

        long draftCount = enrollments.stream().filter(e -> e.getStatus() == GradeStatus.DRAFT).count();
        if (draftCount > 0) {
            tips.add("你有 " + draftCount + " 门课程成绩已录入但未发布，请耐心等待");
        }

        long noGradeCount = enrollments.stream().filter(e -> e.getStatus() == null).count();
        if (noGradeCount > 0) {
            tips.add("你有 " + noGradeCount + " 门课程尚未录入成绩");
        }

        return tips;
    }

    public AdminDashboardVO getAdminDashboard() {
        AdminDashboardVO vo = new AdminDashboardVO();

        // 用户统计
        vo.setStudentCount(userRepository.countByRole(Role.STUDENT));
        vo.setTeacherCount(userRepository.countByRole(Role.TEACHER));
        vo.setAdminCount(userRepository.countByRole(Role.ADMIN));

        // 课程统计
        List<Course> allCourses = courseRepository.findAll();
        vo.setCourseCount((long) allCourses.size());
        vo.setUnassignedCourseCount(allCourses.stream().filter(c -> c.getTeacherId() == null).count());

        // 选课统计
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        vo.setEnrollmentCount((long) allEnrollments.size());

        // 成绩统计
        long publishedCount = 0, draftCount = 0, missingCount = 0;
        for (Enrollment e : allEnrollments) {
            if (e.getStatus() == GradeStatus.PUBLISHED) publishedCount++;
            else if (e.getStatus() == GradeStatus.DRAFT) draftCount++;
            else missingCount++;
        }
        vo.setPublishedGradeCount(publishedCount);
        vo.setDraftGradeCount(draftCount);
        vo.setMissingGradeCount(missingCount);

        // 选课人数统计 (按 courseId 分组)
        Map<Long, Long> enrollmentCountMap = allEnrollments.stream()
                .collect(Collectors.groupingBy(Enrollment::getCourseId, Collectors.counting()));
        Set<Long> teacherIds = allCourses.stream()
                .map(Course::getTeacherId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> teacherNameMap = userRepository.findAllById(teacherIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        Map<Long, Course> courseMap = allCourses.stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        List<CourseSummaryVO> summaries = new ArrayList<>();
        for (Course c : allCourses) {
            CourseSummaryVO s = new CourseSummaryVO();
            s.setCourseId(c.getId());
            s.setCourseName(c.getName());
            s.setTeacherName(teacherNameMap.getOrDefault(c.getTeacherId(), "未分配"));
            long enrolled = enrollmentCountMap.getOrDefault(c.getId(), 0L);
            s.setEnrolledCount((int) enrolled);
            s.setMaxStudents(c.getMaxStudents());
            summaries.add(s);
        }

        // 热门课程 TOP5
        vo.setPopularCourses(summaries.stream()
                .sorted((a, b) -> b.getEnrolledCount().compareTo(a.getEnrolledCount()))
                .limit(5)
                .toList());

        // 满员课程
        vo.setFullCourses(summaries.stream()
                .filter(s -> s.getEnrolledCount() >= s.getMaxStudents())
                .toList());

        return vo;
    }

    private int getCurrentWeek() {
        // Simple: use a fixed reference date or just return current calendar week
        // For a teaching system, week 1 starts from the semester start.
        // As a lightweight approach, calculate week number based on a configured semester start.
        // Fallback: return a mid-range value so most courses show up.
        // Actually, let's just use a reasonable default. The semester typically starts in Feb/Sep.
        // Better approach: just calculate based on weeks since semester start stored in DB or config.
        // For now, use LocalDate.now() with a rough semester start.
        LocalDate now = LocalDate.now();
        // Assume semester starts Feb 24, 2026 (spring) or Sep 1, 2025 (fall)
        // Use whichever is closer and in the past
        LocalDate springStart = LocalDate.of(2026, 2, 23);
        LocalDate fallStart = LocalDate.of(2025, 9, 1);

        LocalDate semesterStart;
        if (now.isAfter(springStart)) {
            semesterStart = springStart;
        } else {
            semesterStart = fallStart;
        }

        long daysBetween = now.toEpochDay() - semesterStart.toEpochDay();
        int week = (int) (daysBetween / 7) + 1;
        return Math.max(1, Math.min(week, 16));
    }
}
