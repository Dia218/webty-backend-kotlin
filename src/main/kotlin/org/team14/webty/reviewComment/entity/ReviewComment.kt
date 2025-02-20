package org.team14.webty.reviewComment.entity

import org.team14.webty.common.entity.BaseEntity
import org.team14.webty.review.entity.Review
import org.team14.webty.user.entity.WebtyUser
import jakarta.persistence.*
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDateTime

@Entity
@Table(
    name = "review_comment",
    indexes = [
        Index(name = "idx_review_comment", columnList = "review_id, depth, comment_id DESC"),
        Index(name = "idx_parent_comment", columnList = "parent_id, comment_id ASC")
    ]
)
class ReviewComment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    val commentId: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: WebtyUser,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    val review: Review,

    @Column(name = "content", nullable = false)
    var content: String,

    @Column(name = "parent_id")
    val parentId: Long? = null,

    @Column(name = "depth")
    val depth: Int = 0,

    @Convert(converter = ListToJsonConverter::class)
    var mentions: List<String> = emptyList()
) : BaseEntity() {

    fun updateComment(content: String, mentions: List<String>) {
        this.content = content
        this.mentions = mentions
        super.updateModifiedAt()
    }

    @Converter
    class ListToJsonConverter : AttributeConverter<List<String>, String> {
        private val objectMapper = ObjectMapper()

        override fun convertToDatabaseColumn(attribute: List<String>?): String =
            objectMapper.writeValueAsString(attribute ?: emptyList<String>())

        override fun convertToEntityAttribute(dbData: String?): List<String> =
            if (dbData.isNullOrBlank()) emptyList()
            else objectMapper.readValue(dbData, object : TypeReference<List<String>>() {})
    }
}