package com.example.mswallet.handler;

import com.example.mswallet.model.Acquisition;
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
import java.util.ArrayList;
import java.util.List;

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
                .flatMap(response -> ServerResponse.created(URI.create("/wallet/".concat(response.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));
    }
    public Mono<ServerResponse> createWallet(ServerRequest request){
        Mono<CreateWalletDTO> walletRequest = request.bodyToMono(CreateWalletDTO.class);
        Mono<Customer> customerForConsumer = Mono.just(new Customer());
        Mono<CreateAcquisitionDTO> acquisitionForConsumer = Mono.just(new CreateAcquisitionDTO());
        return walletRequest
                .zipWith(customerForConsumer, (req, customer) -> {
                    customer.setCustomerType("PERSONAL");
                    customer.setCustomerIdentityType(req.getCustomerIdentityType());
                    customer.setCustomerIdentityNumber(req.getCustomerIdentityNumber());
                    customer.setName(req.getName());
                    customer.setEmail(req.getEmail());
                    customer.setPhone(req.getPhone());
                    customer.setAddress(req.getAddress());
                    walletProducer.sendSaveCustomerService(customer);
                    return customer;
                })
                .zipWhen(customer -> {
                    Acquisition createAcquisitionDTO = new Acquisition();
                    List<Customer> customers = new ArrayList<>();
                    customers.add(customer);
                    createAcquisitionDTO.setCustomerHolder(customers);
                    createAcquisitionDTO.setProduct(Product.builder()
                            .productName("MONEDERO").build());
                    createAcquisitionDTO.setInitial(0.0);
                    walletProducer.sendSaveAcquisitionService(createAcquisitionDTO);
                    return Mono.just(createAcquisitionDTO);
                })
                .flatMap(wallet -> {
                    Wallet wallet1 = new Wallet();
                    wallet1.setCustomer(wallet.getT1());
                    wallet1.setImei("446863186496");
                    wallet1.setVerificationCode("S4AS6D");
                    wallet1.setVerificationPhoto(true);
                    return walletService.create(wallet1);
                })
                .flatMap(wallet -> ServerResponse.created(URI.create("/wallet/".concat(wallet.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(wallet))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));

        /*return Mono.zip(walletRequest, customerForConsumer, acquisitionForConsumer)
                .zipWhen(data -> {
                    data.getT2().setCustomerType("PERSONAL");
                    data.getT2().setCustomerIdentityType(data.getT1().getCustomerIdentityType());
                    data.getT2().setCustomerIdentityNumber(data.getT1().getCustomerIdentityNumber());
                    data.getT2().setName(data.getT1().getName());
                    data.getT2().setEmail(data.getT1().getEmail());
                    data.getT2().setPhone(data.getT1().getPhone());
                    data.getT2().setAddress(data.getT1().getAddress());

                    walletProducer.sendSaveCustomerService(data.getT2());
                   return Mono.just(data.getT2());
                })
                .flatMap(result -> {
                    result.setCustomerHolder(result.getT2().getCustomer());
                    result.getT1().getT3().setProduct(Product.builder()
                                    .productName("MONEDERO")
                            .build());
                    result.getT1().getT3().setInitial(0.0);
                    walletProducer.sendSaveAcquisitionService(result.getT1().getT3());
                    return Mono.just(result.getT2());
                })
                .flatMap(wallet -> ServerResponse.created(URI.create("/wallet/".concat(wallet.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(wallet))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));*/
    }
}
