package com.abcoding.service

import com.abcoding.data.models.Post
import com.abcoding.data.repository.post.PostRepository
import com.abcoding.data.requests.CreateAccountRequest
import com.abcoding.data.requests.CreatePostRequest

class PostService(
    private val repository: PostRepository
) {

    suspend fun createPostIfUserExists(request: CreatePostRequest): Boolean {
        return repository.createPostIfUserExists(
            Post(
                imageUrl = "",
                userId = request.userId,
                timestamp = System.currentTimeMillis(),
                description = request.description
            )
        )
    }
}