package org.team14.webty.reviewComment.dto

data class CommentRequest(
    val content: String,
    val mentions: List<String> = emptyList(),
    val parentCommentId: Long? = null
)