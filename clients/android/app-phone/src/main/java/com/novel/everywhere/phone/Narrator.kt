package com.novel.everywhere.phone

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class Narrator(context: Context) : TextToSpeech.OnInitListener {

    private val engine = TextToSpeech(context, this)
    private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            engine.language = Locale.CHINA
        }
    }

    fun speak(text: String) {
        if (!ready) return
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "novel-line")
    }

    fun stop() {
        engine.stop()
    }

    fun release() {
        engine.shutdown()
    }
}
