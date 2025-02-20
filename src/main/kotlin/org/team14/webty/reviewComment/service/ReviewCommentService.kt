package org.team14.webty.reviewComment.service

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.team14.webty.common.exception.BusinessException
import org.team14.webty.common.exception.ErrorCode
import org.team14.webty.review.entity.Review
import org.team14.webty.review.repository.ReviewRepository
import org.team14.webty.reviewComment.dto.CommentRequest
import org.team14.webty.reviewComment.dto.CommentResponse
import org.team14.webty.reviewComment.mapper.ReviewCommentMapper
import org.team14.webty.reviewComment.repository.ReviewCommentRepository
import org.team14.webty.security.authentication.WebtyUserDetails
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ReviewCommentService(
    private val reviewRepository: ReviewRepository,
    private val commentRepository: ReviewCommentRepository,
    private val commentMapper: ReviewCommentMapper
) {
    fun existsReviewById(reviewId: Long): Boolean = 
        reviewRepository.existsById(reviewId)

    fun getReferenceById(reviewId: Long): Review = 
        reviewRepository.getReferenceById(reviewId)

    @Transactional
    @Cacheable(value = ["comments"], key = "#reviewId")
    fun createComment(
        userDetails: WebtyUserDetails,
        reviewId: Long,
        request: CommentRequest
    ): CommentResponse {
        if (!existsReviewById(reviewId)) {
            throw BusinessException(ErrorCode.REVIEW_NOT_FOUND)
        }

        request.parentCommentId?.let { parentId ->
            commentRepository.findById(parentId).orElseThrow {
                BusinessException(ErrorCode.COMMENT_NOT_FOUND)
            }.takeIf { it.depth >= 2 }?.let {
                throw BusinessException(ErrorCode.COMMENT_WRITING_RESTRICTED)
            }
        }

        val review = getReferenceById(reviewId)
        val comment = commentMapper.toEntity(request, userDetails.webtyUser, review)
        return commentMapper.toResponse(commentRepository.save(comment))
    }

    @Transactional
    @CachePut(value = ["comments"], key = "#reviewId")
    fun updateComment(
        commentId: Long,
        userDetails: WebtyUserDetails,
        request: CommentRequest
    ): CommentResponse {
        val comment = commentRepository.findById(commentId).orElseThrow {
            BusinessException(ErrorCode.COMMENT_NOT_FOUND)
        }

        if (comment.user.userId != userDetails.webtyUser.userId) {
            throw BusinessException(ErrorCode.COMMENT_PERMISSION_DENIED)
        }

        comment.updateComment(request.content, request.mentions)
        return commentMapper.toResponse(comment)
    }

    @Transactional
    @CacheEvict(value = ["comments"], allEntries = true)
    fun deleteComment(commentId: Long, userDetails: WebtyUserDetails) {
        val comment = commentRepository.findById(commentId).orElseThrow {
            BusinessException(ErrorCode.COMMENT_NOT_FOUND)
        }

        if (comment.user.userId != userDetails.webtyUser.userId) {
            throw BusinessException(ErrorCode.COMMENT_PERMISSION_DENIED)
        }

        commentRepository.findByParentIdOrderByCommentIdAsc(commentId).let { childComments ->
            if (childComments.isNotEmpty()) {
                commentRepository.deleteAll(childComments)
            }
        }
        commentRepository.delete(comment)
    }

    @Cacheable(value = ["comments"], key = "#reviewId + '_' + #page + '_' + #size")
    fun getCommentsByReviewId(reviewId: Long, page: Int, size: Int): Page<CommentResponse> {
        if (!existsReviewById(reviewId)) {
            throw BusinessException(ErrorCode.REVIEW_NOT_FOUND)
        }

        val pageable = PageRequest.of(page, size)
        val commentPage = commentRepository.findAllByReviewIdOrderByDepthAndCommentId(reviewId, pageable)

        val commentMap = mutableMapOf<Long, MutableList<CommentResponse>>()
        val rootComments = commentPage.content.filter { comment ->
            if (comment.parentId != null) {
                commentMap.getOrPut(comment.parentId) { mutableListOf() }
                    .add(commentMapper.toResponse(comment))
                false
            } else {
                true
            }
        }.map { commentMapper.toResponse(it) }
            .onEach { root ->
                root.childComments = commentMap[root.commentId] ?: emptyList()
            }

        return PageImpl(rootComments, pageable, commentPage.totalElements)
    }
}