package com.example.api_translation.domain.reponse;

import com.example.api_translation.domain.data.Customer;
import lombok.Data;

import java.util.List;

@Data
public class CustomerResponse {
    private String message;
    private List<Customer> data;
}
