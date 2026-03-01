package com.fraud_detector.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ThreatLevel { LOW, MEDIUM, HIGH }

data class ThreatResult(
    val sender: String,
    val body: String,
    val score: Float,          // 0.0 – 1.0
    val level: ThreatLevel,
    val timestamp: Long
)

@Entity(tableName = "threats")
data class ThreatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val body: String,
    val score: Float,
    val level: String,         // stored as string for Room
    val timestamp: Long
)

fun ThreatResult.toEntity() = ThreatEntity(
    sender = sender, body = body, score = score,
    level = level.name, timestamp = timestamp
)

fun ThreatEntity.toResult() = ThreatResult(
    sender = sender, body = body, score = score,
    level = ThreatLevel.valueOf(level), timestamp = timestamp
)