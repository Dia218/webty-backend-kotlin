// import org.team14.webty.recommend.enumerate.LikeType;
// import org.team14.webty.review.entity.Review;
//
// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
// import jakarta.persistence.FetchType;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
// import lombok.AccessLevel;
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
//
// @Entity
// @Getter
// @NoArgsConstructor(access = AccessLevel.PROTECTED)
// @AllArgsConstructor
// @Builder
// public class Recommend {
// 	@Id
// 	@GeneratedValue(strategy = GenerationType.IDENTITY)
// 	@Column(name = "vote_id")
// 	private Long voteId;
//
// 	@Enumerated(EnumType.STRING)
// 	private LikeType likeType;
//
// 	@ManyToOne(fetch = FetchType.LAZY)
// 	@JoinColumn(name = "review_id")
// 	private Review review;
//
// 	@Column(name = "user_id")
// 	private Long userId;
// }