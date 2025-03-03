package org.team14.webty.userActivity.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.team14.webty.userActivity.dto.UserActivityRequest
import org.team14.webty.userActivity.service.UserActivityService


@RestController
@RequestMapping("/activity")
class UserActivityController(
    private val userActivityService: UserActivityService
) {
    @PostMapping("/logs")
    fun logActivity(@RequestBody request: UserActivityRequest): ResponseEntity<String> {
        userActivityService.saveActivity(request)
        return ResponseEntity.ok("Activity logged successfully")
    }

    //유저가 최근에 본 웹툰들 id 리스트 반환
    @GetMapping("/webtoon/recent/{id}")
    fun recentWebtoon( @PathVariable id:String ): ResponseEntity<List<String>> {
        val response = userActivityService.getRecentWebtoons(id,3)

        return ResponseEntity.ok(response)
    }

    //유저와 유사한 유저들 id 리스트 반환
    @GetMapping("/user/{id}")
    fun similarUsers( @PathVariable id:String ): ResponseEntity<List<String>> {
        val webtoonIds=userActivityService.getRecentWebtoons(id,3)
        val response = userActivityService.getSimilarUsers(webtoonIds)

        return ResponseEntity.ok(response)
    }

    //유사한 유저들이 많이 본 웹툰 id 리스트 반환
    @GetMapping("/webtoon/recommend/{id}")
    fun recommendWebtoons( @PathVariable id:String ): ResponseEntity<List<String>> {
        val webtoonIds=userActivityService.getRecentWebtoons(id,3)
        val userIds = userActivityService.getSimilarUsers(webtoonIds)
        val response = userActivityService.getRecommendWebtoons(userIds)

        return ResponseEntity.ok(response)
    }


}