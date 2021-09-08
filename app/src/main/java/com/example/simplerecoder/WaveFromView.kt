package com.example.simplerecoder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class WaveFromView(context: Context?, attrs: AttributeSet?): View(context,attrs)
{
    private var paint = Paint()
    private var amplitudes = ArrayList<Float>()
    private var spikes = ArrayList<RectF>()

    private var radius = 6f
    var w = 9f

    private var sw = 0f
    private var sh = 400f
    var d =6f

    private var maxSpikes = 0

    init {
        paint.color = Color.rgb(244, 81, 30)
        setDefault()
    }

    private fun setDefault(){
        w = 9f
        d = 6f
        sw = resources.displayMetrics.widthPixels .toFloat()
        maxSpikes = (sw / (w+d)).toInt()
    }
    fun addAmplitude(amp: Float) {
        val norm = min(amp.toInt()/7,400).toFloat()
        amplitudes.add(norm)

        spikes.clear()
        if (amplitudes.size > maxSpikes){
            w /= 2
            d /= 2
            maxSpikes = (sw / (w+d)).toInt()
        }

        for (i in amplitudes.indices){
            val left = i * (d+w)
            val top = (sh - amplitudes[i])/2
            val right = left + w
            val bottom = top + amplitudes[i]
            spikes.add(RectF(left, top, right, bottom))
        }
        invalidate()
    }

    fun clear()
    {
        setDefault()

        amplitudes.clear()
        spikes.clear()
        invalidate()
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        spikes.forEach {
            canvas?.drawRoundRect(it, radius, radius, paint)
        }
    }

    fun addAllAmplitude(amp: MutableList<Int>?) {
        amp!!.indices.forEach { i ->
            var norm = min(amp[i]*100,400).toFloat()
            if (norm == 0.toFloat()){ norm = 10.toFloat()}
            amplitudes.add(norm)
        }
        spikes.clear()
        w = (sw/amplitudes.size)*3/4
        d =(sw/amplitudes.size)/4
        maxSpikes = (sw / (w+d)).toInt()

        for (i in amplitudes.indices){
            val left = i * (d+w)
            val top = (sh - amplitudes[i])/2
            val right = left + w
            val bottom = top + amplitudes[i]
            spikes.add(RectF(left, top, right, bottom))
        }
        invalidate()
    }

}