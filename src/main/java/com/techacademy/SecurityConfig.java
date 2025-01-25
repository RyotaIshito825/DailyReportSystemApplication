package com.techacademy;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    /** 認証・認可設定 */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.formLogin(login -> login.loginProcessingUrl("/login") // 従業員番号・パスワードの送信先
                .loginPage("/login") // ログイン画面
                .usernameParameter("email")  // フォームで送信される"email"をusernameとして処理
                .defaultSuccessUrl("/") // ログイン成功後のリダイレクト先
                .failureUrl("/login?error") // ログイン失敗時のリダイレクト先
                .permitAll() // ログイン画面は未ログインでアクセス可
        ).logout(logout -> logout.logoutSuccessUrl("/login") // ログアウト後のリダイレクト先
        ).authorizeHttpRequests(
                auth -> auth
                        .requestMatchers("/password_reset").permitAll() // ビュー用の /password_reset パスを許可
                        .requestMatchers("/password_reset_submit").permitAll() // ビュー用の /password_reset_submit パスを許可
                        .requestMatchers("/signup").permitAll() // ビュー用の /signup パスを許可
                        .requestMatchers("/signup/add").permitAll() // ビュー用の /signup/add パスを許可
                        .requestMatchers("/signup/add/completion").permitAll() //ビュー用の /signup/add/completion パスを許可
                        .requestMatchers("/password/reset").permitAll() // ビュー用の /password/reset パスを許可
                        .requestMatchers("/passwore/reset/completion").permitAll() // ビュー用の /password/reset/completion パスを許可
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // css等は未ログインでアクセス可
                        .requestMatchers("/img/**").permitAll()
                        .requestMatchers("/employees/**").hasAnyAuthority("ADMIN")
                        .anyRequest().authenticated()); // その他はログイン必要

        return http.build();
    }

    /** ハッシュ化したパスワードの比較に使用する */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}