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
import android.os.CountDownTimer
import android.app.NotificationChannel
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.view.View
import android.preference.PreferenceManager
import android.content.SharedPreferences
import android.util.Log


class Session(context: Context) {

    private val prefs: SharedPreferences

    init {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun set(active: Boolean) {
        prefs.edit().putBoolean("active", active).apply()
    }

    fun get(): Boolean {
        return prefs.getBoolean("active",false)
    }
}

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
    var actived:Session?=null
    var active = false
    override fun onDestroy() {
        super.onDestroy()
        Session(this).set(false)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        actived = Session(this)
        active = actived!!.get()
        Log.d("----------active",active.toString())
        save.setOnClickListener {
            if(!active && second.text.toString()!="") {
                val time = second.text.toString().toLong()
                val type = spinner.selectedItemPosition
                val timeVal = when (type) {
                    0 -> time * 60
                    1 -> time * 3600
                    else -> time * 60
                }
                object : CountDownTimer(timeVal * 1000, 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        remainTime.text = "Qalan vaxt: ${vaxt(millisUntilFinished/1000)}"


                        val mBuilder = NotificationCompat.Builder(this@MainActivity, "notify_ms")
                        val ii = Intent(this@MainActivity, MainActivity::class.java)
                        val pendingIntent = PendingIntent.getActivity(this@MainActivity, 0, ii, 0)
                        val bigText = NotificationCompat.BigTextStyle()
                        bigText.bigText(vaxt(millisUntilFinished/1000))
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

                actived!!.set(true)
                set.visibility = View.GONE
                deset.visibility = View.VISIBLE
            }else if(active){
                set.visibility = View.GONE
                deset.visibility = View.VISIBLE
            }
        }

    }

    fun vaxt(time:Long):String{
        var returning=""
        if(time<60)
            returning = "$time saniyə"
        else if(time>60 && time<3600){
            val minutes = Math.floor((time/60).toDouble()).toInt()
            val seconds = if((time%60).toInt()!=0) (time%60).toString()+" san." else ""
            returning = "$minutes dəq. $seconds"
        }else if(time>3600){
            val hours = Math.floor((time/3600).toDouble()).toInt()
            val minutes = Math.floor((time-(hours*3600))/60.toDouble()).toInt()
            val seconds = if((time%60).toInt()!=0) (time%60).toString()+" san." else ""
            returning = "$hours saat $minutes dəq. $seconds"
        }
        return returning
    }

}
val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
    val mediaPlayer: MediaPlayer? = MediaPlayer()
        when (focusChange) { AudioManager.AUDIOFOCUS_LOSS -> {
            mediaPlayer!!.stop()
        }
    }
}

