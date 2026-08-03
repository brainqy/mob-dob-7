package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyGoalDao {
    @Query("SELECT * FROM daily_goals WHERE date = :date ORDER BY isCompleted ASC, id DESC")
    fun getGoalsForDate(date: String): Flow<List<DailyGoalEntity>>

    @Query("SELECT COUNT(*) FROM daily_goals WHERE date = :date")
    suspend fun getGoalCountForDate(date: String): Int

    @Query("SELECT * FROM daily_goals ORDER BY date DESC, id DESC")
    fun getAllGoals(): Flow<List<DailyGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: DailyGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<DailyGoalEntity>)

    @Update
    suspend fun updateGoal(goal: DailyGoalEntity)

    @Query("DELETE FROM daily_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Int)

    @Query("DELETE FROM daily_goals WHERE date = :date")
    suspend fun deleteGoalsForDate(date: String)
}
