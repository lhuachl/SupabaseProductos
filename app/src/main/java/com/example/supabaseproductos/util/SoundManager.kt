package com.example.supabaseproductos.util

import android.media.AudioAttributes
import android.media.ToneGenerator
import android.media.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SoundManager {
    private var toneGenerator: ToneGenerator? = null

    init {
        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
    }

    suspend fun playCreateSound() {
        withContext(Dispatchers.IO) {
            // High pitch ascending tone for create
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
        }
    }

    suspend fun playUpdateSound() {
        withContext(Dispatchers.IO) {
            // Medium pitch double beep for update
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
        }
    }

    suspend fun playDeleteSound() {
        withContext(Dispatchers.IO) {
            // Low pitch descending tone for delete
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 250)
        }
    }

    suspend fun playReadSound() {
        withContext(Dispatchers.IO) {
            // Short single beep for read
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
        }
    }

    suspend fun playSyncSound() {
        withContext(Dispatchers.IO) {
            // Success sound for sync
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300)
        }
    }

    suspend fun playConnectedSound() {
        withContext(Dispatchers.IO) {
            // Connection established sound
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 200)
        }
    }

    suspend fun playDisconnectedSound() {
        withContext(Dispatchers.IO) {
            // Connection lost sound
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
