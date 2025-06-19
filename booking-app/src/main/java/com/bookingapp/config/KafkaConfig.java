package com.bookingapp.config;

import com.bookingapp.event.BookingEvent;
import com.bookingapp.event.CarEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.annotation.EnableKafka;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.topics.booking-events}")
    private String bookingEventsTopic;

    @Value("${spring.kafka.topics.car-events}")
    private String carEventsTopic;

    @Bean
    public ProducerFactory<String, BookingEvent> bookingEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, CarEvent> carEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, BookingEvent> kafkaTemplate() {
        return new KafkaTemplate<>(bookingEventProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, CarEvent> carEventKafkaTemplate() {
        return new KafkaTemplate<>(carEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, Object> objectProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplateObject() {
        return new KafkaTemplate<>(objectProducerFactory());
    }

    @Bean
    public NewTopic bookingEventsTopic() {
        return TopicBuilder.name(bookingEventsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic carEventsTopic() {
        return TopicBuilder.name(carEventsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
} 