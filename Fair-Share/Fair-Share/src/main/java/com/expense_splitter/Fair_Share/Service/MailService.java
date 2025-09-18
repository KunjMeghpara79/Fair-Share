package com.expense_splitter.Fair_Share.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    // ✅ Generate secure 6-digit OTP
    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // always 6 digits
        return String.valueOf(otp);
    }

    public String sendOtpEmail(String to) throws Exception {
        if (mailSender == null) {
            throw new Exception("MailSender is null");
        }

        // Generate OTP
        String otp = generateOTP();

        // Create email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("fairshare69@gmail.com");
        helper.setTo(to);
        helper.setSubject("FairShare - Your OTP Code");

        // Simple styled HTML content
        String htmlContent = """
        <html>
            <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.5;">
                <h2 style="color: #4CAF50;">OTP Verification</h2>
                <p>Hello,</p>
                <p>Your one-time password (OTP) is:</p>
                <p style="font-size: 24px; font-weight: bold; color: #d9534f;">
        """ + otp + """
                </p>
                <p>This OTP is valid for the next 5 minutes.</p>
                <br>
                <p style="color: gray; font-size: 12px;">If you did not request this email, please ignore it.</p>
            </body>
        </html>
        """;

        helper.setText(htmlContent, true); // true → send HTML email
        mailSender.send(message);

        return otp; // ✅ return OTP so you can save/verify it later
    }
}
