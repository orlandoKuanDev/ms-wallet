package com.example.mswallet.handler;

import com.example.mswallet.coverter.TransferenceConverter;
import com.example.mswallet.coverter.WalletConverter;
import com.example.mswallet.model.*;
import com.example.mswallet.model.dto.*;
import com.example.mswallet.model.dto.response.ApiResponse;
import com.example.mswallet.services.AcquisitionService;
import com.example.mswallet.services.CustomerService;
import com.example.mswallet.services.IWalletService;
import com.example.mswallet.services.TransferenceService;
import com.example.mswallet.topic.producer.WalletProducer;
import com.example.mswallet.util.ImeiGenerator;
import com.example.mswallet.util.VerificationCodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
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
                .switchIfEmpty(Mono.error(new RuntimeException("THE ACQUISITION DOES NOT EXIST")));
    }
    public Mono<ServerResponse> findByPhone(ServerRequest request){
        String phone = request.pathVariable("phone");
        return walletService.findWalletByCustomer_Phone(phone)
                .flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
                .switchIfEmpty(Mono.error(new RuntimeException("THE WALLET DOES NOT EXIST")));
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
    public Mono<Customer> createCustomer(Mono<CreateWalletDTO> walletRequest){
        Mono<Customer> customerForConsumer = Mono.just(new Customer());
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
                });
    }
    public Mono<CreateWalletDTO> validateCustomer(Mono<CreateWalletDTO> walletRequest){
        return walletRequest
                .zipWhen(req -> {
                    return walletService.findWalletByCustomer_Phone(req.getPhone())
                            .switchIfEmpty(Mono.defer(() -> {
                        return Mono.just(new Wallet());
                    }));
                })
                .flatMap(wallet -> {
                    if(wallet.getT2().getCustomer().getCustomerIdentityNumber() != null){
                        return Mono.error(() -> new RuntimeException("The customer already exist"));
                    }
                    return Mono.just(wallet.getT1());
                });
    }
    public Mono<ServerResponse> createWallet(ServerRequest request){
        Mono<CreateWalletDTO> walletRequest = request.bodyToMono(CreateWalletDTO.class);
        return walletRequest
                //.as(this::validateCustomer)
                .as(this::createCustomer)
                .checkpoint("after customer create consumer")
                .delayElement(Duration.ofMillis(2000))
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
                    wallet1.setImei(new ImeiGenerator().generate());
                    wallet1.setVerificationCode(new VerificationCodeGenerator().generate(8));
                    wallet1.setVerificationPhoto(true);
                    return walletService.create(wallet1);
                })
                .flatMap(wallet -> ServerResponse.created(URI.create("/wallet/".concat(wallet.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(wallet))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));
    }

    public Mono<ServerResponse> transactionWallet(ServerRequest request){
        Mono<CreateOperationDTO> createRetireFromWallet = request.bodyToMono(CreateOperationDTO.class);
        return createRetireFromWallet
                .zipWhen(createRetire -> walletService.findWalletByCustomer_Phone(createRetire.getPhone()))
                .switchIfEmpty(Mono.error(new RuntimeException("Wallet does not exist")))
                .flatMap(wallet -> acquisitionService.findAllByCustomer(wallet.getT2().getCustomer().getCustomerIdentityNumber())
                        .collectList()
                        .flatMap(acquisitions -> {
                            Acquisition origen = acquisitions.stream()
                                    .filter(acquisition -> acquisition.getProduct().getProductName().equals("MONEDERO"))
                                    .findFirst()
                                    .orElse(new Acquisition());
                            CreateTransferenceDTO transaction = new CreateTransferenceDTO();
                            transaction.setAmount(wallet.getT1().getAmount());
                            transaction.setAccountNumber(origen.getBill().getAccountNumber());
                            transaction.setDescription(String.format("retire %s soles from wallet",
                                    wallet.getT1().getAmount()));
                            transaction.setCardNumber("");

                            String operation = wallet.getT1().getOperation();
                            if (operation.equals("deposit")){
                               walletProducer.sendSaveDepositService(transaction);
                            }else if (operation.equals("retire")){
                                walletProducer.sendSaveRetireService(transaction);
                            }else{
                               return Mono.error(() -> new RuntimeException("Operation not available"));
                            }
                            return Mono.just(origen);
                        })
                )
                .flatMap(retire -> ServerResponse.ok()
                        .contentType(APPLICATION_JSON)
                        .bodyValue(ApiResponse.builder()
                                .message("The operation was successful")
                                .data(retire)
                                .time(LocalDateTime.now())
                                .build()))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));
    }


    public Mono<ServerResponse> transferenceWallet(ServerRequest request){
        Mono<TransferenceRequestDTO> transferRequest = request.bodyToMono(TransferenceRequestDTO.class);
        return transferRequest
                .zipWhen(transfer -> {
                    Mono<Wallet> origenWallet = walletService
                            .findWalletByCustomer_Phone(transfer.getPhoneOrigen())
                            .switchIfEmpty(Mono.error(new RuntimeException("Wallet origen does not exist")));

                    Mono<Wallet> destineWallet = walletService
                            .findWalletByCustomer_Phone(transfer.getPhoneDestine())
                            .switchIfEmpty(Mono.error(new RuntimeException("Wallet destine does not exist")));
                    return Mono.zip(origenWallet, destineWallet);
                })
                .zipWhen(dataWallet -> {
                    log.info("origenWallet, {}", (dataWallet.getT2().getT1().getCustomer().getCustomerIdentityNumber()));
                    log.info("destineWallet, {}", (dataWallet.getT2().getT2().getCustomer().getCustomerIdentityNumber()));
                    return acquisitionService
                            .findAllByCustomer(dataWallet.getT2().getT1().getCustomer().getCustomerIdentityNumber())
                            .collectList()
                            .flatMap(acquisitions -> {
                                log.info("ACQUISITION_LIST, {}", acquisitions);
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
                                log.info("RETIRE, {}", retire);
                                walletProducer.sendSaveRetireService(retire);
                                return Mono.just(origen);
                            });
                })
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
                    log.info("TRANSFERENCE_CREATE {}", transference.getT1().getT1().getT2().getT1());
                    log.info("TRANSFERENCE_CREATE {}", transference.getT1().getT1().getT2().getT2());
                    transferenceDTO.setOrigenWallet(transference.getT1().getT1().getT2().getT1());
                    transferenceDTO.setDestineWallet(transference.getT1().getT1().getT2().getT2());
                    transferenceDTO.setAmount(transference.getT1().getT1().getT1().getAmount());
                    Transference transferenceCreate = transferenceConverter.convertToEntity(transferenceDTO, new Transference());
                    return transferenceService.create(transferenceCreate);
                })
                .log()
                .flatMap(transference -> ServerResponse.created(URI.create("/transference/".concat(transference.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(transference))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));
    }

    public Mono<ServerResponse> associateWalletWithDebit(ServerRequest request){
        Mono<CreateAssociationDTO> associationDTO = request.bodyToMono(CreateAssociationDTO.class);
        return associationDTO
                .zipWhen(createRetire -> walletService.findWalletByCustomer_Phone(createRetire.getPhone()))
                .switchIfEmpty(Mono.error(new RuntimeException("Wallet does not exist")))
                .flatMap(wallet -> acquisitionService.findAllByCustomer(wallet.getT2().getCustomer().getCustomerIdentityNumber())
                        .collectList()
                        .flatMap(acquisitions -> {
                            Acquisition origen = acquisitions.stream()
                                    .filter(acquisition -> acquisition.getProduct().getProductName().equals("MONEDERO"))
                                    .findFirst()
                                    .orElse(new Acquisition());
                            CreateAssociationResponseDTO associationResponseDTO = new CreateAssociationResponseDTO();
                            associationResponseDTO.setIban(origen.getIban());
                            associationResponseDTO.setCardNumber(wallet.getT1().getCardNumber());
                            walletProducer.sendAssociateDebitService(associationResponseDTO);
                            return Mono.just(origen);
                        })
                )
                .flatMap(retire -> ServerResponse.ok()
                        .contentType(APPLICATION_JSON)
                        .bodyValue(ApiResponse.builder()
                                .message("The association of debit with wallet  was successful")
                                .data(retire)
                                .time(LocalDateTime.now())
                                .build()))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));

    }
}
