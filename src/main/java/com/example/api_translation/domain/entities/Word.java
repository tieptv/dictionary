package com.example.api_translation.domain.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

@Document(collection = "words")
@Data
@Builder
public class Word {

    @MongoId
    private String id;

    private String word;

    private String content;

    @JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss")
    private Date createdAt;

}
