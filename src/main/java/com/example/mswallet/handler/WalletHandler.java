package com.example.mswallet.handler;

import com.example.mswallet.model.Customer;
import com.example.mswallet.model.Product;
import com.example.mswallet.model.Wallet;
import com.example.mswallet.model.dto.CreateAcquisitionDTO;
import com.example.mswallet.model.dto.CreateWalletDTO;
import com.example.mswallet.services.CustomerService;
import com.example.mswallet.services.IWalletService;
import com.example.mswallet.topic.producer.WalletProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.DataInput;
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
                .zipWhen(customer -> {
                    walletProducer.sendSaveCustomerService(customer.getCustomer());
                    return Mono.just(customer);
                })
                .flatMap(response -> ServerResponse.created(URI.create("/wallet/".concat(response.getT1().getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(response.getT1()))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));
    }
    public Mono<ServerResponse> createWallet(ServerRequest request){
        Mono<CreateWalletDTO> walletRequest = request.bodyToMono(CreateWalletDTO.class);
        Mono<Customer> customerForConsumer = Mono.just(new Customer());
        Mono<CreateAcquisitionDTO> acquisitionForConsumer = Mono.just(new CreateAcquisitionDTO());
        return Mono.zip(walletRequest, customerForConsumer, acquisitionForConsumer)
                .zipWhen(data -> {
                    data.getT2().setCustomerType("PERSONAL");
                    data.getT2().setCustomerIdentityType(data.getT1().getCustomerIdentityType());
                    data.getT2().setCustomerIdentityNumber(data.getT1().getCustomerIdentityNumber());
                    data.getT2().setName(data.getT1().getName());
                    data.getT2().setEmail(data.getT1().getEmail());
                    data.getT2().setPhone(data.getT1().getPhone());
                    data.getT2().setAddress(data.getT1().getAddress());

                    walletProducer.sendSaveCustomerService(data.getT2());
                    return walletService.create(Wallet.builder()
                                    .customer(data.getT2())
                                    .verificationCode("DFH19854")
                                    .imei("947859511")
                                    .verificationPhoto(true)
                            .build());
                })
                .flatMap(result -> {
                    result.getT1().getT3().setCustomerHolder(result.getT2().getCustomer());
                    result.getT1().getT3().setProduct(Product.builder()
                                    .productName("MONEDERO")
                            .build());
                    walletProducer.sendSaveAcquisitionService(result.getT1().getT3());
                    return Mono.just(result.getT2());
                })
                .flatMap(wallet -> ServerResponse.created(URI.create("/wallet/".concat(wallet.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(wallet))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));
    }
}
