package ez.streaming;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private static final String STATUS_INIT = "ez.streaming.action.STATUS_INIT";
    private static final String STATUS_PLAYING = "ez.streaming.action.STATUS_PLAYING";
    private static final String STATUS_PAUSE = "ez.streaming.action.STATUS_PAUSE";


    public static final String START_SERVICE = "ez.streaming.action.START_SERVICE";
    public static final String ACTION_RESUME = "ez.streaming.action.ACTION_RESUME";
    public static final String ACTION_PAUSE = "ez.streaming.action.ACTION_PAUSE";


    public static final String VOLUME_CHANGE = "ez.streaming.action.VOLUME_CHANGE";
    public static final String MUTE = "ez.streaming.action.MUTE";
    public static final String UNMUTE = "ez.streaming.action.UNMUTE";


    //boolean mBounded;
    //Music_service musicService = null;
    //private SeekBar volumeControl = null;
    String player_status = STATUS_INIT ;
    boolean muted=false;
    private float lastVolume;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MyActivity", "Activity On Create ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //final MediaPlayer service_player = Music_service.mediaPlayer;

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
                if (player_status == STATUS_PLAYING) {
                    //stopService(new Intent(getApplicationContext(), Music_service.class));
                    player_status = STATUS_PAUSE;
                    volumeControl.setEnabled(false);
                    //TODO
                    startService(new Intent(getApplicationContext(), Music_service.class).setAction(ACTION_PAUSE));
                    //service_player.pause();

                    mPlayPauseButton.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
                } else {
                    muteButton.setEnabled(true);




                    if (player_status == STATUS_INIT) {
                        Log.i("MyActivity", "Llamndo servicio ");
                        int maxVolume = 16;
                        float log1=(float)(Math.log(maxVolume-volumeControl.getProgress()) / Math.log(maxVolume));
                        //Cuando inicia le madno vol de lo que esta en la progress bar
                        startService(new Intent(getApplicationContext(), Music_service.class).setAction(START_SERVICE).putExtra("vol",1-log1));
                        volumeControl.setEnabled(true);
                        muteButton.setAlpha(255);
                        mPlayPauseButton.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
                        player_status = STATUS_PLAYING;
                    }
                    else if (player_status == STATUS_PAUSE) {


                        startService(new Intent(getApplicationContext(), Music_service.class).setAction(MainActivity.ACTION_RESUME));
                        volumeControl.setEnabled(true);
                        muteButton.setAlpha(255);
                        mPlayPauseButton.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
                        player_status = STATUS_PLAYING;
                    }
                }
            }
        });

        muteButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (muted) {
                    Log.i("MyActivity", "Desmuteado ");
                    muted = false;
                    volumeControl.setEnabled(true);
                    //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, lastVolume, 0);
                    //TODO
                    Intent serviceIntent = new Intent(getApplicationContext(), Music_service.class);
                    serviceIntent.setAction(UNMUTE);
                    int maxVolume = 16;
                    float log1=(float)(Math.log(maxVolume-volumeControl.getProgress()) / Math.log(maxVolume));
                    serviceIntent.putExtra("lastVolume", 1 - log1);
                    startService(serviceIntent);

                    //startService(new Intent(getApplicationContext(), Music_service.class).setAction(MainActivity.UNMUTE));
                    //service_player.setVolume(lastVolume, lastVolume);
                    muteButton.setImageResource(R.drawable.ic_volume_up_black_36dp);
                } else {
                    Log.i("MyActivity", "Muteado ");
                    muted = true;

                    volumeControl.setEnabled(false);

                    //TODO
                    startService(new Intent(getApplicationContext(), Music_service.class).setAction(MainActivity.MUTE));
                    //service_player.setVolume(0,0);
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
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);¡
                    //TODO
                    Intent serviceIntent = new Intent(getApplicationContext(), Music_service.class);
                    serviceIntent.setAction(VOLUME_CHANGE);
                    serviceIntent.putExtra("vol", 1 - log1);
                    startService(serviceIntent);
                //startService(new Intent(getApplicationContext(), Music_service.class).setAction(MainActivity.VOLUME_CHANGE).putExtra("vol", 1 - log1));

                //service_player.setVolume(1-log1,1-log1);

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


