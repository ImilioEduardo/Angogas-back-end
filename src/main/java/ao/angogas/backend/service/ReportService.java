package ao.angogas.backend.service;

import java.time.LocalDate;

public interface ReportService {
    byte[] exportOrdersCsv(LocalDate from, LocalDate to);
    byte[] exportPaymentsCsv(LocalDate from, LocalDate to);
}
