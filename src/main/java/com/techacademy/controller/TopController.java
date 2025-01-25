
package com.techacademy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TopController {

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

    @GetMapping(value = "/password_reset")
    public String showPasswordResetPage() {
        return "login/passwordreset";
    }

    @GetMapping(value = "/password_reset_submit")
    public String submitPasswordResetPage() {
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

    @GetMapping(value = "/passwore/reset/completion")
    public String submitPasswordResetCompPage() {
        return "login/password_setting_complete";
    }


}
