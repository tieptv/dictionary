package com.example.api_translation.domain.services;

import com.example.api_translation.app.dtos.WordDTO;
import com.example.api_translation.app.response.WordResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WordService {
    Mono<WordResponse> getWord(WordDTO dto);

    Flux<WordResponse> findAll();
}
