package com.example.mswallet.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CreateTransferenceDTO {
    private String cardNumber;
    private Double amount;
    private String description;
    private String accountNumber;
}