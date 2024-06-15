package com.abcoding.routes

import com.abcoding.data.requests.CreatePostRequest
import com.abcoding.data.requests.DeletePostRequest
import com.abcoding.data.responses.BasicApiResponse
import com.abcoding.service.LikeService
import com.abcoding.service.PostService
import com.abcoding.service.UserService
import com.abcoding.util.ApiResponseMessages
import com.abcoding.util.Constants
import com.abcoding.util.QueryParams
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.createPost(
    postService: PostService,
    userService: UserService
) {
    authenticate {
        post("/api/post/create") {
            val request = call.receiveNullable<CreatePostRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val email = call.principal<JWTPrincipal>()?.getClaim("email", String::class)
            val isEmailByUser = userService.doesEmailBelongToUserId(
                email = email ?: "",
                userId = request.userId
            )
            if (!isEmailByUser) {
                call.respond(HttpStatusCode.Unauthorized, "You are not who you say you are. ")
                return@post
            }

            val didUserExists = postService.createPostIfUserExists(request)
            if (!didUserExists) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse(
                        successful = false,
                        message = ApiResponseMessages.USER_NOT_FOUND
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse(
                        successful = true
                    )
                )
            }
        }
    }
}
    fun Route.getPostsForFollows(
        postService: PostService,
        userService: UserService
    ) {
        authenticate {
            get("/api/post/get") {
                val userId = call.parameters[QueryParams.PARAM_USER_ID] ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                val page = call.parameters[QueryParams.PARAM_PAGE]?.toIntOrNull() ?: 0
                val pageSize =
                    call.parameters[QueryParams.PARAM_PAGE_SIZE]?.toIntOrNull() ?: Constants.DEFAULT_PAGE_SIZE

                ifEmailBelongsToUser(
                    userId = userId,
                    validateEmail = userService::doesEmailBelongToUserId
                ) {
                    val posts = postService.getPostsForFollows(userId, page, pageSize)
                    call.respond(
                        HttpStatusCode.OK,
                        posts
                    )
                }
            }
        }
    }
fun Route.deletePost(
        postService: PostService,
        userService: UserService,
        likeService: LikeService
) {
    delete("/api/post/delete") {
        val request = call.receiveNullable<DeletePostRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@delete
        }
        val post = postService.getPost(request.postId)
        if(post == null) {
            call.respond(HttpStatusCode.NotFound)
            return@delete
        }
        ifEmailBelongsToUser(
                userId = post.userId,
                validateEmail = userService::doesEmailBelongToUserId
        ) {
            postService.deletePost(request.postId)
            likeService.deleteLikesForParent(request.postId)
            // TODO: Delete comments from post
            call.respond(HttpStatusCode.OK)
        }
    }
}
