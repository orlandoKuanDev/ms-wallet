package com.example.mswallet.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateWalletDTO {
    private String customerIdentityType;
    @Indexed(unique = true)
    private String customerIdentityNumber;
    private String name;
    private String email;
    @Indexed(unique = true)
    private String phone;
    private String address;
}
