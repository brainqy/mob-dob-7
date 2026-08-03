package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_goals")
data class DailyGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String = "Applications",
    val targetCount: Int = 1,
    val completedCount: Int = 0,
    val isCompleted: Boolean = false,
    val date: String,
    val createdAt: Long = System.currentTimeMillis()
)
