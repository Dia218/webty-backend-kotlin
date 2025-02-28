// 리뷰 댓글 엔티티 관련 기능을 처리하는 패키지 선언
package org.team14.webty.reviewComment.entity

// 필요한 의존성들을 임포트
import jakarta.persistence.*
import org.team14.webty.common.entity.BaseEntity
import org.team14.webty.review.entity.Review
import org.team14.webty.user.entity.WebtyUser

// JPA 엔티티임을 나타내는 어노테이션
@Entity
// 테이블 이름과 인덱스 설정
@Table(
    name = "review_comment",
    indexes = [
        // 리뷰 ID, 댓글 깊이, 댓글 ID 내림차순으로 인덱스 생성 (계층 구조 조회 최적화)
        Index(name = "idx_review_comment", columnList = "review_id, depth, comment_id DESC"),
        // 부모 댓글 ID, 댓글 ID 오름차순으로 인덱스 생성 (대댓글 조회 최적화)
        Index(name = "idx_parent_comment", columnList = "parent_id, comment_id ASC"),
        // 단순 리뷰 ID로만 검색 (현재 reviewComment를 따로 반환하기 때문에)
        Index(name = "idx_review_comment_simple", columnList = "review_id")
    ]
)
class ReviewComment(
    // 댓글의 고유 식별자 (자동 생성)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    var commentId: Long? = null,

    // 댓글 작성자 정보 (지연 로딩)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: WebtyUser,

    // 댓글이 달린 리뷰 정보 (지연 로딩)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    val review: Review,

    // 댓글 내용 (필수 입력)
    @Column(name = "content", nullable = false)
    var content: String = "",

    // 부모 댓글 ID (대댓글인 경우에만 값이 있음)
    @Column(name = "parent_id")
    var parentId: Long? = null,

    // 댓글의 깊이 (0: 루트 댓글, 1: 대댓글)
    @Column(name = "depth")
    val depth: Int = 0,

    // 멘션된 사용자 목록 (콤마로 구분된 문자열)
    @Column(name = "mentions", columnDefinition = "TEXT")
    var mentions: String = "[]"
) : BaseEntity() {
    // 댓글 내용과 멘션 목록을 업데이트하는 메소드
    fun updateComment(content: String, mentions: List<String>) {
        this.content = content
        this.mentions = mentions.joinToString(",")
    }

    // 멘션 문자열을 리스트로 변환하는 메소드
    fun getMentionsList(): List<String> =
        if (mentions.isBlank()) emptyList()
        else mentions.split(",").filter { it.isNotBlank() }

    // equals 메소드 오버라이드 (ID 기반 비교)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ReviewComment
        return commentId == other.commentId
    }

    // hashCode 메소드 오버라이드 (ID 기반 해시)
    override fun hashCode(): Int = commentId?.hashCode() ?: 0
}