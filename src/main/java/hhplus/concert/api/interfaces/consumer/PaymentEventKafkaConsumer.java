package hhplus.concert.api.interfaces.consumer;

import hhplus.concert.domain.payment.event.components.PaymentEventOutBoxService;
import hhplus.concert.support.type.EventStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventKafkaConsumer {

    private String receivedMessage;

    private final PaymentEventOutBoxService paymentEventOutBoxService;

    public PaymentEventKafkaConsumer(PaymentEventOutBoxService paymentEventOutBoxService) {
        this.paymentEventOutBoxService = paymentEventOutBoxService;
    }
    
    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void consume(String message) {
        log.info("Received message: {} " , message);
        this.receivedMessage = message;
    }

    @Async
    @KafkaListener(topics = "payment-event", groupId = "payment-group")
    public void handleSendMessageKafkaEvent(String paymentId) {
        log.info("KafkaEvent received. payment Id = {}", paymentId);
        try {
            long paymentIdLong = Long.parseLong(paymentId);
            paymentEventOutBoxService.updateEventStatus(paymentIdLong, EventStatus.PUBLISHED);
        } catch (NumberFormatException e) {
            log.error("Invalid paymentId format: {}", paymentId);
        }
    }

    public String getReceivedMessage() {
        return receivedMessage;
    }
}
