package com.example.api_translation.domain.repositories;

import com.example.api_translation.domain.entities.Word;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends MongoRepository<Word, String> {
    Word findBySourceLangAndTargetLangAndWord(String source, String target, String word);
}
