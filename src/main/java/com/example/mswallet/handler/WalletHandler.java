package com.example.mswallet.handler;

import com.example.mswallet.model.Wallet;
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
    @Autowired
    public WalletHandler(IWalletService walletService, WalletProducer walletProducer) {
        this.walletService = walletService;
        this.walletProducer = walletProducer;
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

}
