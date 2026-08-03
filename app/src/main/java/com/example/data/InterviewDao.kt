package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewDao {
    @Query("SELECT * FROM interviews WHERE tenantId = :tenantId ORDER BY date ASC")
    fun getInterviewsFlow(tenantId: String): Flow<List<InterviewEntity>>

    @Query("SELECT * FROM interviews WHERE tenantId = :tenantId AND type = :type ORDER BY date ASC")
    fun getInterviewsByTypeFlow(tenantId: String, type: String): Flow<List<InterviewEntity>>

    @Query("SELECT * FROM interviews WHERE tenantId = :tenantId ORDER BY date ASC")
    suspend fun getInterviews(tenantId: String): List<InterviewEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(interview: InterviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(interviews: List<InterviewEntity>)

    @Query("DELETE FROM interviews WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM interviews WHERE tenantId = :tenantId AND type = :type")
    suspend fun deleteByType(tenantId: String, type: String)
}
