package com.example.api_translation.domain.services.impl;

import com.example.api_translation.app.response.WordResponse;
import com.example.api_translation.domain.entities.Word;
import com.example.api_translation.domain.exceptions.BusinessException;
import com.example.api_translation.domain.exceptions.ErrorMessage;
import com.example.api_translation.domain.repositories.WordRepository;
import com.example.api_translation.domain.services.WordService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WordServiceImpl implements WordService {

    @Autowired
    private WordRepository wordRepository;

    @Value("${oxford.application_id}")
    private String applicationId;

    @Value("${oxford.application_key}")
    private String applicationKey;

    @Value("${oxford.translation_api}")
    private String apiTranslation;

    @Override
    public WordResponse getWord(String sourceLang, String targetLang, String word) {
        ModelMapper modelMapper = new ModelMapper();
        Word w = wordRepository.findBySourceLangAndTargetLangAndWord(sourceLang, targetLang,word);
        if (w == null) {
            String content = getContent(sourceLang, targetLang, word);
            w = Word.builder().word(word).sourceLang(sourceLang)
                    .targetLang(targetLang).content(content).build();
            w = wordRepository.save(w);
        }

        return modelMapper.map(w, WordResponse.class);
    }

    private String getContent(String sourceLang, String targetLang, String word) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("app_id", applicationId);
        headers.set("app_key", applicationKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = apiTranslation + sourceLang + "/" + targetLang + "/" + word;
        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        } catch(Exception e) {
            e.printStackTrace();
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.WORD_NOT_FOUND);
        }

    }
}
