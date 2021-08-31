package com.example.mswallet.handler;

import com.example.mswallet.coverter.TransferenceConverter;
import com.example.mswallet.coverter.WalletConverter;
import com.example.mswallet.model.*;
import com.example.mswallet.model.dto.CreateAcquisitionDTO;
import com.example.mswallet.model.dto.CreateTransferenceDTO;
import com.example.mswallet.model.dto.CreateWalletDTO;
import com.example.mswallet.model.dto.TransferenceDTO;
import com.example.mswallet.services.AcquisitionService;
import com.example.mswallet.services.CustomerService;
import com.example.mswallet.services.IWalletService;
import com.example.mswallet.services.TransferenceService;
import com.example.mswallet.topic.producer.WalletProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

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
    private final AcquisitionService acquisitionService;
    private final TransferenceService transferenceService;
    private final WalletConverter walletConverter;
    private final TransferenceConverter transferenceConverter;
    @Autowired
    public WalletHandler(IWalletService walletService, WalletProducer walletProducer, CustomerService customerService, AcquisitionService acquisitionService, TransferenceService transferenceService, WalletConverter walletConverter, TransferenceConverter transferenceConverter) {
        this.walletService = walletService;
        this.walletProducer = walletProducer;
        this.customerService = customerService;
        this.acquisitionService = acquisitionService;
        this.transferenceService = transferenceService;
        this.walletConverter = walletConverter;
        this.transferenceConverter = transferenceConverter;
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(walletService.findAll(), Wallet.class);
    }
    public Mono<ServerResponse> findByIdentityNumber(ServerRequest request){
        String identityNumber = request.pathVariable("identityNumber");
        return acquisitionService.findAllByCustomer(identityNumber)
                .collectList()
                .flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
                .switchIfEmpty(Mono.error(new RuntimeException("THE PRODUCT DOES NOT EXIST")));
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
    }
    public Mono<ServerResponse> transferenceWallet(ServerRequest request){
        Mono<Transference> transferRequest = request.bodyToMono(Transference.class);
        return transferRequest
                .zipWhen(transfer -> {
                    Mono<Wallet> origenWallet = walletService
                            .findWalletByCustomer_Phone(transfer.getOrigenWallet().getCustomer().getPhone())
                            .switchIfEmpty(Mono.error(new RuntimeException("Walllet does not exist")));

                    Mono<Wallet> destineWallet = walletService
                            .findWalletByCustomer_Phone(transfer.getDestineWallet().getCustomer().getPhone())
                            .switchIfEmpty(Mono.error(new RuntimeException("Walllet does not exist")));

                    return Mono.zip(origenWallet, destineWallet);
                })
                .zipWhen(dataWallet -> acquisitionService
                        .findAllByCustomer(dataWallet.getT2().getT1().getCustomer().getCustomerIdentityNumber())
                        .collectList()
                        .flatMap(acquisitions -> {
                            Acquisition origen = acquisitions.stream()
                                    .filter(acquisition -> acquisition.getProduct().getProductName().equals("MONEDERO"))
                                    .findFirst()
                                    .orElse(new Acquisition());
                            CreateTransferenceDTO retire = new CreateTransferenceDTO();
                            retire.setAmount(dataWallet.getT1().getAmount());
                            retire.setAccountNumber(origen.getBill().getAccountNumber());
                            retire.setDescription(String.format("send money from %s to %s",
                                    dataWallet.getT2().getT1().getCustomer().getPhone(),
                                    dataWallet.getT2().getT2().getCustomer().getPhone()));
                            retire.setCardNumber("");
                            walletProducer.sendSaveRetireService(retire);
                            return Mono.just(origen);
                        }))
                .zipWhen(dataWalletDestine -> {
                    return acquisitionService
                            .findAllByCustomer(dataWalletDestine.getT1().getT2().getT2().getCustomer().getCustomerIdentityNumber())
                            .collectList()
                            .flatMap(acquisitionsDestine -> {
                                Acquisition destine = acquisitionsDestine.stream()
                                        .filter(acquisition -> acquisition.getProduct().getProductName().equals("MONEDERO"))
                                        .findFirst()
                                        .orElse(new Acquisition());
                                CreateTransferenceDTO deposit = new CreateTransferenceDTO();
                                deposit.setAmount(dataWalletDestine.getT1().getT1().getAmount());
                                deposit.setAccountNumber(destine.getBill().getAccountNumber());
                                deposit.setDescription(String.format("receive money from %s to %s",
                                        dataWalletDestine.getT1().getT2().getT1().getCustomer().getPhone(),
                                        dataWalletDestine.getT1().getT2().getT2().getCustomer().getPhone()));
                                deposit.setCardNumber("");
                                walletProducer.sendSaveDepositService(deposit);
                                return Mono.just(deposit);
                            });
                })
                .flatMap(transference -> {
                    TransferenceDTO transferenceDTO = new TransferenceDTO();
                    transferenceDTO.setOrigenWallet(transference.getT1().getT1().getT2().getT1());
                    transferenceDTO.setDestineWallet(transference.getT1().getT1().getT2().getT2());
                    transferenceDTO.setAmount(transference.getT1().getT1().getT1().getAmount());
                    Transference transferenceCreate = transferenceConverter.convertToEntity(transferenceDTO, new Transference());
                    return transferenceService.create(transferenceCreate);
                })
                .flatMap(transference -> ServerResponse.created(URI.create("/transference/".concat(transference.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(transference))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));


    }
}
