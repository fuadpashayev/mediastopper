package pashayev.mediastopper

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


class mediaService : Service() {


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("----------a",intent.extras.getParcelable<Parcelable>("second").toString())
//        Handler().postDelayed({
//            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
//            am.requestAudioFocus(focusChangeListener,
//                    AudioManager.STREAM_MUSIC,
//                    AudioManager.AUDIOFOCUS_GAIN)
//        },second)
        return Service.START_STICKY
    }
}
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        save.setOnClickListener {
            val second = second.text
            val intent = Intent(this,mediaService::class.java)
            intent.putExtra("second",second)
            startService(intent)

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

