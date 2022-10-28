package com.example.api_translation.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

@Document(collection = "words")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Word {

    @MongoId
    private String id;

    private String sourceLang;

    private String targetLang;

    private String word;

    private Object content;

    private Date createdAt = new Date();

}
