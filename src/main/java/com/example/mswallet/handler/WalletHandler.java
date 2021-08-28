package com.example.mswallet.handler;

import com.example.mswallet.model.Customer;
import com.example.mswallet.model.Wallet;
import com.example.mswallet.services.CustomerService;
import com.example.mswallet.services.IWalletService;
import com.example.mswallet.topic.producer.WalletProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class WalletHandler {
    private final IWalletService walletService;
    private final WalletProducer walletProducer;
    private final CustomerService customerService;
    @Autowired
    public WalletHandler(IWalletService walletService, WalletProducer walletProducer, CustomerService customerService) {
        this.walletService = walletService;
        this.walletProducer = walletProducer;
        this.customerService = customerService;
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(walletService.findAll(), Wallet.class);
    }

    public Mono<ServerResponse> save(ServerRequest request){
        Mono<Wallet> walletRequest = request.bodyToMono(Wallet.class);
        return walletRequest.flatMap(walletService::create)
                .flatMap(customer -> {
                    walletProducer.sendSaveCustomerService(customer.getCustomer());
                    return Mono.just(customer);
                })
                .flatMap(customer -> ServerResponse.created(URI.create("/customer/".concat(customer.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(customer))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));
    }
    public Mono<ServerResponse> createWallet(ServerRequest request){
        Mono<Wallet> walletRequest = request.bodyToMono(Wallet.class);
        walletRequest
                .zipWhen(wallet -> customerService.findByCustomerIdentityNumber(wallet.getCustomer().getCustomerIdentityNumber()))
                .zipWhen(data -> {
                    //crear acquistion
                });
    }
}
