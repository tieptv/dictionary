package com.example.api_translation.domain.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Customer implements Serializable {
    @JsonProperty("token")
    private String token;

    @JsonProperty("publicKey")
    private String publicKey;

    @JsonProperty("coin_id")
    private String coinId;
}
