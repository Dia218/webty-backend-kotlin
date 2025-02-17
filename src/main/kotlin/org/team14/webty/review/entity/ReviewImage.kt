package org.team14.webty.review.entity

import jakarta.persistence.*

@Entity
class ReviewImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_image_id")
    val reviewImageId: Long? = null,

    val imageUrl: String,

    @ManyToOne
    @JoinColumn(name = "review_id")
    val review: Review
)
