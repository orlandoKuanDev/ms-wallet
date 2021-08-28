package com.example.mswallet.repository;

import com.example.mswallet.model.Bill;
import com.example.mswallet.model.Wallet;
import reactor.core.publisher.Mono;

public interface IBillRepository extends IRepository<Bill, String>{
    Mono<Bill> findByAccountNumber(String accountNumber);
}
