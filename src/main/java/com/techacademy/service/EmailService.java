package com.techacademy.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.techacademy.entity.Employee;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    private static final String LOCAL_HOST = "http://localhost:8080/";

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendResetToken(String email, String token) {

        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            messageHelper.setFrom("udonishito0506@gmail.com");
            messageHelper.setTo(email);
            messageHelper.setSubject("【日報管理システム】パスワードの再設定");
            messageHelper.setText("\n" +
                                  "アカウントのパスワードをリセットするには、次のリンクをクリックしてください。 \n" +
                                  "\n" +
                                  LOCAL_HOST + "password/reset?token=" + token + "\n" +
                                  "\n" +
                                  "パスワードのリセットを依頼していない場合は、このメールを無視してください。\n" +
                                  "\n" +
                                  "よろしくお願いいたします。 \n" +
                                  "日報管理システム 管理部より");
            javaMailSender.send(message);

        } catch(MessagingException e) {
            throw new RuntimeException("メッセージの設定に失敗しました。", e);
        }

    }

}
