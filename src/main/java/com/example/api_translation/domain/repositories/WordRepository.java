package com.example.api_translation.domain.repositories;

import com.example.api_translation.domain.entities.Word;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface WordRepository extends ReactiveMongoRepository<Word, String> {
    Mono<Word> findBySourceLangAndTargetLangAndWord(String source, String target, String word);
}
