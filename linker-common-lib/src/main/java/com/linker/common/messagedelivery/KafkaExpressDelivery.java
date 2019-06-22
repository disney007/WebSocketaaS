package com.linker.common.messagedelivery;

import lombok.Setter;
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

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Slf4j
public class KafkaExpressDelivery implements ExpressDelivery {
    private final static Duration POLL_TIMEOUT = Duration.ofMillis(Long.MAX_VALUE);

    String hosts;
    String consumerTopic;
    String consumerGroupId;

    public KafkaExpressDelivery(String hosts, String consumerTopic, String consumerGroupId) {
        this.hosts = hosts;
        this.consumerTopic = consumerTopic;
        this.consumerGroupId = consumerGroupId;
    }

    @Setter
    ExpressDeliveryListener listener;

    Consumer<String, String> consumer;

    Producer<String, String> producer;

    private Producer<String, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }

    Consumer<String, String> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        // Create the consumer using props.
        final Consumer<String, String> consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(consumerTopic));
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

    @Override
    public void start() {
        consumer = createConsumer();
        producer = createProducer();
        log.info("kafka:consumer connected");
        new Thread(this::runConsumer).start();
    }

    @Override
    public void stop() {
        log.info("kafka:close consumer");
        consumer.close();
    }

    @Override
    public void deliveryMessage(String target, String message) throws IOException {
        ProducerRecord<String, String> record = new ProducerRecord<>(target, message);
        producer.send(record, (recordMetadata, e) -> {
            log.info("kafka:send message complete");
        });
    }

    @Override
    public void onMessageArrived(String message) {
        if (this.listener != null) {
            this.listener.onMessageArrived(this, message);
        }
    }

    @Override
    public ExpressDeliveryType getType() {
        return ExpressDeliveryType.KAFKA;
    }
}
