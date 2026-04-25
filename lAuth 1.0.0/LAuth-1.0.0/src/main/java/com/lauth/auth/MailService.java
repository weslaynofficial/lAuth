package com.lauth.auth;

import org.bukkit.plugin.Plugin;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class MailService {

    private final Plugin plugin;

    public MailService(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean sendVerificationCode(String toEmail, String playerName, String code) {
        String smtpUser = plugin.getConfig().getString("mail.smtp.user", "");
        String smtpPassword = plugin.getConfig().getString("mail.smtp.app-password", "");
        String fromName = plugin.getConfig().getString("mail.from-name", "lAuth");
        String subject = plugin.getConfig().getString("mail.subject", "E-posta Dogrulama Kodunuz");

        if (smtpUser.isBlank() || smtpPassword.isBlank()) {
            plugin.getLogger().warning("Mail gonderimi atlandi: SMTP bilgileri eksik.");
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", plugin.getConfig().getString("mail.smtp.host", "smtp.gmail.com"));
        props.put("mail.smtp.port", plugin.getConfig().getString("mail.smtp.port", "587"));
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpUser, fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(
                    "Merhaba " + playerName + ",\n\n" +
                    "Dogrulama kodunuz: " + code + "\n" +
                    "Kod suresi: " + plugin.getConfig().getInt("mail.code-expire-seconds", 300) + " saniye.\n\n" +
                    "Eger bu islemi siz yapmadiysaniz bu maili dikkate almayin."
            );

            Transport.send(message);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Mail gonderim hatasi: " + e.getMessage());
            return false;
        }
    }
}
