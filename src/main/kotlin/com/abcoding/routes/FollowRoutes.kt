package com.abcoding.routes

import com.abcoding.data.repository.follow.FollowRepository
import com.abcoding.data.requests.FollowUpdateRequest
import com.abcoding.data.responses.BasicApiResponse
import com.abcoding.service.FollowService
import com.abcoding.util.ApiResponseMessages.USER_NOT_FOUND
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.followUser(followService: FollowService) {
    authenticate {
        post("/api/following/follow") {
            val request = call.receiveNullable<FollowUpdateRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val didUserExist = followService.followUserIfExists(request, call.userId)
            if(didUserExist) {
                call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse(
                                successful = true
                        )
                )
            } else {
                call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse(
                                successful = false,
                                message = USER_NOT_FOUND
                        )
                )
            }
        }
    }

}

fun Route.unfollowUser(followService: FollowService) {
    delete("/api/following/unfollow") {
        val request = call.receiveNullable<FollowUpdateRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@delete
        }
        val didUserExist = followService.unfollowUserIfExists(request, call.userId)
        if(didUserExist) {
            call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse(
                            successful = true
                    )
            )
        } else {
            call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse(
                            successful = false,
                            message = USER_NOT_FOUND
                    )
            )
        }
    }
}