package com.signtogether.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "conversation_sessions")
data class ConversationSession(
    @PrimaryKey
    val sessionId: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val participantName: String = "Unknown",
    val isHelpDeskMode: Boolean = false
)
