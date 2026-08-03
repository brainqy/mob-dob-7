package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyGoalRepository(private val dailyGoalDao: DailyGoalDao) {

    fun getTodayGoals(): Flow<List<DailyGoalEntity>> {
        val today = getTodayDateString()
        return dailyGoalDao.getGoalsForDate(today)
    }

    fun getGoalsForDate(dateStr: String): Flow<List<DailyGoalEntity>> {
        return dailyGoalDao.getGoalsForDate(dateStr)
    }

    suspend fun addGoal(goal: DailyGoalEntity) {
        dailyGoalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: DailyGoalEntity) {
        dailyGoalDao.updateGoal(goal)
    }

    suspend fun toggleGoalCompleted(goal: DailyGoalEntity) {
        val newCompleted = !goal.isCompleted
        val newCount = if (newCompleted) goal.targetCount else 0
        dailyGoalDao.updateGoal(
            goal.copy(
                isCompleted = newCompleted,
                completedCount = newCount
            )
        )
    }

    suspend fun incrementProgress(goal: DailyGoalEntity) {
        val nextCount = (goal.completedCount + 1).coerceAtMost(goal.targetCount)
        val isCompleted = nextCount >= goal.targetCount
        dailyGoalDao.updateGoal(
            goal.copy(
                completedCount = nextCount,
                isCompleted = isCompleted
            )
        )
    }

    suspend fun decrementProgress(goal: DailyGoalEntity) {
        val nextCount = (goal.completedCount - 1).coerceAtLeast(0)
        val isCompleted = nextCount >= goal.targetCount
        dailyGoalDao.updateGoal(
            goal.copy(
                completedCount = nextCount,
                isCompleted = isCompleted
            )
        )
    }

    suspend fun deleteGoal(id: Int) {
        dailyGoalDao.deleteGoalById(id)
    }

    suspend fun seedDefaultGoalsIfEmpty(dateStr: String = getTodayDateString()) {
        val count = dailyGoalDao.getGoalCountForDate(dateStr)
        if (count == 0) {
            val defaultPresetGoals = listOf(
                DailyGoalEntity(
                    title = "Apply to 3 companies",
                    category = "Applications",
                    targetCount = 3,
                    completedCount = 0,
                    isCompleted = false,
                    date = dateStr
                ),
                DailyGoalEntity(
                    title = "Practice 1 interview question",
                    category = "Interview Prep",
                    targetCount = 1,
                    completedCount = 0,
                    isCompleted = false,
                    date = dateStr
                ),
                DailyGoalEntity(
                    title = "Send 2 networking connection requests",
                    category = "Networking",
                    targetCount = 2,
                    completedCount = 0,
                    isCompleted = false,
                    date = dateStr
                ),
                DailyGoalEntity(
                    title = "Update resume or review application tracker",
                    category = "Resume & Portfolio",
                    targetCount = 1,
                    completedCount = 0,
                    isCompleted = false,
                    date = dateStr
                )
            )
            dailyGoalDao.insertGoals(defaultPresetGoals)
        }
    }

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Date())
        }
    }
}
