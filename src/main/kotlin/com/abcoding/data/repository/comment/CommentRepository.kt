package com.abcoding.data.repository.comment

import com.abcoding.data.models.Comment
import com.abcoding.data.responses.CommentResponse

interface CommentRepository {

    suspend fun createComment(comment: Comment): String

    suspend fun deleteComment(commentId: String): Boolean

    suspend fun deleteCommentsFromPost(postId: String): Boolean

    suspend fun getCommentsForPost(postId: String, ownUserId: String): List<CommentResponse>
    suspend fun getComment(commentId: String): Comment?
}