package com.example.mswallet.model.dto;

import com.example.mswallet.model.Wallet;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TransferenceDTO {
    private Wallet origenWallet;
    private Wallet destineWallet;
    private Double amount;
}
