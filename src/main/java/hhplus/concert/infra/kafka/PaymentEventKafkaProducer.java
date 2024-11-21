package hhplus.concert.infra.kafka;

import hhplus.concert.domain.payment.event.components.PaymentEventPublisher;
import hhplus.concert.domain.payment.event.models.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Qualifier("kafkaPublisher")
public class PaymentEventKafkaProducer implements PaymentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public PaymentEventKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     *  Kafka Template 을 통해 Kafka 로 message 를 전송
     */
    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }

    /**
     *  Kafka Template 을 통해 Kafka 로 payment-event message 전송
     */
    @Override
    public void publishPaymentEvent(PaymentEvent event) {
        kafkaTemplate.send("payment-event", event.getPaymentId().toString());
    }
}
