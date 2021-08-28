package com.example.mswallet.topic.producer;

import com.example.mswallet.model.Customer;
import com.example.mswallet.model.Deposit;
import com.example.mswallet.model.Retire;
import com.example.mswallet.model.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class WalletProducer {

    private final static String CREATE_CUSTOMER_TOPIC = "created-customer-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String walletTopic = "created-wallet-topic";

    private final String transferWithdrawalTopic = "created-wallet-retire-topic";

    private final String transferDepositTopic = "created-wallet-deposit-topic";


    @Autowired
    public WalletProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /** Envia datos del wallet al topico. */
    public void sendCreatedWalletTopic(Wallet wallet) {

        kafkaTemplate.send(walletTopic, wallet);

    }

    /** Envia datos del transfer al topico. */
    public void sendCreatedTransferWithdrawalTopic(Retire retire) {

        kafkaTemplate.send(transferWithdrawalTopic, retire);

    }

    /** Envia datos del transfer al topico. */
    public void sendCreatedTransferDepositTopic(Deposit deposit) {

        kafkaTemplate.send(transferDepositTopic, deposit);

    }

    public void sendSaveCustomerService(Customer customer) {
        kafkaTemplate.send(CREATE_CUSTOMER_TOPIC, customer );
    }
}
