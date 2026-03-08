package com.signtogether.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "conversation_messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class ConversationMessage(
    @PrimaryKey
    val messageId: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String, // "USER" (Deaf Person) or "STAFF" (Hearing Person)
    val input: String,
    val translatedOutput: String,
    val confidenceScore: Double? = null,
    val mode: String // "SignToText" or "TextToSign"
)
