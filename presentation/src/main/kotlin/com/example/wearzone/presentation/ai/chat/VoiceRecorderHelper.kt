package com.example.wearzone.presentation.ai.chat

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class VoiceRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecording(): File? {
        audioFile = File(context.cacheDir, "audio_message.m4a")
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)
            try {
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
        return audioFile
    }

    fun stopRecording() {
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder = null
        }
    }
}
