package com.connorng.ReUzit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(
            String toMail,
            String subject,
            String body
    ){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("learningenglish151103@gmail.com");
        message.setTo(toMail);
        message.setText(body);
        message.setSubject(subject);

        mailSender.send(message);
    }
}
