package com.example.mswallet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "transference")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transference {
    @Id
    private String id;

    @Field(name = "origenWallet")
    private Wallet origenWallet;

    @Field(name = "destineWallet")
    private Wallet destineWallet;

    @Field(name = "amount")
    private Double amount;
}
