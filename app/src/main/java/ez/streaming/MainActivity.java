package ez.streaming;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    //boolean mBounded;
    //Music_service musicService = null;
    //private SeekBar volumeControl = null;
    boolean isPlaying ;
    boolean muted=false;
    float lastVolume;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MyActivity", "Activity On Create ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        final ImageButton powerButton = (ImageButton) findViewById(R.id.power);
        final ImageButton muteButton = (ImageButton) findViewById(R.id.mMuteButton);
        muteButton.setEnabled(false);
        muteButton.setAlpha(64);

        final ImageButton mPlayPauseButton = (ImageButton) findViewById(R.id.mPlayPauseButton);

        final SeekBar volumeControl = (SeekBar) findViewById(R.id.volBar);

        final TextView marquesina  = (TextView) findViewById(R.id.programa);

        marquesina.setSelected(true);  // Set focus to the textview


        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (isPlaying) {
                    stopService(new Intent(getApplicationContext(), Music_service.class));
                    isPlaying = false;
                    volumeControl.setEnabled(false);

                    mPlayPauseButton.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
                } else {
                    muteButton.setEnabled(true);
                    startService(new Intent(getApplicationContext(), Music_service.class));
                    isPlaying = true;
                    volumeControl.setEnabled(true);
                    muteButton.setAlpha(255);
                    mPlayPauseButton.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
                }
            }
        });

        muteButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (muted) {

                    muted = false;
                    volumeControl.setEnabled(true);
                    //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, lastVolume, 0);
                    Music_service.mediaPlayer.setVolume(lastVolume, lastVolume);
                    muteButton.setImageResource(R.drawable.ic_volume_up_black_36dp);
                } else {

                    muted = true;
                    int maxVolume = 15;
                    float log1=(float)(Math.log(maxVolume-volumeControl.getProgress()) / Math.log(maxVolume));
                    lastVolume = 1-log1;
                    volumeControl.setEnabled(false);
                    //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                    Music_service.mediaPlayer.setVolume(0,0);
                    muteButton.setImageResource(R.drawable.ic_volume_off_black_36dp);
                }
            }
        });




        powerButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                stopService(new Intent(getApplicationContext(), Music_service.class));

                finish();

            }
        });





            volumeControl.setMax(maxVolume);
            volumeControl.setProgress(curVolume);
            volumeControl.setEnabled(false);
            volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()

            {
                @Override
                public void onStopTrackingTouch (SeekBar arg0){
            }

                @Override
                public void onStartTrackingTouch (SeekBar arg0){
            }

                @Override
                public void onProgressChanged (SeekBar arg0,int progress, boolean arg2){
                Log.i("MyActivity", "Progress " + progress);
                int maxVolume = 15;
                float log1=(float)(Math.log(maxVolume-progress) / Math.log(maxVolume));
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                Music_service.mediaPlayer.setVolume(1-log1,1-log1);

            }
            }

            );
        }

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

        stopService(new Intent(getApplicationContext(), Music_service.class));

    }
}


