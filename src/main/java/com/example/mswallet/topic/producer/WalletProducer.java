package com.example.mswallet.topic.producer;

import com.example.mswallet.model.Acquisition;
import com.example.mswallet.model.Customer;
import com.example.mswallet.model.dto.CreateAcquisitionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WalletProducer {

    private final static String SERVICE_WALLET_TOPIC = "service-wallet-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public WalletProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendSaveCustomerService(Customer customer) {
        kafkaTemplate.send(SERVICE_WALLET_TOPIC, customer );
    }

    public void sendSaveAcquisitionService(CreateAcquisitionDTO acquisition) {
        kafkaTemplate.send(SERVICE_WALLET_TOPIC, acquisition );
    }
}
