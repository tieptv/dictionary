package com.example.api_translation.app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferCoinDTO {

    private List<CoinWallet> coin_wallets;

    private String note = "Hello";

    private String customer_wallet;

    public TransferCoinDTO(String coinId, String customer_wallet) {
        this.coin_wallets = Arrays.asList(new CoinWallet(coinId, 1));
        this.customer_wallet = customer_wallet;
    }
}


