package pashayev.mediastopper

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import android.os.CountDownTimer
import android.app.NotificationChannel
import android.os.Build
import android.support.v4.app.NotificationCompat


class mediaService : Service() {


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val timeVal = intent.getLongExtra("timeVal",100)*1000
        Handler().postDelayed({
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.requestAudioFocus(focusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN)
        },timeVal)

        return Service.START_STICKY
    }
}
class MainActivity : AppCompatActivity() {
    var active=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        save.setOnClickListener {
            if(!active) {
                val time = second.text.toString().toLong()
                val type = spinner.selectedItemPosition
                val timeVal = when (type) {
                    0 -> time * 60
                    1 -> time * 3600
                    else -> time * 60
                }
                object : CountDownTimer(timeVal * 1000, 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        remainTime.text = "Qalan vaxt: ${millisUntilFinished / 1000}"


                        val mBuilder = NotificationCompat.Builder(this@MainActivity, "notify_ms")
                        val ii = Intent(this@MainActivity, MainActivity::class.java)
                        val pendingIntent = PendingIntent.getActivity(this@MainActivity, 0, ii, 0)
                        val bigText = NotificationCompat.BigTextStyle()
                        bigText.bigText("${millisUntilFinished / 1000} saniyə")
                        bigText.setBigContentTitle("Qalan vaxt")
                        bigText.setSummaryText("MediaStopper vaxt məlumatı")
                        mBuilder.setContentIntent(pendingIntent)
                        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
                        mBuilder.setPriority(Notification.PRIORITY_MAX)
                        mBuilder.setStyle(bigText)

                        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        {
                            val channel = NotificationChannel("notify_ms", "Mediastopper",
                                    NotificationManager.IMPORTANCE_DEFAULT)
                            mNotificationManager.createNotificationChannel(channel)
                        }
                        mNotificationManager.notify(0, mBuilder.build())

                    }

                    override fun onFinish() {
                        remainTime.setText("Bitdi!")
                        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        mNotificationManager.cancel(0)
                        active=false
                    }

                }.start()



                val intent = Intent(this, mediaService::class.java)
                intent.putExtra("timeVal", timeVal)
                startService(intent)

                active=true
            }
        }

    }

}
val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
    val mediaPlayer: MediaPlayer? = MediaPlayer()
    when (focusChange) { AudioManager.AUDIOFOCUS_LOSS -> {
        mediaPlayer!!.stop()
    }
    }
}

