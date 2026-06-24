package ao.angogas.backend.service.impl;

import ao.angogas.backend.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@angogas.ao}")
    private String fromEmail;

    @Override
    @Async
    public void sendVerificationEmail(String to, String nome, String token) {
        String link = frontendUrl + "/verificar-email?token=" + token;
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "AngoGÁS");
            helper.setTo(to);
            helper.setSubject("Verifica o teu email — AngoGÁS");
            helper.setText(buildHtml(nome, link), true);
            mailSender.send(message);
            log.info("Verification email sent to {}", to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
        }
    }

    private String buildHtml(String nome, String link) {
        return """
            <!DOCTYPE html>
            <html lang="pt">
            <body style="margin:0;padding:0;background:#F5F5F5;font-family:Inter,Arial,sans-serif">
              <div style="max-width:560px;margin:40px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08)">
                <div style="background:#1A1A2E;padding:32px 40px;text-align:center">
                  <p style="margin:0;font-size:26px;font-weight:900;color:#E85C00;letter-spacing:-1px">AngoGÁS</p>
                  <p style="margin:4px 0 0;color:rgba(255,255,255,.4);font-size:11px;letter-spacing:.12em;text-transform:uppercase">A energia que move Angola</p>
                </div>
                <div style="padding:40px">
                  <h2 style="margin:0 0 8px;font-size:22px;font-weight:800;color:#1A1A1A">Olá, %s!</h2>
                  <p style="color:#6B7280;font-size:15px;line-height:1.6;margin:0 0 28px">
                    Clica no botão abaixo para verificar o teu endereço de email e activar todas as funcionalidades da tua conta.
                  </p>
                  <div style="text-align:center;margin:32px 0">
                    <a href="%s"
                       style="display:inline-block;background:#E85C00;color:#fff;font-weight:800;font-size:15px;padding:14px 36px;border-radius:12px;text-decoration:none;letter-spacing:.02em">
                      Verificar email
                    </a>
                  </div>
                  <p style="color:#9CA3AF;font-size:13px;line-height:1.5;margin:0">
                    Este link expira em <strong>24 horas</strong>. Se não criaste conta no AngoGÁS, ignora este email.
                  </p>
                  <hr style="border:none;border-top:1px solid #F3F4F6;margin:24px 0">
                  <p style="color:#9CA3AF;font-size:12px;margin:0">
                    Ou copia este endereço para o browser:<br>
                    <span style="color:#E85C00;word-break:break-all">%s</span>
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(nome, link, link);
    }
}
