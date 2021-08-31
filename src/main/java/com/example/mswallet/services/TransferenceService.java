package com.example.mswallet.services;

import com.example.mswallet.model.Transference;
import com.example.mswallet.repository.IRepository;
import com.example.mswallet.repository.ITransferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransferenceService extends BaseService<Transference, String> implements ITransferenceService{
    private final ITransferenceRepository repository;

    @Autowired
    public TransferenceService(ITransferenceRepository repository) {
        this.repository = repository;
    }

    @Override
    protected IRepository<Transference, String> getRepository() {
        return repository;
    }
}
