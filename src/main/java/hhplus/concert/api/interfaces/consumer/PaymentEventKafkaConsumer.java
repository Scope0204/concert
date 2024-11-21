package hhplus.concert.api.interfaces.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventKafkaConsumer {

    private String receivedMessage;

    @KafkaListener(topics = "test_topic", groupId = "test-group")
    public void consume(String message) {
        System.out.println("Received message: " + message);
        this.receivedMessage = message;
    }

    public String getReceivedMessage() {
        return receivedMessage;
    }
}
