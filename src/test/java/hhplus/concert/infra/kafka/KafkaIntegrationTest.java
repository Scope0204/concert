package hhplus.concert.infra.kafka;

import hhplus.concert.api.interfaces.consumer.PaymentEventKafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class KafkaIntegrationTest {

    @Autowired
    PaymentEventKafkaConsumer consumer;

    @Autowired
    PaymentEventKafkaProducer producer;

    @Test
    void 카프카를_사용하여_발행된_메시지가_정상적으로_소비되었는지_내용을_확인() throws InterruptedException {
        // given
        String topic = "test-topic";
        String message = "test_message!";

        // when
        producer.send(topic, message);

        // then
        Thread.sleep(5000); // 5초 대기
        assertThat(consumer.getReceivedMessage()).isEqualTo(message);
    }
}