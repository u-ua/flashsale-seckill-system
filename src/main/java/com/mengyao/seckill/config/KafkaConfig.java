package com.mengyao.seckill.config;

import com.mengyao.seckill.order.OrderMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;
import java.util.*;

@Configuration
public class KafkaConfig {
    @Bean NewTopic ordersTopic(@Value("${seckill.kafka-topic}") String topic) { return new NewTopic(topic, 3, (short) 1); }
    @Bean
    ProducerFactory<String, OrderMessage> orderProducerFactory(@Value("${spring.kafka.bootstrap-servers}") String servers) {
        Map<String, Object> p = new HashMap<>();
        p.put("bootstrap.servers", servers); p.put("key.serializer", StringSerializer.class); p.put("value.serializer", JsonSerializer.class);
        p.put("enable.idempotence", true); p.put("acks", "all");
        return new DefaultKafkaProducerFactory<>(p);
    }
    @Bean KafkaTemplate<String, OrderMessage> orderKafkaTemplate(ProducerFactory<String, OrderMessage> pf) { return new KafkaTemplate<>(pf); }
}
