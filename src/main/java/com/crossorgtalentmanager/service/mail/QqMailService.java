package com.crossorgtalentmanager.service.mail;

import jakarta.annotation.Resource;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * 基于 QQ 邮箱的简单邮件发送服务。
 *
 * 使用 application-local.yml 中配置的 QQ 邮箱账号和授权码发送邮件。
 */
@Service
@Slf4j
public class QqMailService {

    @Value("${mail.qq.username}")
    private String fromEmail;

    @Value("${mail.qq.auth-code}")
    private String authCode;

    /**
     * SMTP 服务器地址（QQ 邮箱）
     */
    private static final String SMTP_HOST = "smtp.qq.com";

    /**
     * 发送企业注册成功邮件，包含登录账号与默认密码。
     */
    public void sendCompanyRegistrationSuccessEmail(String toEmail, String companyName,
            String username, String password) throws MessagingException {
        String subject = "企业注册申请已通过 - 跨组织人才管理系统";
        String content = String.format("""
                您好！

                您在跨组织人才管理系统提交的企业注册申请已通过审核。

                企业名称：%s
                登录账号：%s
                默认密码：%s

                为保障账号安全，请您登录系统后尽快修改密码。

                （本邮件为系统自动发送，请勿直接回复）
                """, companyName, username, password);
        sendPlainTextMail(toEmail, subject, content);
    }

    /**
     * 发送企业注册失败邮件，包含拒绝原因。
     */
    public void sendCompanyRegistrationFailEmail(String toEmail, String companyName,
            String rejectReason) throws MessagingException {
        String subject = "企业注册申请未通过 - 跨组织人才管理系统";
        String content = String.format("""
                您好！

                很抱歉，您在跨组织人才管理系统提交的企业注册申请未通过审核。

                企业名称：%s
                拒绝原因：%s

                如有疑问，可联系系统管理员进一步沟通。

                （本邮件为系统自动发送，请勿直接回复）
                """, companyName, rejectReason);
        sendPlainTextMail(toEmail, subject, content);
    }

    /**
     * 发送纯文本邮件
     */
    public void sendPlainTextMail(String toEmail, String subject, String content) throws MessagingException {
        if (fromEmail == null || authCode == null) {
            log.warn("QQ 邮箱配置缺失，跳过发送邮件。fromEmail={}, authCodeNull={}",
                    fromEmail, authCode == null);
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject(subject, "UTF-8");
        message.setText(content, "UTF-8");

        Transport transport = session.getTransport("smtp");
        try {
            transport.connect(SMTP_HOST, fromEmail, authCode);
            transport.sendMessage(message, message.getAllRecipients());
        } finally {
            try {
                transport.close();
            } catch (Exception ignored) {
            }
        }
        log.info("发送邮件成功，to={}, subject={}", toEmail, subject);
    }
}



