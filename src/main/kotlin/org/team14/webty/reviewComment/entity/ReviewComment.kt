// 리뷰 댓글 엔티티 관련 기능을 처리하는 패키지 선언
package org.team14.webty.reviewComment.entity

// 필요한 의존성들을 임포트
import org.team14.webty.common.entity.BaseEntity
import org.team14.webty.review.entity.Review
import org.team14.webty.user.entity.WebtyUser
import jakarta.persistence.*
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDateTime

// JPA 엔티티임을 나타내는 어노테이션
@Entity
// 테이블 이름과 인덱스 정의
@Table(
    name = "review_comment",
    indexes = [
        // 리뷰 ID, depth, 댓글 ID(내림차순) 기준 인덱스
        Index(name = "idx_review_comment", columnList = "review_id, depth, comment_id DESC"),
        // 부모 댓글 ID, 댓글 ID(오름차순) 기준 인덱스
        Index(name = "idx_parent_comment", columnList = "parent_id, comment_id ASC")
    ]
)
// ReviewComment 클래스 정의, BaseEntity 상속
class ReviewComment(
    // 기본 키 정의
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    val commentId: Long = 0,

    // 댓글 작성자와의 다대일 관계 정의
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: WebtyUser,

    // 댓글이 속한 리뷰와의 다대일 관계 정의
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    val review: Review,

    // 댓글 내용
    @Column(name = "content", nullable = false)
    var content: String,

    // 부모 댓글 ID (대댓글인 경우)
    @Column(name = "parent_id")
    val parentId: Long? = null,

    // 댓글의 깊이 (0: 루트 댓글, 1: 대댓글)
    @Column(name = "depth")
    val depth: Int = 0,

    // 멘션된 사용자 목록 (JSON으로 저장)
    @Convert(converter = ListToJsonConverter::class)
    var mentions: List<String> = emptyList()
) : BaseEntity() {  // 생성일시, 수정일시를 제공하는 BaseEntity 상속

    @PrePersist
    fun prePersist() {
        // 엔티티가 저장되기 직전에 modifiedAt을 null로 설정
        this.modifiedAt = null
    }

    // 댓글 내용과 멘션 목록을 업데이트하는 메서드
    fun updateComment(content: String, mentions: List<String>) {
        // 내용이나 멘션이 실제로 변경되었을 때만 수정일시 업데이트
        if (this.content != content || this.mentions != mentions) {
            this.content = content
            this.mentions = mentions
            // BaseEntity의 수정일시 업데이트
            updateModifiedAt()
        }
    }

    // List<String>을 JSON 문자열로 변환하는 컨버터 클래스
    @Converter
    class ListToJsonConverter : AttributeConverter<List<String>, String> {
        // ObjectMapper 인스턴스 생성
        private val objectMapper = ObjectMapper()

        // 엔티티 속성을 데이터베이스 컬럼으로 변환
        override fun convertToDatabaseColumn(attribute: List<String>?): String =
            // List를 JSON 문자열로 변환
            objectMapper.writeValueAsString(attribute ?: emptyList<String>())

        // 데이터베이스 컬럼을 엔티티 속성으로 변환
        override fun convertToEntityAttribute(dbData: String?): List<String> =
            // JSON 문자열이 비어있으면 빈 리스트 반환
            if (dbData.isNullOrBlank()) emptyList()
            // JSON 문자열을 List<String>으로 변환
            else objectMapper.readValue(dbData, object : TypeReference<List<String>>() {})
    }

    // getter 메서드 추가
    fun getCreatedAt(): LocalDateTime = createdAt
    fun getModifiedAt(): LocalDateTime? = modifiedAt
}