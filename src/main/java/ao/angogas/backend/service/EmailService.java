package ao.angogas.backend.service;

public interface EmailService {
    void sendVerificationEmail(String to, String nome, String token);
}
