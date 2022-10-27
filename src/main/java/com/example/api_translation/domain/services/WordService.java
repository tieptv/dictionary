package com.example.api_translation.domain.services;

import com.example.api_translation.app.response.WordResponse;

public interface WordService {
    WordResponse getWord(String word);
}
