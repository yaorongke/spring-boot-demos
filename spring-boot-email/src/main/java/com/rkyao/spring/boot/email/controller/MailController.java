package com.rkyao.spring.boot.email.controller;

import com.rkyao.spring.boot.email.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮件发送
 */
@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    /**
     * 发送邮件
     * localhost:8080/mail/send?to=294830731@qq.com&subject=这是邮件主题&text=这是邮件内容
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param text 邮件内容
     */
    @RequestMapping("/send")
    public void send(String to, String subject, String text) {
        mailService.sendSimpleMail(to, subject, text);
    }

}
