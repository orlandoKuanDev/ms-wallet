package com.example.mswallet.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "wallet")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Wallet {
    @Id
    private String id;

    @Field(name = "customer")
    private Customer customer;

    @Field(name = "imei")
    private String imei;

    @Indexed(unique = true)
    @Field(name = "verificationCode")
    private String verificationCode;

    @Field(name = "verificationPhoto")
    private Boolean verificationPhoto;

    @Field(name = "acquisition")
    private Acquisition acquisition;

}
