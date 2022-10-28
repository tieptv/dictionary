package com.example.api_translation.app.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class WordDTO {

    @NotNull(message = "source lang not null")
    private String sourceLang;

    @NotNull(message = "source lang not null")
    private String targetLang;

    @NotNull(message = "word not null")
    private String word;
}
