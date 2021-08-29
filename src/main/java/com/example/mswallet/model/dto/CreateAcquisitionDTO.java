package com.example.mswallet.model.dto;

import com.example.mswallet.model.Customer;
import com.example.mswallet.model.Product;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAcquisitionDTO {
    private Product product;
    private Customer customerHolder;
    private Double initial;
}
/*
 "product": {
         "productName": "CREDITO PERSONAL"
         },
         "customerHolder": [
         {
         "customerIdentityNumber" : "70055041"
         }
         ],
         "initial" : 4000*/
