package com.linker.processor.messagedelivery;

import com.linker.common.Message;
import com.linker.common.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Properties;

@Service
@Slf4j
public class KafkaExpressDelivery implements ExpressDelivery {
    private final static String TOPIC = "outgoing-topic";
    private final static String BOOTSTRAP_SERVERS = "localhost:29092";

    Producer<String, String> producer = createProducer();

    private Producer<String, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }

    @Override
    public void deliveryMessage(Message message) throws IOException {
        String json = Utils.toJson(message);
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(TOPIC, json);
        producer.send(record, (recordMetadata, e) -> {
            log.info("kafka:send message complete");
        });
    }

    @Override
    public void onMessageArrived(Message message) {

    }
}
