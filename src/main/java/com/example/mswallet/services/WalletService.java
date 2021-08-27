package com.example.mswallet.services;

import com.example.mswallet.model.Wallet;
import com.example.mswallet.repository.IRepository;
import com.example.mswallet.repository.IWalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService extends BaseService<Wallet, String> implements IWalletService{
    private final IWalletRepository repository;

    @Autowired
    public WalletService(IWalletRepository repository) {
        this.repository = repository;
    }

    @Override
    protected IRepository<Wallet, String> getRepository() {
        return repository;
    }
}
