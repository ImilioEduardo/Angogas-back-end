package ao.angogas.backend.service.impl;

import ao.angogas.backend.model.Order;
import ao.angogas.backend.model.Payment;
import ao.angogas.backend.repository.OrderRepository;
import ao.angogas.backend.repository.PaymentRepository;
import ao.angogas.backend.service.ReportService;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] exportOrdersCsv(LocalDate from, LocalDate to) {
        List<Order> orders = orderRepository.findAll(
                PageRequest.of(0, 10000, Sort.by("criadoEm").descending())).getContent();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            writer.writeNext(new String[]{"ID", "Cliente", "Entregador", "Status", "Total (Kz)", "Pagamento", "Data"});

            for (Order o : orders) {
                writer.writeNext(new String[]{
                        o.getId().toString(),
                        o.getCliente().getNome(),
                        o.getEntregador() != null ? o.getEntregador().getNome() : "",
                        o.getStatus().name(),
                        o.getTotalKz().toPlainString(),
                        o.getMetodoPagamento() != null ? o.getMetodoPagamento().name() : "",
                        o.getCriadoEm().toLocalDate().toString()
                });
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar CSV de pedidos", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportPaymentsCsv(LocalDate from, LocalDate to) {
        List<Payment> payments = paymentRepository.findAll(
                PageRequest.of(0, 10000, Sort.by("criadoEm").descending())).getContent();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            writer.writeNext(new String[]{"ID", "Pedido ID", "Método", "Status", "Valor (Kz)", "Data", "Confirmado Em"});

            for (Payment p : payments) {
                writer.writeNext(new String[]{
                        p.getId().toString(),
                        p.getOrder().getId().toString(),
                        p.getMetodo().name(),
                        p.getStatus().name(),
                        p.getValorKz().toPlainString(),
                        p.getCriadoEm().toLocalDate().toString(),
                        p.getConfirmadoEm() != null ? p.getConfirmadoEm().toLocalDate().toString() : ""
                });
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar CSV de pagamentos", e);
        }
    }
}
