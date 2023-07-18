package com.marcus.securebusiness.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        // Configure mailSender properties (e.g., host, port, credentials)
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("securebusiness.team.dont.reply@gmail.com");
        mailSender.setPassword("tmozqubmsatyonpt");
        mailSender.setProtocol("smtp");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        return mailSender;
    }
    /*spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: securebusiness.team.dont.reply@gmail.com
    password: tmozqubmsatyonpt
    protocol: smtp
    tls: true
    properties.mail.smtp:
      auth: true
      starttls.enable: true
      ssl.trust: smtp.gmail.com*/
}
