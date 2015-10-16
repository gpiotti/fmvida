package ez.streaming;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    boolean mBounded;
    Music_service musicService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MyActivity", "Activity On Create ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToggleButton toggle = (ToggleButton) findViewById(R.id.togglebutton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent mIntent = new Intent(getApplicationContext(), Music_service.class);
                if (isChecked) {
                    startService(mIntent);
                } else {
                    stopService(mIntent);
                }
                bindService(mIntent, mConnection, BIND_AUTO_CREATE);
            }
        });

        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        SeekBar volControl = (SeekBar)findViewById(R.id.volBar);
        volControl.setMax(maxVolume);
        volControl.setProgress(curVolume);
        volControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                Log.i("MyActivity", "Progress " + progress);
                //Intent intent = getIntent();
                //intent.getExtras().toString();
                //(Music_service)intent.onVolumeChanged(progress);
                //Music_service.mediaPlayer.setVolume(progress, progress);
                musicService.setVolume(progress);
            }
        });
    }

    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            Log.i("MyActivity", "Service is disconnected");
            mBounded = false;
            musicService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("MyActivity", "Service is connected");
            mBounded = true;
            Music_service.LocalBinder mLocalBinder = (Music_service.LocalBinder)service;
            musicService = mLocalBinder.getServerInstance();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        Log.i("MyActivity", "Activity On Stop ");
    }
    protected void onRestart() {
        super.onRestart();
        Log.i("MyActivity", "Activity On Restart ");
    }
    protected void onDestroy() {
        super.onDestroy();  // Always call the superclass method first
        Log.i("MyActivity", "Activity On Destroy ");

    }
}


