package com.example.mswallet.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "wallet")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {

    @Id
    private String id;

    @Field(name = "name")
    private String name;

    @Field(name = "identityType")
    private String identityType;

    @Field(name = "identityNumber")
    private String identityNumber;

    @Field(name = "phoneNumber")
    private String phoneNumber;

    @Field(name = "cardNumber")
    private String cardNumber;

    @Field(name = "imei")
    private String imei;

    @Field(name = "email")
    private String email;

}
