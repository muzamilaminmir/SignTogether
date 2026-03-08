package com.signtogether.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.signtogether.SignTogetherApp
import com.signtogether.data.room.ConversationMessage
import com.signtogether.data.room.ConversationSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ConversationViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dao = (application as SignTogetherApp).database.conversationDao()

    val allSessions: Flow<List<ConversationSession>> = dao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ConversationMessage>> {
        return dao.getMessagesForSession(sessionId)
    }

    fun insertSession(session: ConversationSession) {
        viewModelScope.launch {
            dao.insertSession(session)
        }
    }

    fun insertMessage(message: ConversationMessage) {
        viewModelScope.launch {
            dao.insertMessage(message)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            dao.deleteSession(sessionId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            dao.clearAllHistory()
        }
    }
}
