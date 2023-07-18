package com.marcus.securebusiness.service.implementation;

import com.marcus.securebusiness.dto.UserDTO;
import com.marcus.securebusiness.enumeration.VerificationType;
import com.marcus.securebusiness.exception.ApiException;
import com.marcus.securebusiness.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.marcus.securebusiness.query.UserQuery.UPDATE_USER_IMAGE_QUERY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;

@Service
@AllArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    @Autowired
    private final JavaMailSender mailSender;
    @Override
    public void sendVerificationEmail(String firstName, String email, String verificationUrl, VerificationType verificationType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("securebusiness.team.dont.reply@gmail.com");
            message.setTo(email);
            message.setText(getEmailMessage(firstName, verificationUrl, verificationType));
            message.setSubject(String.format("SecureBusiness - %s Verification Email", StringUtils.capitalize(verificationType.getType())));
            mailSender.send(message);
            log.info("Email send to {}", firstName);
        } catch (Exception exception) {log.error(exception.getMessage());}
    }

    @Override
    public void sendEmailWithAttachment(String firstName, String email, String attachment, VerificationType verificationType) {
        try {
            //attachment = reformatAttachment(attachment);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            log.info(String.format("Email send to %s with email: %s with type: %s\nattachment: %s", firstName, email, verificationType, attachment));
            helper.setFrom("securebusiness.team.dont.reply@gmail.com");
            helper.setTo(email);
            helper.setText(getEmailMessage(firstName, "", verificationType));
            helper.setSubject("SecureBusiness - Notification Email");
            // Adding the file attachment
            FileSystemResource file = new FileSystemResource(new File(attachment));
            helper.addAttachment(file.getFilename(), file);
            log.info("File name {}, file body: {}", file.getFilename(), file);
            // Sending mail with attachment
            mailSender.send(message);
            log.info("Email send to {}", firstName);
        } catch (Exception exception) {log.error(exception.getMessage());}
    }

    private String getEmailMessage(String firstName, String verificationUrl, VerificationType verificationType) {
        switch (verificationType) {
            case PASSWORD -> {return "Hello "+firstName+"\n\nReset password request. Please click the link below to reset your password.\n\n"+verificationUrl+"\n\nThe Support Team";}
            case ACCOUNT -> {return "Hello "+firstName+"\n\nYour new account has been created. Please click the link below to activate your account.\n\n"+verificationUrl+"\n\nThe Support Team";}
            case ATTACHMENT -> {return "Hello "+firstName+"\n\nHere is your new invoice. Please check. Contact us if you have any question.\n\nThe Support Team";}
            default -> throw new ApiException("Unable to send email, Email type unknown.");
        }
    }

    private String reformatAttachment(String attachment) {
        String str = "";
        for (int i = 0; i<attachment.length(); i++) {
            if (attachment.substring(i,i+1).equals("C")) {
                for (int j = i; j<attachment.length(); j++) {
                    if (attachment.substring(j, j+1).equals("p") &&
                            attachment.substring(j+1, j+2).equals("d") &&
                            attachment.substring(j+2, j+3).equals("f"))
                        str = attachment.substring(i, j+3);
                }
                return str;
            }
        }
        log.info(str);
        return str;
    }
}
