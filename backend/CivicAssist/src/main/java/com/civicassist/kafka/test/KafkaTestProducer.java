package com.civicassist.kafka.test;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaTestProducer {
    private static final String TOPIC="civicassist.kafka-test";
    private final KafkaTemplate<String,String> kafkaTemplate;

    public KafkaTestProducer(KafkaTemplate<String,String> kafkaTemplate)
    {
        this.kafkaTemplate=kafkaTemplate;
    }

    public void sendMessage(String message){
        kafkaTemplate.send(TOPIC,message)
                .whenComplete((result,exception)->{
                    if(exception!=null){
                        System.err.println("Failed to send message: "+exception.getMessage());
                    }else{
                        System.out.println("Message sent successfully: "+message);
                    }
                });
    }
}
