package com.example.api_translation.domain.services.impl;

import com.example.api_translation.app.response.WordResponse;
import com.example.api_translation.domain.entities.Word;
import com.example.api_translation.domain.repositories.WordRepository;
import com.example.api_translation.domain.services.WordService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WordServiceImpl implements WordService {

    @Autowired
    private WordRepository wordRepository;

    @Override
    public WordResponse getWord(String word) {
        ModelMapper modelMapper = new ModelMapper();
        Word w = wordRepository.findByWord(word);
//        if (w == null) {
//
//        }

        return modelMapper.map(w, WordResponse.class);
    }

    private String getContent(String word) {
        RestTemplate restTemplate = new RestTemplate();
        return null;
    }
}
