package com.jwxt.service;

import com.jwxt.common.BusinessException;
import com.jwxt.config.JwtUtils;
import com.jwxt.dto.LoginRequest;
import com.jwxt.dto.LoginResponse;
import com.jwxt.dto.RegisterRequest;
import com.jwxt.entity.Course;
import com.jwxt.entity.CourseCategory;
import com.jwxt.entity.Enrollment;
import com.jwxt.entity.Role;
import com.jwxt.entity.User;
import com.jwxt.repository.CourseRepository;
import com.jwxt.repository.CourseSlotRepository;
import com.jwxt.repository.EnrollmentRepository;
import com.jwxt.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final CourseSlotRepository courseSlotRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils,
                       EnrollmentRepository enrollmentRepository,
                       CourseRepository courseRepository, CourseSlotRepository courseSlotRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.courseSlotRepository = courseSlotRepository;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        if (!user.getEnabled()) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        return new LoginResponse(token, user.getRole().name(), user.getName(), user.getId());
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setEnabled(true);
        user = userRepository.save(user);

        if (user.getRole() == Role.STUDENT) {
            List<Course> requiredCourses = courseRepository.findByCategory(CourseCategory.REQUIRED);
            for (Course course : requiredCourses) {
                Enrollment enrollment = new Enrollment();
                enrollment.setStudentId(user.getId());
                enrollment.setCourseId(course.getId());
                enrollmentRepository.save(enrollment);
            }
        }
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void updateUserStatus(Long userId, Boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId, Long currentUserId) {
        if (userId.equals(currentUserId)) {
            throw new BusinessException("不能删除自己的账号");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("不能删除管理员账号");
        }

        if (user.getRole() == Role.TEACHER) {
            List<Long> courseIds = courseRepository.findByTeacherId(userId).stream()
                    .map(c -> c.getId()).toList();
            for (Long courseId : courseIds) {
                enrollmentRepository.findByCourseId(courseId).forEach(e -> enrollmentRepository.deleteById(e.getId()));
                courseSlotRepository.deleteByCourseId(courseId);
            }
            courseRepository.deleteAllById(courseIds);
        }

        enrollmentRepository.findByStudentId(userId).forEach(e -> enrollmentRepository.deleteById(e.getId()));
        userRepository.deleteById(userId);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
}
