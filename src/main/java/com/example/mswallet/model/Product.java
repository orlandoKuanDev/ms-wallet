package com.example.mswallet.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
    @Field(name = "productName")
    private String productName;

    @Field(name = "productType")
    private String productType;

    @Field(name = "rules")
    private Rules rules;
}

