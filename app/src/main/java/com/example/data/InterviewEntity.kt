package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interviews")
data class InterviewEntity(
    @PrimaryKey val id: String,
    val companyName: String,
    val jobTitle: String,
    val status: String,            // "Upcoming", "Completed", "Cancelled", "Pending"
    val date: String,              // e.g. "Fri, July 25, 2026"
    val time: String,              // e.g. "2:00 PM PST"
    val location: String,
    val interviewer: String,
    val notes: String,
    val type: String,              // "JOB_TRACKER", "AI_MOCK", "EXPERT", "FRIEND"
    val tenantId: String = "platform",
    val updatedAt: Long = System.currentTimeMillis()
)

fun InterviewEntity.toInterviewItem(): InterviewItem {
    return InterviewItem(
        id = id,
        companyName = companyName,
        jobTitle = jobTitle,
        status = status,
        date = date,
        time = time,
        location = location,
        interviewer = interviewer,
        notes = notes,
        type = type,
        tenantId = tenantId
    )
}

fun InterviewItem.toInterviewEntity(): InterviewEntity {
    return InterviewEntity(
        id = id,
        companyName = companyName,
        jobTitle = jobTitle,
        status = status,
        date = date,
        time = time,
        location = location,
        interviewer = interviewer,
        notes = notes,
        type = type,
        tenantId = tenantId
    )
}

fun InterviewItem.toInterviewEntity(type: String): InterviewEntity {
    return InterviewEntity(
        id = id,
        companyName = companyName,
        jobTitle = jobTitle,
        status = status,
        date = date,
        time = time,
        location = location,
        interviewer = interviewer,
        notes = notes,
        type = type,
        tenantId = tenantId
    )
}
