package com.example.api_translation.domain.data;

import lombok.Data;

import java.util.Map;

@Data
public class ShopCustomerToken {

    private Map<String, String> shopTokens;

    private Map<String, Customer> customerTokens;

}
