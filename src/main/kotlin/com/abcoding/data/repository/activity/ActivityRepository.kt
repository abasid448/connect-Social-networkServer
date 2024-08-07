package com.abcoding.data.repository.activity

import com.abcoding.data.models.Activity
import com.abcoding.data.responses.ActivityResponse
import com.abcoding.util.Constants

interface ActivityRepository {

    suspend fun getActivitiesForUser(
            userId: String,
            page: Int = 0,
            pageSize: Int = Constants.DEFAULT_ACTIVITY_PAGE_SIZE
    ): List<ActivityResponse>

    suspend fun createActivity(activity: Activity)

    suspend fun deleteActivity(activityId: String): Boolean

}
