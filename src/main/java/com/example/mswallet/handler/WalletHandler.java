package com.example.mswallet.handler;

import com.example.mswallet.model.Wallet;
import com.example.mswallet.services.IWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class WalletHandler {
    private final IWalletService walletService;

    @Autowired
    public WalletHandler(IWalletService walletService) {
        this.walletService = walletService;
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(walletService.findAll(), Wallet.class);
    }
}
