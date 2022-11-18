package com.example.api_translation.domain.services;

import com.example.api_translation.app.response.WordResponse;

import java.util.concurrent.ExecutionException;

public interface WordService {
    WordResponse getWord(String sourceLang, String targetLang, String word);

    String charge(int num) throws ExecutionException, InterruptedException;

    String transfer(int num) throws ExecutionException, InterruptedException;
}
