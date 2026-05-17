package com.jwxt.service;

import com.jwxt.common.BusinessException;
import com.jwxt.dto.CourseRequest;
import com.jwxt.dto.CourseVO;
import com.jwxt.dto.ScheduleVO;
import com.jwxt.entity.Course;
import com.jwxt.entity.CourseSlot;
import com.jwxt.entity.Enrollment;
import com.jwxt.entity.User;
import com.jwxt.repository.CourseRepository;
import com.jwxt.repository.CourseSlotRepository;
import com.jwxt.repository.EnrollmentRepository;
import com.jwxt.repository.GradeRepository;
import com.jwxt.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseSlotRepository slotRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository,
                         CourseSlotRepository slotRepository,
                         EnrollmentRepository enrollmentRepository,
                         GradeRepository gradeRepository,
                         UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.slotRepository = slotRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.gradeRepository = gradeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Course create(Long teacherId, CourseRequest request) {
        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setTeacherId(teacherId);
        course.setMaxStudents(request.getMaxStudents());
        course.setSemester(request.getSemester());
        course.setCredit(request.getCredit());
        course = courseRepository.save(course);
        saveSlot(course.getId(), request.getSchedule(), request.getLocation());
        return course;
    }

    @Transactional
    public Course update(Long courseId, CourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("课程不存在"));
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setMaxStudents(request.getMaxStudents());
        course.setSemester(request.getSemester());
        course.setCredit(request.getCredit());
        course = courseRepository.save(course);
        slotRepository.deleteByCourseId(courseId);
        saveSlot(course.getId(), request.getSchedule(), request.getLocation());
        return course;
    }

    private void saveSlot(Long courseId, String schedule, String location) {
        if (schedule != null && !schedule.isBlank()) {
            CourseSlot slot = new CourseSlot();
            slot.setCourseId(courseId);
            slot.setSchedule(schedule);
            slot.setLocation(location);
            slotRepository.save(slot);
        }
    }

    @Transactional
    public void delete(Long courseId) {
        if (enrollmentRepository.existsByCourseId(courseId)) {
            throw new BusinessException("已有学生选课，无法删除");
        }
        slotRepository.deleteByCourseId(courseId);
        courseRepository.deleteById(courseId);
    }

    @Transactional
    public void forceDelete(Long courseId) {
        enrollmentRepository.findByCourseId(courseId).forEach(e -> enrollmentRepository.deleteById(e.getId()));
        gradeRepository.deleteByCourseId(courseId);
        slotRepository.deleteByCourseId(courseId);
        courseRepository.deleteById(courseId);
    }

    public List<Course> listAll() {
        return courseRepository.findAll();
    }

    public List<Course> listByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    public Course getById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在"));
    }

    public List<CourseSlot> getSlots(Long courseId) {
        return slotRepository.findByCourseId(courseId);
    }

    @Transactional
    public void enroll(Long studentId, Long courseId) {
        Course course = getById(courseId);
        long currentCount = enrollmentRepository.countByCourseId(courseId);
        if (currentCount >= course.getMaxStudents()) {
            throw new BusinessException("课程已满，无法选课");
        }
        if (enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId).isPresent()) {
            throw new BusinessException("已选过该课程");
        }
        checkScheduleConflict(studentId, course);
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void unenroll(Long studentId, Long courseId) {
        enrollmentRepository.deleteByStudentIdAndCourseId(studentId, courseId);
    }

    private void checkScheduleConflict(Long studentId, Course newCourse) {
        List<CourseSlot> newSlots = slotRepository.findByCourseId(newCourse.getId());
        if (newSlots.isEmpty()) return;

        Set<String> newSlotSchedules = newSlots.stream().map(CourseSlot::getSchedule).collect(Collectors.toSet());
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        if (enrollments.isEmpty()) return;

        List<Long> enrolledCourseIds = enrollments.stream().map(Enrollment::getCourseId).toList();
        List<CourseSlot> allEnrolledSlots = slotRepository.findByCourseIdIn(enrolledCourseIds);
        Map<Long, List<CourseSlot>> slotsByCourse = allEnrolledSlots.stream()
                .collect(Collectors.groupingBy(CourseSlot::getCourseId));

        Map<Long, Course> courseCache = courseRepository.findAllById(enrolledCourseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        for (Enrollment enr : enrollments) {
            List<CourseSlot> enrolledSlots = slotsByCourse.get(enr.getCourseId());
            if (enrolledSlots == null) continue;
            for (CourseSlot es : enrolledSlots) {
                if (newSlotSchedules.contains(es.getSchedule())) {
                    Course enrolledCourse = courseCache.get(enr.getCourseId());
                    String courseName = enrolledCourse != null ? enrolledCourse.getName() : "未知课程";
                    throw new BusinessException("上课时间冲突：已选课程「" + courseName + "」有时间重叠");
                }
            }
        }
    }

    public List<ScheduleVO> getSchedule(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        if (enrollments.isEmpty()) return List.of();

        List<Long> courseIds = enrollments.stream().map(Enrollment::getCourseId).toList();
        Map<Long, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        Set<Long> teacherIds = courseMap.values().stream().map(Course::getTeacherId).collect(Collectors.toSet());
        Map<Long, String> teacherNameMap = userRepository.findAllById(teacherIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        List<CourseSlot> allSlots = slotRepository.findByCourseIdIn(courseIds);
        Map<Long, List<CourseSlot>> slotsByCourse = allSlots.stream()
                .collect(Collectors.groupingBy(CourseSlot::getCourseId));

        List<ScheduleVO> result = new ArrayList<>();
        for (Enrollment enr : enrollments) {
            Course course = courseMap.get(enr.getCourseId());
            if (course == null) continue;
            String teacherName = teacherNameMap.getOrDefault(course.getTeacherId(), "未知");
            List<CourseSlot> courseSlots = slotsByCourse.getOrDefault(course.getId(), List.of());

            if (courseSlots.isEmpty()) {
                result.add(buildScheduleVO(course, teacherName, null));
            } else {
                for (CourseSlot slot : courseSlots) {
                    result.add(buildScheduleVO(course, teacherName, slot.getSchedule()));
                }
            }
        }
        return result;
    }

    private ScheduleVO buildScheduleVO(Course course, String teacherName, String schedule) {
        ScheduleVO vo = new ScheduleVO();
        vo.setCourseId(course.getId());
        vo.setCourseName(course.getName());
        vo.setTeacherName(teacherName);
        vo.setSemester(course.getSemester());
        vo.setCredit(course.getCredit());
        vo.setSchedule(schedule);
        return vo;
    }

    public List<CourseVO> listWithEnrollmentStatus(Long studentId) {
        List<Course> courses = courseRepository.findAll();
        List<Enrollment> myEnrollments = enrollmentRepository.findByStudentId(studentId);
        Set<Long> myCourseIds = myEnrollments.stream().map(Enrollment::getCourseId).collect(Collectors.toSet());
        return buildCourseVOs(courses, myCourseIds);
    }

    public List<CourseVO> listManageVO() {
        return buildCourseVOs(courseRepository.findAll(), Set.of());
    }

    public List<CourseVO> listByTeacherVO(Long teacherId) {
        return buildCourseVOs(courseRepository.findByTeacherId(teacherId), Set.of());
    }

    private List<CourseVO> buildCourseVOs(List<Course> courses, Set<Long> enrolledCourseIds) {
        if (courses.isEmpty()) return List.of();

        List<Long> courseIds = new ArrayList<>(courses.size());
        Set<Long> teacherIds = new HashSet<>();
        for (Course c : courses) {
            courseIds.add(c.getId());
            teacherIds.add(c.getTeacherId());
        }

        Map<Long, String> teacherNameMap = userRepository.findAllById(teacherIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        Map<Long, Long> countMap = new HashMap<>();
        List<Object[]> counts = enrollmentRepository.countGroupByCourseId(courseIds);
        for (Object[] row : counts) {
            countMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        List<CourseSlot> allSlots = slotRepository.findByCourseIdIn(courseIds);
        Map<Long, List<CourseSlot>> slotsByCourse = allSlots.stream()
                .collect(Collectors.groupingBy(CourseSlot::getCourseId));

        return courses.stream().map(course -> {
            List<CourseSlot> slots = slotsByCourse.getOrDefault(course.getId(), List.of());
            CourseVO vo = new CourseVO();
            vo.setId(course.getId());
            vo.setName(course.getName());
            vo.setDescription(course.getDescription());
            vo.setTeacherId(course.getTeacherId());
            vo.setTeacherName(teacherNameMap.getOrDefault(course.getTeacherId(), "未知"));
            vo.setMaxStudents(course.getMaxStudents());
            vo.setEnrolledCount(countMap.getOrDefault(course.getId(), 0L));
            vo.setSemester(course.getSemester());
            vo.setCredit(course.getCredit());
            if (slots.isEmpty()) {
                vo.setSchedule(null);
                vo.setLocation(null);
            } else {
                StringBuilder sbSchedule = new StringBuilder();
                StringBuilder sbLocation = new StringBuilder();
                for (int i = 0; i < slots.size(); i++) {
                    if (i > 0) {
                        sbSchedule.append("；");
                        sbLocation.append("；");
                    }
                    sbSchedule.append(slots.get(i).getSchedule());
                    sbLocation.append(slots.get(i).getLocation() != null ? slots.get(i).getLocation() : "");
                }
                vo.setSchedule(sbSchedule.toString());
                vo.setLocation(sbLocation.toString());
            }
            vo.setEnrolled(enrolledCourseIds.contains(course.getId()));
            return vo;
        }).toList();
    }
}
