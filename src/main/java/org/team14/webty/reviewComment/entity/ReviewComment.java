package org.team14.webty.reviewComment.entity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.team14.webty.review.entity.Review;
import org.team14.webty.user.entity.WebtyUser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review_comment", indexes = {
    @Index(name = "idx_review_comment", columnList = "review_id, depth, comment_id DESC"),
    @Index(name = "idx_parent_comment", columnList = "parent_id, comment_id ASC")
})
public class ReviewComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private WebtyUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    public Review review;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    // Adjacency List 방식으로 변경
    @Column(name = "parent_id")
    public Long parentId;  // 부모 댓글의 ID를 직접 저장

    @Column(name = "depth")
    private Integer depth;  // 댓글의 깊이 (0: 루트 댓글, 1: 대댓글, 2: 대대댓글...)

    @Convert(converter = ListToJsonConverter.class)
    private List<String> mentions = new ArrayList<>();

    @Converter
    public static class ListToJsonConverter implements AttributeConverter<List<String>, String> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(List<String> attribute) {
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (IOException e) {
                throw new RuntimeException("Failed to convert list to JSON", e);
            }
        }

        @Override
        public List<String> convertToEntityAttribute(String dbData) {
            try {
                return objectMapper.readValue(dbData, new TypeReference<>() {
                });
            } catch (IOException e) {
                throw new RuntimeException("Failed to convert JSON to list", e);
            }
        }
    }


    @Builder
    public ReviewComment(WebtyUser user, Review review, String content, Long parentId, List<String> mentions) {
        this.user = user;
        this.review = review;
        this.content = content;
        this.parentId = parentId;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
        this.mentions = mentions;
        
        // depth 설정 로직 수정
        this.depth = (parentId == null) ? 0 : 1;  // 임시로 단순화
    }

    public void updateComment(String comment) {
        this.content = comment;
        this.modifiedAt = LocalDateTime.now();
    }
}
