//package com.example.Haroon.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//import org.springframework.core.io.ByteArrayResource;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//
//@Service
//public class EmailService {
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    @Value("${spring.mail.host}")
//    private String host;
//
//    @Value("${spring.mail.username}")
//    private String username;
//
//    @Value("${spring.mail.password}")
//    private String password;
//
//    public void sendEmailWithAttachment(String to, String body, String subject, byte[] attachment, String filename) throws MessagingException {
//        MimeMessage mimeMessage = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
//
//        helper.setSubject(subject);
//        helper.setFrom(username);
//        helper.setText(body);
//        helper.setTo(to);
//
//        helper.addAttachment(filename, new ByteArrayResource(attachment));
//
//        mailSender.send(mimeMessage);
//        System.out.println("Mail sent successfully with Excel attachment!!");
//    }
//
//}
