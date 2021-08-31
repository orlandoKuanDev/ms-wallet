package com.example.mswallet.coverter;

import com.example.mswallet.model.Wallet;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WalletConverter implements BaseConverter<Wallet, Object> {

    private final ModelMapper modelMapper;

    @Autowired
    public WalletConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public List<Object> convertToDto(List<Wallet> entity, Object dto) {
        return null;
    }

    @Override
    public Page<Object> convertToDto(Page<Wallet> entity, Object dto) {
        return null;
    }

    @Override
    public Object convertToDto(Wallet entity, Object dto) {
        return modelMapper.map(entity, dto.getClass());
    }

    @Override
    public Wallet convertToEntity(Object dto, Wallet entity) {
        return modelMapper.map(dto, entity.getClass());
    }
}
