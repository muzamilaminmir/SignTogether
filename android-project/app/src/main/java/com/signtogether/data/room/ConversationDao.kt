package com.signtogether.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Insert
    suspend fun insertSession(session: ConversationSession)

    @Insert
    suspend fun insertMessage(message: ConversationMessage)

    @Query("SELECT * FROM conversation_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ConversationSession>>

    @Query("SELECT * FROM conversation_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ConversationMessage>>

    @Query("DELETE FROM conversation_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM conversation_sessions")
    suspend fun clearAllHistory()
}
