package com.example.mswallet.services;

import com.example.mswallet.model.Wallet;
import reactor.core.publisher.Mono;

public interface IWalletService extends IBaseService<Wallet, String>{
    Mono<Wallet> findWalletByCustomer_Phone(String phone);
    Boolean existsWalletByCustomer_CustomerIdentityNumber(String customerIdentityNumber);
}
