package com.example.mswallet.repository;

import com.example.mswallet.model.Wallet;
import reactor.core.publisher.Mono;

public interface IWalletRepository extends IRepository<Wallet, String>{
    Mono<Wallet> findWalletByCustomer_Phone(String phone);
    Boolean existsWalletByCustomer_CustomerIdentityNumber(String customerIdentityNumber);
}
