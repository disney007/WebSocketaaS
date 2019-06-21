package com.linker.connector.messagedelivery;

import com.linker.common.express.ExpressDeliveryType;
import com.linker.connector.PostOffice;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Service
@Slf4j
public class KafkaExpressDelivery implements ExpressDelivery {
    private final static String TOPIC = "outgoing-topic";
    private final static String TOPIC_INCOMING = "topic-incoming";
    private final static String BOOTSTRAP_SERVERS = "localhost:29092";
    private final static Duration POLL_TIMEOUT = Duration.ofMillis(Long.MAX_VALUE);

    @Autowired
    PostOffice postOffice;

    Consumer<String, String> consumer = createConsumer();

    Producer<String, String> producer = createProducer();

    private Producer<String, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }

    Consumer<String, String> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group-outgoing");

        // Create the consumer using props.
        final Consumer<String, String> consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(TOPIC));
        return consumer;
    }

    void runConsumer() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(POLL_TIMEOUT);
            for (TopicPartition partition : records.partitions()) {
                List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                for (ConsumerRecord<String, String> record : partitionRecords) {
                    this.onMessageArrived(record.value());
                }
                long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
            }
        }
    }

    @PostConstruct
    public void start() {
        log.info("kafka:consumer connected");
        new Thread(this::runConsumer).start();
    }

    @PreDestroy
    public void onDestroy() {
        log.info("kafka:close consumer");
        consumer.close();
    }

    @Override
    public void deliveryMessage(String message) throws IOException {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(TOPIC_INCOMING, message);
        producer.send(record, (recordMetadata, e) -> {
            log.info("kafka:send message complete");
        });
    }

    @Override
    public void onMessageArrived(String message) {
        postOffice.onMessageArrived(message, this);
    }

    @Override
    public ExpressDeliveryType getType() {
        return ExpressDeliveryType.KAFKA;
    }
}
