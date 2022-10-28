package com.example.api_translation.app.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WordDTO {

    @NotBlank(message = "source lang not empty")
    @JsonProperty("source_lang")
    private String sourceLang;

    @NotBlank(message = "source lang not empty")
    @JsonProperty("target_lang")
    private String targetLang;

    @NotBlank(message = "word not empty")
    @JsonProperty("word")
    private String word;
}
