package com.example.mswallet.services;

import com.example.mswallet.model.Bill;
import com.example.mswallet.repository.IBillRepository;
import com.example.mswallet.repository.IRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BillService extends BaseService<Bill, String> implements IBillService{
    private final IBillRepository repository;

    @Autowired
    public BillService(IBillRepository repository) {
        this.repository = repository;
    }

    @Override
    protected IRepository<Bill, String> getRepository() {
        return repository;
    }

    @Override
    public Mono<Bill> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber);
    }
}
