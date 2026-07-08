package com.example.wearzone.presentation.ai.chat.composable

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.presentation.R
import com.example.wearzone.presentation.ai.chat.VoiceRecorderHelper
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun ChatInputBar(
    inputText: String,
    isSending: Boolean,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onTranscribeAudio: (File) -> Unit
) {
    val context = LocalContext.current
    val voiceRecorder = remember { VoiceRecorderHelper(context) }
    var isRecording by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    // Ticks the timer while recording, resets when a new recording starts
    LaunchedEffect(isRecording) {
        if (isRecording) {
            elapsedSeconds = 0
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Optionally handle permission denial
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .imePadding(),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 4.dp)
            ) {
                if (isRecording) {
                    RecordingBar(elapsedSeconds = elapsedSeconds)
                } else {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onTextChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.chat_input_placeholder)) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        maxLines = 4
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier.padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRecording) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.secondaryContainer
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        isRecording = true
                                        voiceRecorder.startRecording()

                                        // Suspends until finger is released or gesture is cancelled
                                        tryAwaitRelease()

                                        isRecording = false
                                        voiceRecorder.stopRecording()
                                        val file = File(context.cacheDir, "audio_message.m4a")
                                        if (file.exists()) {
                                            onTranscribeAudio(file)
                                        }
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = stringResource(R.string.hold_to_record),
                        tint = if (isRecording) MaterialTheme.colorScheme.onError
                        else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // 3. Send Button
                val canSend = inputText.isNotBlank() && !isSending
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (canSend) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable(enabled = canSend) {
                            onSendClicked()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.send),
                            tint = if (canSend) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
