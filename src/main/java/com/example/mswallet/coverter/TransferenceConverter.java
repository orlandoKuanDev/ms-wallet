package com.example.mswallet.coverter;

import com.example.mswallet.model.Transference;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

public class TransferenceConverter implements BaseConverter<Transference, Object>{
    private final ModelMapper modelMapper;

    @Autowired
    public TransferenceConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    @Override
    public List<Object> convertToDto(List<Transference> entity, Object dto) {
        return null;
    }

    @Override
    public Page<Object> convertToDto(Page<Transference> entity, Object dto) {
        return null;
    }

    @Override
    public Object convertToDto(Transference entity, Object dto) {
        return modelMapper.map(entity, dto.getClass());
    }

    @Override
    public Transference convertToEntity(Object dto, Transference entity) {
        return modelMapper.map(dto, entity.getClass());
    }
}
