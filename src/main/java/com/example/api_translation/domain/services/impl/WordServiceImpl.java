package com.example.api_translation.domain.services.impl;

import com.example.api_translation.app.dtos.WordDTO;
import com.example.api_translation.app.response.WordResponse;
import com.example.api_translation.domain.entities.Word;
import com.example.api_translation.domain.exceptions.BusinessException;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public Mono<WordResponse> getWord(WordDTO dto) {
        ModelMapper modelMapper = new ModelMapper();
        Mono<Word> w = wordRepository.
                findBySourceLangAndTargetLangAndWord(dto.getSourceLang(), dto.getTargetLang(), dto.getWord())
                .switchIfEmpty(Mono.defer(() -> {
                    Object content = getContent(dto.getSourceLang(), dto.getTargetLang(), dto.getWord());
                    Word word = Word.builder().word(dto.getWord()).sourceLang(dto.getSourceLang())
                            .targetLang(dto.getTargetLang()).content(content).build();

                    return wordRepository.save(word);
                }));
        return w.map(item -> {
            WordResponse wordResponse = modelMapper.map(item, WordResponse.class);
            return wordResponse;
        });
    }

    @Override
    public Flux<WordResponse> findAll() {
        ModelMapper modelMapper = new ModelMapper();
        Flux<Word> allWord = wordRepository.findAll();
        return allWord.map(item  -> {
            WordResponse wordResponse = modelMapper.map(item, WordResponse.class);
            return wordResponse;
        });
    }

    private Object getContent(String sourceLang, String targetLang, String word) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("app_id", applicationId);
        headers.set("app_key", applicationKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = apiTranslation.replace("{sourceLang}", sourceLang)
                .replace("{targetLang}", targetLang).replace("{word}", word);
        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, Object.class).getBody();
        } catch(Exception e) {
            e.printStackTrace();
            throw new BusinessException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

    }
}
