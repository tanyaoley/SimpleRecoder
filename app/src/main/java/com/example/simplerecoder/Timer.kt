package com.example.simplerecoder

import android.os.Looper
import android.os.Handler

class Timer(listener: OnTimerTickListener) {

    interface  OnTimerTickListener{
        fun onTimerTick(duration: String)
    }
private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    var duration = 0L
    private var delay = 100L

    init {
        runnable = Runnable {
            duration += delay
            handler.postDelayed(runnable, delay)

            listener.onTimerTick(format())
        }
    }

    fun start(){
        handler.postDelayed(runnable, delay)
    }

    fun pause(){
        handler.removeCallbacks(runnable)
    }

    fun stop(){
        handler.removeCallbacks(runnable)
        duration = 0L
    }

    private fun format(): String{
        val millis = duration % 1000
        val seconds = (duration/1000) % 60
        val minutes = (duration/60000) % 60
        return "%02d:%02d:%02d".format(minutes, seconds, millis/10)
    }
}