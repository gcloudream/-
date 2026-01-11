package com.silemore.service;

import com.silemore.entity.EmergencyContact;
import com.silemore.entity.User;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JavaMailSender mailSender;
    private final String supportEmail;
    private final String baseUrl;
    private final String verifyPath;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.support-email}") String supportEmail,
                        @Value("${app.base-url}") String baseUrl,
                        @Value("${app.contact-verify-path}") String verifyPath) {
        this.mailSender = mailSender;
        this.supportEmail = supportEmail;
        this.baseUrl = baseUrl;
        this.verifyPath = verifyPath;
    }

    public void sendWelcomeEmail(User user) {
        String body = "尊敬的 " + user.getNickname() + "：\n\n"
                + "欢迎使用死了么。请每日完成签到，系统将在必要时通知您的紧急联系人。\n\n"
                + "本邮件由系统自动发送，请勿直接回复。\n";

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(supportEmail);
        mail.setTo(user.getEmail());
        mail.setSubject("【死了么】欢迎使用");
        mail.setText(body);
        mailSender.send(mail);
    }

    public void sendReminderEmail(User user) {
        String body = "尊敬的 " + user.getNickname() + "：\n\n"
                + "这是您的每日签到提醒，请记得完成签到。\n\n"
                + "如果您已签到，请忽略此邮件。\n\n"
                + "本邮件由系统自动发送，请勿直接回复。\n";

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(supportEmail);
        mail.setTo(user.getEmail());
        mail.setSubject("【死了么】每日签到提醒");
        mail.setText(body);
        mailSender.send(mail);
    }

    public void sendVerificationEmail(EmergencyContact contact, User user, String token, String message) {
        String verifyLink = buildVerifyLink(token, "verify");
        String rejectLink = buildVerifyLink(token, "reject");

        StringBuilder body = new StringBuilder();
        body.append(contact.getName()).append(" 您好！\n\n")
                .append(user.getNickname()).append(" 使用\"死了么\"安全监测应用，\n")
                .append("并希望将您设为紧急联系人。\n\n");

        if (message != null && !message.isBlank()) {
            body.append("来自用户的留言：").append(message).append("\n\n");
        }

        body.append("如果您同意成为紧急联系人，请点击以下链接确认：\n")
                .append(verifyLink).append("\n\n")
                .append("如您不愿意成为紧急联系人，请点击拒绝链接：\n")
                .append(rejectLink).append("\n\n")
                .append("链接有效期：7天。若您不认识发件人，请忽略此邮件。\n\n")
                .append("—— 死了么 安全保障系统\n")
                .append("支持邮箱：").append(supportEmail).append("\n");

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(supportEmail);
        mail.setTo(contact.getEmail());
        mail.setSubject("【死了么】" + user.getNickname() + " 邀请您成为紧急联系人");
        mail.setText(body.toString());
        mailSender.send(mail);
    }

    public void sendAlertEmail(EmergencyContact contact, User user, int missedDays,
                               java.time.LocalDateTime lastCheckIn) {
        String lastCheckInText = lastCheckIn == null
                ? "暂无"
                : DATE_TIME_FORMATTER.format(lastCheckIn);

        String body = "尊敬的 " + contact.getName() + "：\n\n"
                + "您好！\n\n"
                + "您被设为 " + user.getNickname() + "(" + user.getEmail() + ") 的紧急联系人。\n\n"
                + "我们检测到该用户已连续 " + missedDays + " 天未在\"死了么\"应用中签到。\n"
                + "最后一次签到时间：" + lastCheckInText + "\n\n"
                + "请您尽快尝试与其取得联系，确认其是否安全。\n\n"
                + "此邮件由系统自动发送，请勿直接回复。\n\n"
                + "—— 死了么 安全保障系统\n";

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(supportEmail);
        mail.setTo(contact.getEmail());
        mail.setSubject("【紧急】您的朋友 " + user.getNickname() + " 已连续 " + missedDays + " 天未签到");
        mail.setText(body);
        mailSender.send(mail);
    }

    public void sendRecoveryEmail(EmergencyContact contact, User user) {
        String body = "尊敬的 " + contact.getName() + "：\n\n"
                + "用户 " + user.getNickname() + " 已恢复签到。\n"
                + "您可认为其目前安全。\n\n"
                + "此邮件由系统自动发送，请勿直接回复。\n\n"
                + "—— 死了么 安全保障系统\n";

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(supportEmail);
        mail.setTo(contact.getEmail());
        mail.setSubject("【死了么】恢复通知：用户已签到");
        mail.setText(body);
        mailSender.send(mail);
    }

    public void sendRemovedEmail(EmergencyContact contact, User user) {
        String body = "尊敬的 " + contact.getName() + "：\n\n"
                + "您已被用户 " + user.getNickname() + " 移除为紧急联系人。\n\n"
                + "如有疑问，请与用户直接联系。\n\n"
                + "—— 死了么 安全保障系统\n";

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(supportEmail);
        mail.setTo(contact.getEmail());
        mail.setSubject("【死了么】紧急联系人移除通知");
        mail.setText(body);
        mailSender.send(mail);
    }

    private String buildVerifyLink(String token, String action) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = verifyPath.startsWith("/") ? verifyPath : "/" + verifyPath;
        return base + path + "?token=" + token + "&action=" + action;
    }
}
