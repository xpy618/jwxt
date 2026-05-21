package com.jwxt.service;

import com.jwxt.common.BusinessException;
import com.jwxt.dto.CourseRequest;
import com.jwxt.dto.CourseVO;
import com.jwxt.dto.EnrollmentPreviewVO;
import com.jwxt.dto.ScheduleVO;
import com.jwxt.entity.Course;
import com.jwxt.entity.CourseSlot;
import com.jwxt.entity.Enrollment;
import com.jwxt.entity.User;
import com.jwxt.repository.CourseRepository;
import com.jwxt.repository.CourseSlotRepository;
import com.jwxt.repository.EnrollmentRepository;
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
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository,
                         CourseSlotRepository slotRepository,
                         EnrollmentRepository enrollmentRepository,
                         UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.slotRepository = slotRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Course create(Long teacherId, CourseRequest request) {
        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setTeacherId(request.getTeacherId() != null ? request.getTeacherId() : teacherId);
        course.setMaxStudents(request.getMaxStudents());
        course.setSemester(request.getSemester());
        course.setCredit(request.getCredit());
        course = courseRepository.save(course);
        saveSlots(course.getId(), request.getSchedules(), request.getLocation(),
                   request.getStartWeek(), request.getEndWeek());
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
        course.setTeacherId(request.getTeacherId());
        course = courseRepository.save(course);
        slotRepository.deleteByCourseId(courseId);
        saveSlots(course.getId(), request.getSchedules(), request.getLocation(),
                   request.getStartWeek(), request.getEndWeek());
        return course;
    }

    private void saveSlots(Long courseId, List<String> schedules, String location,
                           Integer startWeek, Integer endWeek) {
        if (schedules != null && !schedules.isEmpty()) {
            for (String schedule : schedules) {
                if (schedule != null && !schedule.isBlank()) {
                    CourseSlot slot = new CourseSlot();
                    slot.setCourseId(courseId);
                    slot.setSchedule(schedule);
                    slot.setLocation(location != null && !location.isBlank() ? location : null);
                    slot.setStartWeek(startWeek != null ? startWeek : 1);
                    slot.setEndWeek(endWeek != null ? endWeek : 16);
                    slotRepository.save(slot);
                }
            }
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
            throw new BusinessException("人满了哦 😅");
        }
        if (enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId).isPresent()) {
            throw new BusinessException("已选过该课程");
        }
        float currentCredits = enrollmentRepository.sumCreditByStudentId(studentId);
        if (currentCredits + course.getCredit() > 30) {
            throw new BusinessException("选课总学分不能超过30，当前已选 " + currentCredits + " 学分");
        }
        checkScheduleConflict(studentId, course);
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollmentRepository.save(enrollment);
    }

    public EnrollmentPreviewVO previewEnrollments(Long studentId, List<Long> previewCourseIds) {
        List<Course> previewCourses = courseRepository.findAllById(previewCourseIds);
        Set<Long> foundIds = previewCourses.stream().map(Course::getId).collect(Collectors.toSet());
        List<String> warnings = new ArrayList<>();
        List<Long> conflictCourseIds = new ArrayList<>();
        Set<Long> alreadyEnrolled = new HashSet<>();

        List<Enrollment> myEnrollments = enrollmentRepository.findByStudentId(studentId);
        Set<Long> enrolledCourseIds = myEnrollments.stream().map(Enrollment::getCourseId).collect(Collectors.toSet());

        float selectedCredits = enrollmentRepository.sumCreditByStudentId(studentId);
        float previewCredits = 0;

        for (Long cid : previewCourseIds) {
            if (!foundIds.contains(cid)) {
                warnings.add("课程 #" + cid + " 不存在");
                conflictCourseIds.add(cid);
                continue;
            }
            if (enrolledCourseIds.contains(cid)) {
                warnings.add("课程已选过，不能重复加入预选篮");
                alreadyEnrolled.add(cid);
                conflictCourseIds.add(cid);
                continue;
            }
            long cnt = enrollmentRepository.countByCourseId(cid);
            Course c = previewCourses.stream().filter(x -> x.getId().equals(cid)).findFirst().orElse(null);
            if (c != null && cnt >= c.getMaxStudents()) {
                warnings.add(c.getName() + " 已满员（" + cnt + "/" + c.getMaxStudents() + "）");
                conflictCourseIds.add(cid);
            }
            if (c != null) previewCredits += c.getCredit();
        }

        if (selectedCredits + previewCredits > 30) {
            warnings.add("总学分将超过30上限（已选 " + selectedCredits + " + 预选 " + previewCredits + " = " + (selectedCredits + previewCredits) + "）");
        }

        // conflict detection: within preview basket + preview vs enrolled
        List<Long> effectivePreviewIds = previewCourseIds.stream()
                .filter(cid -> !alreadyEnrolled.contains(cid) && foundIds.contains(cid)).toList();
        List<CourseSlot> previewSlots = slotRepository.findByCourseIdIn(effectivePreviewIds);
        Map<Long, List<CourseSlot>> previewSlotsMap = previewSlots.stream()
                .collect(Collectors.groupingBy(CourseSlot::getCourseId));

        // within preview basket
        List<Long> previewIdList = new ArrayList<>(effectivePreviewIds);
        for (int i = 0; i < previewIdList.size(); i++) {
            for (int j = i + 1; j < previewIdList.size(); j++) {
                List<CourseSlot> slotsA = previewSlotsMap.get(previewIdList.get(i));
                List<CourseSlot> slotsB = previewSlotsMap.get(previewIdList.get(j));
                if (slotsA == null || slotsB == null) continue;
                if (hasConflict(slotsA, slotsB)) {
                    Long cidA = previewIdList.get(i);
                    Long cidB = previewIdList.get(j);
                    Course ca = previewCourses.stream().filter(x -> x.getId().equals(cidA)).findFirst().orElse(null);
                    Course cb = previewCourses.stream().filter(x -> x.getId().equals(cidB)).findFirst().orElse(null);
                    if (ca != null && cb != null) {
                        warnings.add(ca.getName() + " 与 " + cb.getName() + " 时间冲突");
                        if (!conflictCourseIds.contains(ca.getId())) conflictCourseIds.add(ca.getId());
                        if (!conflictCourseIds.contains(cb.getId())) conflictCourseIds.add(cb.getId());
                    }
                }
            }
        }

        // preview vs enrolled
        List<CourseSlot> enrolledSlots = slotRepository.findByCourseIdIn(new ArrayList<>(enrolledCourseIds));
        Map<Long, List<CourseSlot>> enrolledSlotsMap = enrolledSlots.stream()
                .collect(Collectors.groupingBy(CourseSlot::getCourseId));

        for (Long pcid : effectivePreviewIds) {
            List<CourseSlot> pSlots = previewSlotsMap.get(pcid);
            if (pSlots == null) continue;
            for (Long ecid : enrolledCourseIds) {
                List<CourseSlot> eSlots = enrolledSlotsMap.get(ecid);
                if (eSlots == null) continue;
                if (hasConflict(pSlots, eSlots)) {
                    Course pc = previewCourses.stream().filter(x -> x.getId().equals(pcid)).findFirst().orElse(null);
                    Course ec = courseRepository.findById(ecid).orElse(null);
                    if (pc != null && ec != null) {
                        warnings.add(pc.getName() + " 与已选课程 " + ec.getName() + " 时间冲突");
                        if (!conflictCourseIds.contains(pcid)) conflictCourseIds.add(pcid);
                    }
                }
            }
        }

        boolean valid = warnings.isEmpty();
        EnrollmentPreviewVO vo = new EnrollmentPreviewVO();
        vo.setTotalCredits((double) previewCredits);
        vo.setSelectedCredits((double) selectedCredits);
        vo.setAvailableCredits(Math.max(0, 30.0 - selectedCredits));
        vo.setValid(valid);
        vo.setWarnings(warnings.isEmpty() ? null : warnings);
        vo.setConflictCourseIds(conflictCourseIds.isEmpty() ? null : conflictCourseIds);
        return vo;
    }

    private boolean hasConflict(List<CourseSlot> slotsA, List<CourseSlot> slotsB) {
        for (CourseSlot a : slotsA) {
            for (CourseSlot b : slotsB) {
                if (schedulesOverlap(a, b)) return true;
            }
        }
        return false;
    }

    @Transactional
    public void unenroll(Long studentId, Long courseId) {
        enrollmentRepository.deleteByStudentIdAndCourseId(studentId, courseId);
    }

    private void checkScheduleConflict(Long studentId, Course newCourse) {
        List<CourseSlot> newSlots = slotRepository.findByCourseId(newCourse.getId());
        if (newSlots.isEmpty()) return;

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        if (enrollments.isEmpty()) return;

        List<Long> enrolledCourseIds = enrollments.stream().map(Enrollment::getCourseId).toList();
        List<CourseSlot> allEnrolledSlots = slotRepository.findByCourseIdIn(enrolledCourseIds);
        Map<Long, List<CourseSlot>> slotsByCourse = allEnrolledSlots.stream()
                .collect(Collectors.groupingBy(CourseSlot::getCourseId));

        for (Enrollment enr : enrollments) {
            List<CourseSlot> enrolledSlots = slotsByCourse.get(enr.getCourseId());
            if (enrolledSlots == null) continue;
            for (CourseSlot es : enrolledSlots) {
                for (CourseSlot ns : newSlots) {
                    if (schedulesOverlap(es, ns)) {
                        throw new BusinessException("选不了了哦 ⏰");
                    }
                }
            }
        }
    }

    private boolean schedulesOverlap(CourseSlot a, CourseSlot b) {
        if (!a.getSchedule().equals(b.getSchedule())) return false;
        int aStart = a.getStartWeek() != null ? a.getStartWeek() : 1;
        int aEnd = a.getEndWeek() != null ? a.getEndWeek() : 16;
        int bStart = b.getStartWeek() != null ? b.getStartWeek() : 1;
        int bEnd = b.getEndWeek() != null ? b.getEndWeek() : 16;
        return Math.max(aStart, bStart) <= Math.min(aEnd, bEnd);
    }

    public List<ScheduleVO> getSchedule(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        if (enrollments.isEmpty()) return List.of();

        List<Long> courseIds = enrollments.stream().map(Enrollment::getCourseId).toList();
        Map<Long, Course> courseMap = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        Set<Long> teacherIds = courseMap.values().stream().map(Course::getTeacherId).filter(Objects::nonNull).collect(Collectors.toSet());
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
                    result.add(buildScheduleVO(course, teacherName, slot));
                }
            }
        }
        return result;
    }

    private ScheduleVO buildScheduleVO(Course course, String teacherName, CourseSlot slot) {
        ScheduleVO vo = new ScheduleVO();
        vo.setCourseId(course.getId());
        vo.setCourseName(course.getName());
        vo.setTeacherName(teacherName);
        vo.setSemester(course.getSemester());
        vo.setCredit(course.getCredit());
        if (slot != null) {
            vo.setSchedule(slot.getSchedule());
            vo.setLocation(slot.getLocation());
            vo.setStartWeek(slot.getStartWeek() != null ? slot.getStartWeek() : 1);
            vo.setEndWeek(slot.getEndWeek() != null ? slot.getEndWeek() : 16);
        }
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
            if (c.getTeacherId() != null) teacherIds.add(c.getTeacherId());
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
                vo.setStartWeek(1);
                vo.setEndWeek(16);
            } else {
                StringBuilder sbSchedule = new StringBuilder();
                StringBuilder sbLocation = new StringBuilder();
                int minStart = Integer.MAX_VALUE;
                int maxEnd = Integer.MIN_VALUE;
                for (int i = 0; i < slots.size(); i++) {
                    if (i > 0) {
                        sbSchedule.append("；");
                        sbLocation.append("；");
                    }
                    sbSchedule.append(slots.get(i).getSchedule());
                    sbLocation.append(slots.get(i).getLocation() != null ? slots.get(i).getLocation() : "");
                    int sw = slots.get(i).getStartWeek() != null ? slots.get(i).getStartWeek() : 1;
                    int ew = slots.get(i).getEndWeek() != null ? slots.get(i).getEndWeek() : 16;
                    if (sw < minStart) minStart = sw;
                    if (ew > maxEnd) maxEnd = ew;
                }
                vo.setSchedule(sbSchedule.toString());
                vo.setLocation(sbLocation.toString());
                vo.setStartWeek(minStart);
                vo.setEndWeek(maxEnd);
            }
            vo.setEnrolled(enrolledCourseIds.contains(course.getId()));
            return vo;
        }).toList();
    }
}
