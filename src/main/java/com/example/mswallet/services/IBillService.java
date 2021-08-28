package com.example.mswallet.services;

import com.example.mswallet.model.Bill;
import com.example.mswallet.model.Wallet;
import reactor.core.publisher.Mono;

public interface IBillService extends IBaseService<Bill, String>{
    Mono<Bill> findByAccountNumber(String accountNumber);
}
