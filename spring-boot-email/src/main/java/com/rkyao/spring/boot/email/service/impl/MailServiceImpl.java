package com.rkyao.spring.boot.email.service.impl;

import com.rkyao.spring.boot.email.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 邮件发送服务
 */
@Service
public class MailServiceImpl implements MailService {

    private Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderMail;

    @Override
    public void sendSimpleMail(String to, String subject, String text) {
        Assert.hasText(to, "收件人邮箱不能为空");
        Assert.hasText(subject, "邮件主题不能为空");
        Assert.hasText(text, "邮件内容不能为空");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderMail);   //邮件发送人
        message.setTo(to);  //邮件接收人
        message.setSubject(subject);   //邮件主题
        message.setText(text);   //邮件内容
        mailSender.send(message);
        logger.info("The mail has been sent, to: {}, subject: {}, text: {}", to, subject, text);
    }

}
