package com.example.api_translation.app.controllers;

import com.example.api_translation.app.response.WordResponse;
import com.example.api_translation.domain.services.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("api")
public class controllers {
    @Autowired
    private WordService wordService;

    @GetMapping
    public WordResponse getWord(@RequestParam("word") String word, @RequestParam("source") String sourceLang,
    @RequestParam("target") String targetLang) {
        return wordService.getWord(sourceLang, targetLang,  word);
    }

    @GetMapping("/proxy")
    public String check() throws ExecutionException, InterruptedException {
        return wordService.checkProxy();
    }
}
