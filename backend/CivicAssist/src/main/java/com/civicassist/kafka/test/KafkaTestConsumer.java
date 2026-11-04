package com.civicassist.kafka.test;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaTestConsumer {
    @KafkaListener(topics="civicassist.kafka-test")
    public void consumeMessage(String message){
        System.out.println("Message received from kafka: "+message);
    }
}
