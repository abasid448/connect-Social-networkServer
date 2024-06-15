package com.abcoding.routes

import com.abcoding.data.requests.CreateCommentRequest
import com.abcoding.data.requests.DeleteCommentRequest
import com.abcoding.data.responses.BasicApiResponse
import com.abcoding.service.CommentService
import com.abcoding.service.LikeService
import com.abcoding.service.UserService
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
        userService: UserService
) {
    authenticate {
        post {
            val request = call.receiveNullable<CreateCommentRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            ifEmailBelongsToUser(
                    userId = request.userId,
                    validateEmail = userService::doesEmailBelongToUserId
            ) {
                when (commentService.createComment(request)) {
                    is CommentService.ValidationEvent.ErrorFieldEmpty -> {
                        call.respond(
                                HttpStatusCode.OK,
                                BasicApiResponse(
                                        successful = false,
                                        message = ApiResponseMessages.FIELDS_BLANK
                                )
                        )
                    }

                    is CommentService.ValidationEvent.ErrorCommentTooLong -> {
                        call.respond(
                                HttpStatusCode.OK,
                                BasicApiResponse(
                                        successful = false,
                                        message = ApiResponseMessages.COMMENT_TOO_LONG
                                )
                        )
                    }

                    is CommentService.ValidationEvent.Success -> {
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
    }

}

fun Route.getCommentsForPost(
        commentService: CommentService
) {

    authenticate {
        get("/api/comment/get") {
            val postId = call.parameters[QueryParams.PARAM_POST_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val comments = commentService.getCommentsForPost(postId)
            call.respond(HttpStatusCode.OK, comments)
        }
    }

}

fun Route.deleteComment(
    commentService: CommentService,
    userService: UserService,
    likeService: LikeService
){
authenticate {
    delete("/api/comment/delete"){
        val request = call.receiveNullable<DeleteCommentRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@delete
        }
        ifEmailBelongsToUser(
                userId = request.userId,
                validateEmail = userService::doesEmailBelongToUserId
        ){
            val deleted = commentService.deleteComment(request.commentId)
            if (deleted){
                 likeService.deleteLikesForParent(request.commentId)
                call.respond(HttpStatusCode.OK,BasicApiResponse(successful = true))
            }else{
                call.respond(HttpStatusCode.NotFound,BasicApiResponse(successful = false))
            }
        }
    }
}
}