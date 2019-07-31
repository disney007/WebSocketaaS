package com.linker.common.messagedelivery;

import com.google.common.collect.ImmutableSet;
import com.linker.common.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KafkaExpressDelivery implements ExpressDelivery {
    private final static Duration POLL_TIMEOUT = Duration.ofMillis(Long.MAX_VALUE);

    String hosts;
    String consumerTopic;
    String consumerGroupId;

    @Setter
    KafkaCache kafkaCache;

    public KafkaExpressDelivery(String hosts, String consumerTopic, String consumerGroupId) {
        this.hosts = hosts;
        this.consumerTopic = consumerTopic;
        this.consumerGroupId = consumerGroupId;
    }

    @Setter
    @Getter
    ExpressDeliveryListener listener;

    Consumer<String, byte[]> consumer;

    Producer<String, byte[]> producer;

    private Producer<String, byte[]> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new KafkaProducer<>(props);
    }

    Consumer<String, byte[]> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        // Create the consumer using props.
        final Consumer<String, byte[]> consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(consumerTopic));
        return consumer;
    }

    List<String> processRecords(List<ConsumerRecord<String, byte[]>> records) {
        Set<String> duplicateKeys = ImmutableSet.of();
        List<String> originalKeys = null;
        if (kafkaCache != null) {
            originalKeys = records.stream().map(ConsumerRecord::key).collect(Collectors.toList());
            duplicateKeys = kafkaCache.getDuplicateItems(originalKeys);
        }

        for (ConsumerRecord<String, byte[]> record : records) {
            String key = record.key();
            if (!duplicateKeys.contains(key)) {
                this.onMessageArrived(record.value());
                if (kafkaCache != null) {
                    kafkaCache.addItem(key);
                }
            }
        }

        return originalKeys;
    }

    void runConsumer() {
        try {
            while (true) {
                ConsumerRecords<String, byte[]> records = consumer.poll(POLL_TIMEOUT);
                for (TopicPartition partition : records.partitions()) {
                    List<ConsumerRecord<String, byte[]>> partitionRecords = records.records(partition);
                    List<String> keys = processRecords(partitionRecords);
                    long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                    consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
                    if (kafkaCache != null) {
                        kafkaCache.deleteItems(keys);
                    }
                }
            }
        } catch (WakeupException e) {
            log.info("wake up consumer to showdown");
        } finally {
            consumer.close();
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
        consumer.wakeup();
        Utils.sleep(3000L);
        log.info("kafka:close producer");
        producer.close();
    }

    @Override
    public void deliverMessage(String target, byte[] message) throws IOException {
        try {
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(target, UUID.randomUUID().toString(), message);
            producer.send(record);
            if (listener != null) {
                listener.onMessageDelivered(this, target, message);
            }
        } catch (KafkaException e) {
            throw new IOException("kafka: failed to send message", e);
        }
    }

    @Override
    public void onMessageArrived(byte[] message) {
        if (this.listener != null) {
            this.listener.onMessageArrived(this, message);
        }
    }

    @Override
    public ExpressDeliveryType getType() {
        return ExpressDeliveryType.KAFKA;
    }
}
