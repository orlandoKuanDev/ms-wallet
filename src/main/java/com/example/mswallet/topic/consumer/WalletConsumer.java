package com.example.mswallet.topic.consumer;

import com.example.mswallet.model.Bill;
import com.example.mswallet.services.IBillService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

@Component
public class WalletConsumer {


    private final IBillService billService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public WalletConsumer(IBillService billService) {
        this.billService = billService;
    }

    @KafkaListener(topics = "created-bill-topic", groupId = "wallet-group")
    public Disposable retrieveCreatedAccount(String data) throws JsonProcessingException {

        Bill bill = objectMapper.readValue(data, Bill.class);

        return Mono.just(bill)
                .log()
                .flatMap(billService::update)
                .subscribe();

    }

}
