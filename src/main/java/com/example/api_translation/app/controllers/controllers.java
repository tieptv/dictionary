package com.example.api_translation.app.controllers;

import com.example.api_translation.app.dtos.WordDTO;
import com.example.api_translation.app.response.WordResponse;
import com.example.api_translation.domain.services.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("api")
public class controllers {
    @Autowired
    private WordService wordService;

    @PostMapping
    public Mono<WordResponse> getWord(@Valid @RequestBody WordDTO dto) {
        return wordService.getWord(dto);
    }

    @GetMapping
    public Flux<WordResponse> getAllWord() {
        return wordService.findAll();
    }
}
