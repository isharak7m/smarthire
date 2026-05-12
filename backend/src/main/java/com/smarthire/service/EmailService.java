package com.smarthire.service;

import com.smarthire.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Email notification service — demonstrates JavaMailSender with HTML emails.
 * Uses @Async to avoid blocking the HTTP response thread.
 * Guarded by smarthire.email.enabled=true so it won't fail in dev/CI.
 */
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${smarthire.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${smarthire.email.from:noreply@smarthire.io}")
    private String fromAddress;

    /**
     * Sends a welcome email to the newly onboarded employee.
     * Called asynchronously so it doesn't slow down the POST /employees response.
     */
    @Async
    public void sendWelcomeEmail(Employee employee) {
        if (!emailEnabled || mailSender == null) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(employee.getEmail());
            helper.setSubject("Welcome to " +
                (employee.getDepartment() != null ? employee.getDepartment().getName() : "SmartHire") +
                " — Your Onboarding Begins!");

            String html = buildWelcomeHtml(employee);
            helper.setText(html, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            // Log but don't propagate — email failure shouldn't break the API
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    /**
     * Notifies HR when an employee completes all onboarding tasks.
     */
    @Async
    public void sendOnboardingCompleteEmail(Employee employee) {
        if (!emailEnabled || mailSender == null) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(fromAddress); // Notify HR
            helper.setSubject("✅ Onboarding Complete: " + employee.getFullName());

            String html = "<h2>" + employee.getFullName() + " has completed onboarding!</h2>" +
                          "<p>Role: " + employee.getRole() + "</p>" +
                          "<p>All tasks checked off. Please update records.</p>";
            helper.setText(html, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send completion email: " + e.getMessage());
        }
    }

    private String buildWelcomeHtml(Employee emp) {
        String dept = emp.getDepartment() != null ? emp.getDepartment().getName() : "your team";
        String startDate = emp.getStartDate() != null ? emp.getStartDate().toString() : "soon";

        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; background: #0f0f1a; color: #e2e8f0; padding: 40px;">
              <div style="max-width:600px;margin:0 auto;background:#1a1a2e;border-radius:16px;padding:40px;border:1px solid #7c3aed;">
                <h1 style="color:#a78bfa;">Welcome to SmartHire! 🎉</h1>
                <p>Hi <strong>%s</strong>,</p>
                <p>We're thrilled to have you join <strong>%s</strong> as a <strong>%s</strong>.</p>
                <p>Your start date is <strong>%s</strong>.</p>
                <p>You have 5 onboarding tasks waiting for you in the portal. Complete them to get fully set up!</p>
                <a href="https://smarthire.vercel.app" style="display:inline-block;background:#7c3aed;color:#fff;padding:12px 24px;border-radius:8px;text-decoration:none;margin-top:20px;">
                  View My Onboarding Portal →
                </a>
                <p style="margin-top:30px;color:#64748b;font-size:12px;">SmartHire Onboarding System</p>
              </div>
            </body>
            </html>
            """.formatted(emp.getFullName(), dept, emp.getRole() != null ? emp.getRole() : "team member", startDate);
    }
}
