package org.team14.webty.reviewComment.mapper

import org.springframework.stereotype.Component
import org.team14.webty.review.entity.Review
import org.team14.webty.reviewComment.dto.CommentRequest
import org.team14.webty.reviewComment.dto.CommentResponse
import org.team14.webty.reviewComment.entity.ReviewComment
import org.team14.webty.user.dto.UserDataResponse
import org.team14.webty.user.entity.WebtyUser

@Component
object ReviewCommentMapper {
    fun toEntity(request: CommentRequest, user: WebtyUser, review: Review) = ReviewComment(
        user = user,
        review = review,
        content = request.content,
        parentId = request.parentCommentId,
        depth = if (request.parentCommentId != null) 1 else 0,
        mentions = request.mentions
    )

    fun toResponse(comment: ReviewComment?) = comment?.let {
        CommentResponse(
            user = UserDataResponse(it.user),
            commentId = it.commentId,
            content = it.content,
            createdAt = it.createdAt,
            modifiedAt = it.modifiedAt,
            parentId = it.parentId,
            mentions = it.mentions,
            childComments = emptyList()
        )
    } ?: throw IllegalArgumentException("Comment cannot be null")
}