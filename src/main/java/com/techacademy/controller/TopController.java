
package com.techacademy.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.techacademy.entity.Employee;
import com.techacademy.service.EmailService;
import com.techacademy.service.EmployeeService;

@Controller
public class TopController {

    private final EmailService emailService;
    private final EmployeeService employeeService;

    public TopController(EmailService emailService, EmployeeService employeeService) {
        this.emailService = emailService;
        this.employeeService = employeeService;
    }

    // ログイン画面表示
    @GetMapping(value = "/login")
    public String login() {
        return "login/login";
    }

    // ログイン後のトップページ表示
    @GetMapping(value = "/")
    public String top() {
        return "redirect:/reports";
    }

    // パスワード再設定画面の表示
    @GetMapping(value = "/password_reset")
    public String showPasswordResetPage() {
        return "login/passwordreset";
    }

    // パスワード再設定URL送信後画面の表示
    @PostMapping(value = "/password_reset_submit")
    public String submitPasswordResetPage(String email) {
        System.out.println(email);
        String token = UUID.randomUUID().toString();
//        Employee employee = employeeService.findByEmail(email);
        emailService.sendResetToken(email, token);

        return "login/passwordreset_submit";
    }

    @GetMapping(value = "/signup")
    public String showSignUpPage() {
        return "login/signup";
    }

    @GetMapping(value = "/signup/add")
    public String showEmailSignUpPage() {
        return "login/email_signup";
    }

    @GetMapping(value = "/signup/add/completion")
    public String submitSignUpPage() {
        return "login/emailsignup_submit";
    }

    @GetMapping(value = "/password/reset")
    public String emailSubmitPasswordResetPage() {
        return "login/email_passwordreset.html";
    }

    @GetMapping(value = "/password/reset/completion")
    public String submitPasswordResetCompPage() {
        return "login/password_setting_complete";
    }


}
