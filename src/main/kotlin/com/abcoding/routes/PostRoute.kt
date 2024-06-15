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
) {
    authenticate {
        post("/api/post/create") {
            val request = call.receiveNullable<CreatePostRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val userId = call.userId

            val didUserExist = postService.createPostIfUserExists(request, userId)
            if (!didUserExist) {
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
                                successful = true,
                        )
                )
            }
        }
    }
}
fun Route.getPostsForFollows(
        postService: PostService,
) {
    authenticate {
        get("/api/post/get") {
            val page = call.parameters[QueryParams.PARAM_PAGE]?.toIntOrNull() ?: 0
            val pageSize = call.parameters[QueryParams.PARAM_PAGE_SIZE]?.toIntOrNull() ?:
            Constants.DEFAULT_POST_PAGE_SIZE

            val posts = postService.getPostsForFollows(call.userId, page, pageSize)
            call.respond(
                    HttpStatusCode.OK,
                    posts
            )
        }
    }
}
fun Route.deletePost(
        postService: PostService,
        likeService: LikeService
) {
    authenticate {
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
            if(post.userId == call.userId) {
                postService.deletePost(request.postId)
                likeService.deleteLikesForParent(request.postId)
                // TODO: Delete comments from post
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}