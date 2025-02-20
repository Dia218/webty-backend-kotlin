import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class WebtoonSearchRequest(
    @field:Size(max = 100)
    val webtoonName: String? = null,
    
    @field:Pattern(regexp = "^(NAVER|KAKAO_PAGE)?$", message = "Platform must be either NAVER or KAKAO_PAGE")
    val platform: String?,
    
    @field:Size(max = 100)
    val authors: String?,
    
    val finished: Boolean?,
    
    @field:Min(0)
    val page: Int = 0,
    
    @field:Min(1)
    @field:Max(100)
    val size: Int = 10,
    
    @field:Pattern(
        regexp = "^(WEBTOON_NAME|AUTHORS|PLATFORM|FINISHED)$",
        message = "Sort by must be one of: WEBTOON_NAME, PLATFORM, AUTHORS, FINISHED"
    )
    val sortBy: String = "WEBTOON_NAME",
    
    @field:Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be either asc or desc")
    val sortDirection: String = "asc"
)

