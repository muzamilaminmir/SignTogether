package com.signtogether.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.signtogether.data.room.ConversationMessage
import com.signtogether.data.room.ConversationSession
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun exportSessionToPdf(
        context: Context,
        session: ConversationSession,
        messages: List<ConversationMessage>
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        
        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
            color = Color.BLACK
        }
        
        val textPaint = Paint().apply {
            textSize = 14f
            color = Color.DKGRAY
        }

        val df = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val sessionDate = df.format(Date(session.timestamp))
        
        canvas.drawText("SignTogether Conversation Transcript", 50f, 50f, titlePaint)
        canvas.drawText("Date: $sessionDate", 50f, 80f, textPaint)
        canvas.drawText("Participant: ${session.participantName}", 50f, 100f, textPaint)
        canvas.drawLine(50f, 120f, 545f, 120f, titlePaint)

        var yPosition = 150f

        for (msg in messages) {
            val msgDate = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp))
            val senderText = "${msg.sender} ($msgDate):"
            
            // Check if we need a new page
            if (yPosition > 800f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }

            textPaint.isFakeBoldText = true
            textPaint.color = if (msg.sender == "USER") Color.parseColor("#91C6BC") else Color.parseColor("#215E61")
            canvas.drawText(senderText, 50f, yPosition, textPaint)
            
            yPosition += 20f
            textPaint.isFakeBoldText = false
            textPaint.color = Color.BLACK
            
            // Draw input
            canvas.drawText("Input: ${msg.input}", 60f, yPosition, textPaint)
            yPosition += 20f
            
            // Draw Output
            canvas.drawText("Translated: ${msg.translatedOutput}", 60f, yPosition, textPaint)
            yPosition += 30f // Space between messages
        }

        pdfDocument.finishPage(page)

        // Save PDF to Downloads folder
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, "SignTogether_${session.sessionId.substring(0, 8)}.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "Saved PDF to Downloads!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}
