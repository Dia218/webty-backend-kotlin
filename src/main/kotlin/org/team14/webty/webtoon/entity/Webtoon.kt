package org.team14.webty.webtoon.entity

import jakarta.persistence.*
import lombok.NoArgsConstructor
import org.team14.webty.common.entity.BaseEntity
import org.team14.webty.webtoon.enumerate.Platform


@Entity
class Webtoon(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "webtoon_id")
    var webtoonId: Long? = null,  // PK는 var 유지 (JPA가 자동 생성)

    val webtoonName: String,

    @Enumerated(EnumType.STRING)
    val platform: Platform,

    val webtoonLink: String,

    val thumbnailUrl: String,

    val authors: String,

    val finished: Boolean

) {

    override fun equals(other: Any?): Boolean {
        // 같은 객체면 true 반환
        if (this === other) return true

        // 다른 클래스 타입이거나 null인 경우
        if (other == null || javaClass != other.javaClass) return false

        // other를 Webtoon으로 캐스팅
        other as Webtoon

        // 식별자(webtoonId)가 null이 아니면 비교하고, 같으면 true 반환
        return webtoonId != null && webtoonId == other.webtoonId
    }

    override fun hashCode(): Int {
        // webtoonId가 null일 경우 0을 반환, 그렇지 않으면 hashCode 값을 반환
        return webtoonId?.hashCode() ?: 0
    }
}
