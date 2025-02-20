package org.team14.webty.reviewComment.dto

import org.team14.webty.user.dto.UserDataResponse
import java.time.LocalDateTime

data class CommentResponse(
    val user: UserDataResponse,
    val commentId: Long,
    val content: String,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime?,
    val parentId: Long?,
    val mentions: List<String>,
    var childComments: List<CommentResponse> = emptyList()
)