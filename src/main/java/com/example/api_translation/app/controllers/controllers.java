package com.example.api_translation.app.controllers;

import com.example.api_translation.app.response.WordResponse;
import com.example.api_translation.domain.services.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class controllers {
    @Autowired
    private WordService wordService;

    public WordResponse getWord(@RequestParam("word") String word) {
        return wordService.getWord(word);
    }
}
