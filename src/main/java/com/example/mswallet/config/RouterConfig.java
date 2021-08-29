package com.example.mswallet.config;

import com.example.mswallet.handler.WalletHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {
    @Bean
    public RouterFunction<ServerResponse> rutas(WalletHandler handler){
        return route(GET("/wallets"), handler::findAll)
                .andRoute(POST("/wallet"), handler::save)
                .andRoute(POST("/wallet/create"), handler::createWallet);
    }
}