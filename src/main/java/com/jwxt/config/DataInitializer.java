package com.jwxt.config;

import com.jwxt.entity.*;
import com.jwxt.repository.CourseRepository;
import com.jwxt.repository.CourseSlotRepository;
import com.jwxt.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseSlotRepository slotRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CourseRepository courseRepository,
                           CourseSlotRepository slotRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.slotRepository = slotRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRole(Role.ADMIN);
        admin.setName("管理员");
        userRepository.save(admin);

        User t1 = createTeacher("zengzr", "曾增日");
        User t2 = createTeacher("liutk", "刘堂科");
        User t3 = createTeacher("sunqt", "孙其庭");
        User t4 = createTeacher("zongliang", "宗亮");
        User t5 = createTeacher("zengdt", "曾德天");
        User t6 = createTeacher("tangyong", "唐勇");
        User t7 = createTeacher("zhanyz", "詹煜卓");

        User s1 = createStudent("wangzhaohan", "王兆含");
        User s2 = createStudent("hechao", "何超");
        User s3 = createStudent("zangyuexiang", "臧跃翔");
        User s4 = createStudent("gaoheng", "高亨");

        createCourse("机器学习", "机器学习理论与实战，涵盖监督学习、无监督学习、深度学习基础",
                t1.getId(), 60, "2025-2026-2", 3.0f, CourseCategory.REQUIRED,
                new String[][]{{"周一 10:20-12:00", "致远-107", "2", "9"},
                        {"周二 08:20-10:00", "专业四机房", "7", "10"},
                        {"周三 10:20-12:00", "致远-107", "2", "9"},
                        {"周五 10:20-12:00", "专业六机房", "7", "10"}});

        createCourse("数据库原理与应用", "关系型数据库设计、SQL查询优化、事务管理与并发控制",
                t5.getId(), 60, "2025-2026-2", 3.0f, CourseCategory.REQUIRED,
                new String[][]{{"周二 10:20-12:00", "播馨-101", "2", "17"},
                        {"周四 10:20-12:00", "播馨-110", "9", "16"}});

        createCourse("操作系统原理与Linux应用", "操作系统核心概念、进程管理、内存管理及Linux系统实践",
                t5.getId(), 60, "2025-2026-2", 3.5f, CourseCategory.REQUIRED,
                new String[][]{{"周三 08:20-10:00", "播馨-102", "2", "11"},
                        {"周四 08:20-10:00", "专业四机房", "5", "10"}});

        createCourse("计算机网络", "TCP/IP协议栈、网络层与传输层协议、网络安全基础",
                t4.getId(), 60, "2025-2026-2", 3.0f, CourseCategory.REQUIRED,
                new String[][]{{"周三 16:30-18:10", "播馨-101", "2", "11"},
                        {"周四 14:30-16:10", "专业三机房", "3", "10"},
                        {"周五 14:30-16:10", "播馨-101", "2", "11"}});

        createCourse("毛泽东思想和中国特色社会主义理论体系概论", "思想政治理论必修课",
                t3.getId(), 80, "2025-2026-2", 3.0f, CourseCategory.REQUIRED,
                new String[][]{{"周一 14:30-16:10", "致远-201", "2", "17"}});

        createCourse("人工智能数学基础", "线性代数、概率论、最优化理论等在人工智能中的应用",
                t4.getId(), 60, "2025-2026-2", 3.0f, CourseCategory.REQUIRED,
                new String[][]{{"周一 16:30-18:10", "播馨-101", "2", "17"}});

        createCourse("大学体育(四)(羽毛球3-56)", "羽毛球基本技术与战术训练",
                t6.getId(), 30, "2025-2026-2", 1.0f, CourseCategory.PE,
                new String[][]{{"周三 14:30-16:10", "羽毛球主馆", "2", "17"}});
    }

    private User createTeacher(String username, String name) {
        User teacher = new User();
        teacher.setUsername(username);
        teacher.setPassword(passwordEncoder.encode("123456"));
        teacher.setRole(Role.TEACHER);
        teacher.setName(name);
        return userRepository.save(teacher);
    }

    private User createStudent(String username, String name) {
        User student = new User();
        student.setUsername(username);
        student.setPassword(passwordEncoder.encode("123456"));
        student.setRole(Role.STUDENT);
        student.setName(name);
        return userRepository.save(student);
    }

    private Course createCourse(String name, String description, Long teacherId,
                                int maxStudents, String semester, float credit,
                                CourseCategory category,
                                String[][] slots) {
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        course.setTeacherId(teacherId);
        course.setMaxStudents(maxStudents);
        course.setSemester(semester);
        course.setCredit(credit);
        course.setCategory(category);
        course = courseRepository.save(course);

        for (String[] slot : slots) {
            CourseSlot cs = new CourseSlot();
            cs.setCourseId(course.getId());
            cs.setSchedule(slot[0]);
            cs.setLocation(slot[1]);
            cs.setStartWeek(slot.length > 2 && slot[2] != null ? Integer.parseInt(slot[2]) : 1);
            cs.setEndWeek(slot.length > 3 && slot[3] != null ? Integer.parseInt(slot[3]) : 16);
            slotRepository.save(cs);
        }
        return course;
    }
}
