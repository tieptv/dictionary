package com.example.api_translation.app.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargeCoinDTO {

    @JsonProperty("address_wallet_customer")
    private String publicKey;

    private Integer amount;


}
