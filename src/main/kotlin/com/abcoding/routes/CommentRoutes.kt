package com.abcoding.routes

import com.abcoding.data.requests.CreateCommentRequest
import com.abcoding.data.requests.DeleteCommentRequest
import com.abcoding.data.responses.BasicApiResponse
import com.abcoding.service.ActivityService
import com.abcoding.service.CommentService
import com.abcoding.service.LikeService
import com.abcoding.util.ApiResponseMessages
import com.abcoding.util.QueryParams
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.createComment(
        commentService: CommentService,
        activityService: ActivityService
) {
    authenticate {
        post("/api/comment/create") {
            val request = call.receiveNullable<CreateCommentRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val userId = call.userId
            when (commentService.createComment(request, userId)) {
                is CommentService.ValidationEvent.ErrorFieldEmpty -> {
                    call.respond(
                            HttpStatusCode.OK,
                            BasicApiResponse<Unit>(
                                    successful = false,
                                    message = ApiResponseMessages.FIELDS_BLANK
                            )
                    )
                }
                is CommentService.ValidationEvent.ErrorCommentTooLong -> {
                    call.respond(
                            HttpStatusCode.OK,
                            BasicApiResponse<Unit>(
                                    successful = false,
                                    message = ApiResponseMessages.COMMENT_TOO_LONG
                            )
                    )
                }
                is CommentService.ValidationEvent.Success -> {
                    activityService.addCommentActivity(
                            byUserId = userId,
                            postId = request.postId,
                    )
                    call.respond(
                            HttpStatusCode.OK,
                            BasicApiResponse<Unit>(
                                    successful = true,
                            )
                    )
                }
                is CommentService.ValidationEvent.UserNotFound -> {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse<Unit>(
                            successful = false,
                            message = "User not found"
                        )
                    )
                }
            }
        }
    }
}

fun Route.getCommentsForPost(
        commentService: CommentService,
) {
    get("/api/comment/get") {
        val postId = call.parameters[QueryParams.PARAM_POST_ID] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
            }
        val comments = commentService.getCommentsForPost(postId, call.userId)
        call.respond(HttpStatusCode.OK, comments)
    }
}

fun Route.deleteComment(
        commentService: CommentService,
        likeService: LikeService
) {
    authenticate {
        delete("/api/comment/delete") {
            val request = call.receiveNullable<DeleteCommentRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            val comment = commentService.getCommentById(request.commentId)
            if (comment?.userId != call.userId) {
                call.respond(HttpStatusCode.Unauthorized)
                return@delete
            }
            val deleted = commentService.deleteComment(request.commentId)
            if (deleted) {
                likeService.deleteLikesForParent(request.commentId)
                call.respond(HttpStatusCode.OK, BasicApiResponse<Unit>(successful = true))
            } else {
                call.respond(HttpStatusCode.NotFound, BasicApiResponse<Unit>(successful = false))
            }
        }
    }
}