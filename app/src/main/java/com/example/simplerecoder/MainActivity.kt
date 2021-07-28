package com.example.simplerecoder

import android.annotation.TargetApi
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import linc.com.amplituda.Amplituda as Amplituda


class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null


    private var output: String? = null
    private var recording: Boolean = false
    private var pause: Boolean = false
    private var permissionToRecordAccepted = false
    private var permissionToWriteAccepted = false
    private var startPlaying = false

    private lateinit var timer: Timer


    private val permissions = arrayOf(
        "android.permission.RECORD_AUDIO",
        "android.permission.WRITE_EXTERNAL_STORAGE",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        val requestCode = 200
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        timer = Timer(this)

        val recordButton: ImageButton = findViewById(R.id.recordButton)
        val playButton: ImageButton = findViewById(R.id.playButton)
        val playFromBeginningButton: ImageButton = findViewById(R.id.playFromBeginningButton)
        val loopButton: ImageButton = findViewById(R.id.loopButton)

        recordButton.setOnClickListener {
            if (startPlaying){mediaPlayer?.pause()}
            when (recording) {
                false -> {
                    recordButton.setImageResource(R.drawable.ic_baseline_record_red)
                    startRecording()
                }
                true -> {
                    recordButton.setImageResource(R.drawable.ic_baseline_record_black)
                    pauseRecording()
                }
            }
        }
        playButton.setOnClickListener {
            if (recording) {
                stopRecording()
                recordButton.setImageResource(R.drawable.ic_baseline_record_black)
            }
            if (startPlaying){
                playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
            else {playButton.setImageResource(R.drawable.ic_baseline_pause_24)}
            startPlaying()
        }

        playFromBeginningButton.setOnClickListener {

            if (recording) {
                recordButton.setImageResource(R.drawable.ic_baseline_record_black)
                stopRecording()
            }
            playButton.setImageResource(R.drawable.ic_baseline_pause_24)
            startPlaying(0)
        }
        loopButton.setOnClickListener {

            if ((mediaPlayer?.isLooping) == true){
            loopButton.setImageResource(R.drawable.ic_baseline_repeat_black)
            }
            else{
                loopButton.setImageResource(R.drawable.ic_baseline_repeat_red)
            }
            mediaPlayer?.isLooping = !((mediaPlayer?.isLooping) ?: false)
        }
    }


    override fun onStop() {
        super.onStop()
        mediaRecorder?.release()
        mediaRecorder = null
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun startPlaying(position: Int = 0) {
        val waveFromView: WaveFromView = findViewById(R.id.waveFromView)
        output = this.externalMediaDirs.first().absolutePath + "/recording.mp3"

        when (mediaPlayer) {
            null -> {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(applicationContext, Uri.parse(output))
                }
                mediaPlayer!!.prepare()
            }
            else -> waveFromView.clear()
        }

        if (!startPlaying)
        {
            startPlaying = true
            val amplituda = Amplituda(this@MainActivity)
            amplituda.fromFile(output)
            amplituda.amplitudesAsList { waveFromView.addAllAmplitude(it) }
            mediaPlayer!!.start()
            mediaPlayer!!.seekTo(position)
        }
        else{
            startPlaying = false
            mediaPlayer!!.pause()
        }
    }



    private fun startRecording() {
        try {
            output = this.externalMediaDirs.first().absolutePath + "/recording.mp3"
            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
            mediaRecorder?.setAudioEncodingBitRate(16*44100)
            mediaRecorder?.setAudioSamplingRate(44100)
            mediaRecorder?.setOutputFile(output)

            recording = true
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            timer.start()

            val waveFromView: WaveFromView = findViewById(R.id.waveFromView)
            waveFromView.clear()

            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording(){
        timer.stop()
            recording = false
            pause = false
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
    }


    @TargetApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {
        if(recording) {
            if(!pause){
                timer.pause()
                Toast.makeText(this,"Stopped!", Toast.LENGTH_SHORT).show()
                mediaRecorder?.pause()
                pause = true

            }else{
                resumeRecording()
                Log.d("test11", "resume")
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {
        timer.start()
        Toast.makeText(this,"Resume!", Toast.LENGTH_SHORT).show()
        mediaRecorder?.resume()
        pause = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            200 -> {
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                permissionToWriteAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
            }
        }
        if (!permissionToRecordAccepted) super@MainActivity.finish()
        if (!permissionToWriteAccepted) super@MainActivity.finish()
    }

    override fun onTimerTick(duration: String) {
        val durationTimer: TextView = findViewById(R.id.duration)
        val waveFromView: WaveFromView = findViewById(R.id.waveFromView)

        durationTimer.text = duration

        waveFromView.addAmplitude(mediaRecorder!!.maxAmplitude.toFloat())
    }


}