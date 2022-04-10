package com.rkyao.spring.boot.email.service;

/**
 * 邮件服务接口
 */
public interface MailService {

    /**
     * 发送普通纯文本邮件
     *
     * @param to 收件人地址
     * @param subject 邮件主题
     * @param text 邮件内容
     */
    void sendSimpleMail(String to, String subject, String text);

}