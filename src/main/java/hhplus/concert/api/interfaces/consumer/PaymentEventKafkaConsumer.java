package hhplus.concert.api.interfaces.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentEventKafkaConsumer {

    private String receivedMessage;

    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void consume(String message) {
        log.info("Received message: {} " , message);
        this.receivedMessage = message;
    }

    public String getReceivedMessage() {
        return receivedMessage;
    }
}
