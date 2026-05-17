package com.jwxt.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/index")
    public String index(Authentication auth, Model model) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        model.addAttribute("username", auth.getName());
        model.addAttribute("role", role.replace("ROLE_", ""));
        return "index";
    }

    @GetMapping("/courses")
    public String courses(Authentication auth, Model model) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        model.addAttribute("username", auth.getName());
        model.addAttribute("role", role.replace("ROLE_", ""));
        return "courses";
    }

    @GetMapping("/schedule")
    public String schedule(Authentication auth, Model model) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        model.addAttribute("username", auth.getName());
        model.addAttribute("role", role.replace("ROLE_", ""));
        return "schedule";
    }

    @GetMapping("/grades")
    public String grades(Authentication auth, Model model) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        model.addAttribute("username", auth.getName());
        model.addAttribute("role", role.replace("ROLE_", ""));
        return "grades";
    }

    @GetMapping("/admin/users")
    public String adminUsers(Authentication auth, Model model) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        model.addAttribute("username", auth.getName());
        model.addAttribute("userId", auth.getCredentials());
        model.addAttribute("role", role.replace("ROLE_", ""));
        return "admin/users";
    }
}
