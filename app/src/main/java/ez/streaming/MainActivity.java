package ez.streaming;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.content.Context;
import android.content.Intent;

import android.media.AudioManager;

import android.net.ConnectivityManager;

import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    //Audiomanager
    AudioManager audioManager;

    //Buttons y UI
    ImageButton reconectButton;
    ImageButton powerButton;
    ImageButton muteButton;
    ImageButton mPlayPauseButton;
    SeekBar volumeControl;
    TextView marquesina;
    Animation reconnectBlink;

    public static final String STATUS_INIT = "ez.streaming.action.STATUS_INIT";
    public static final String STATUS_PLAYING = "ez.streaming.action.STATUS_PLAYING";
    public static final String STATUS_PAUSE = "ez.streaming.action.STATUS_PAUSE";
    public static final String STATUS_CONNECTING = "ez.streaming.action.STATUS_CONNECTING";


    public static final String START_SERVICE = "ez.streaming.action.START_SERVICE";
    public static final String ACTION_RESUME = "ez.streaming.action.ACTION_RESUME";
    public static final String ACTION_PAUSE = "ez.streaming.action.ACTION_PAUSE";


    public static final String VOLUME_CHANGE = "ez.streaming.action.VOLUME_CHANGE";
    public static final String MUTE = "ez.streaming.action.MUTE";
    public static final String UNMUTE = "ez.streaming.action.UNMUTE";

    public static final String FACEBOOK_URL = "https://www.facebook.com/juancruz.rubino.12";
    public static final String TWITTER_URL = "https://twitter.com/fmvida1035";


    String player_status = STATUS_INIT;
    boolean muted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("sendMessage"));

        Log.i("MyActivity", "Activity On Create ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reconectButton = (ImageButton) findViewById(R.id.reconnecting);
        reconnectBlink = AnimationUtils.loadAnimation(this, R.anim.reconect_blink);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        powerButton = (ImageButton) findViewById(R.id.power);
        muteButton = (ImageButton) findViewById(R.id.mMuteButton);
        muteButton.setEnabled(false);
        muteButton.setAlpha(64);

        mPlayPauseButton = (ImageButton) findViewById(R.id.mPlayPauseButton);
        volumeControl = (SeekBar) findViewById(R.id.volBar);
        marquesina = (TextView) findViewById(R.id.programa);

        marquesina.setSelected(true);

        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (player_status == STATUS_PLAYING) {
                    player_status = STATUS_PAUSE;
                    volumeControl.setEnabled(false);
                    muteButton.setEnabled(false);
                    muteButton.setAlpha(64);
                    startService(new Intent(getApplicationContext(), Music_service.class).setAction(ACTION_PAUSE));
                    mPlayPauseButton.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
                } else {
                    if (player_status == STATUS_INIT) {
                        Log.i("MyActivity", "Llamndo servicio ");
                        int maxVolume = 16;
                        float log1 = (float) (Math.log(maxVolume - volumeControl.getProgress()) / Math.log(maxVolume));
                        //Cuando inicia le mando vol de lo que esta en la progress bar
                        startService(new Intent(getApplicationContext(), Music_service.class).setAction(START_SERVICE).putExtra("vol", 1 - log1));
                    } else if (player_status == STATUS_PAUSE) {
                        startService(new Intent(getApplicationContext(), Music_service.class).setAction(MainActivity.ACTION_RESUME));
                    }
                    muteButton.setEnabled(true);
                    muteButton.setAlpha(255);
                    mPlayPauseButton.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
                    player_status = STATUS_PLAYING;
                    volumeControl.setEnabled(true);
                }
            }
        });

        muteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            if (muted) {
                Log.i("MyActivity", "Desmuteado ");
                muted = false;
                volumeControl.setEnabled(true);
                Intent serviceIntent = new Intent(getApplicationContext(), Music_service.class);
                serviceIntent.setAction(UNMUTE);
                int maxVolume = 16;
                float log1=(float)(Math.log(maxVolume-volumeControl.getProgress()) / Math.log(maxVolume));
                serviceIntent.putExtra("lastVolume", 1 - log1);
                startService(serviceIntent);
                muteButton.setImageResource(R.drawable.ic_volume_up_black_36dp);
            } else {
                Log.i("MyActivity", "Muteado ");
                muted = true;
                volumeControl.setEnabled(false);
                startService(new Intent(getApplicationContext(), Music_service.class).setAction(MainActivity.MUTE));
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
        volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                Log.i("MyActivity", "Progress " + progress);
                int maxVolume = 15;
                float log1 = (float) (Math.log(maxVolume - progress) / Math.log(maxVolume));


                Intent serviceIntent = new Intent(getApplicationContext(), Music_service.class);
                serviceIntent.setAction(VOLUME_CHANGE);
                serviceIntent.putExtra("vol", 1 - log1);
                startService(serviceIntent);
            }
        });

        ImageView imgFacebook = (ImageView)findViewById(R.id.facebook);
        imgFacebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(FACEBOOK_URL));
                startActivity(intent);
            }
        });

        ImageView imgTwitter = (ImageView)findViewById(R.id.twitter);
        imgTwitter.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(TWITTER_URL));
                startActivity(intent);
            }
        });
    }

    private void updateUI(String command){
        switch(command) {
            case  STATUS_CONNECTING:
                reconectButton.setVisibility(View.VISIBLE);
                reconectButton.startAnimation(reconnectBlink);

                break;
            case  STATUS_PLAYING:
                reconectButton.clearAnimation();
                reconectButton.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        switch(intent.getStringExtra("command")) {
            case STATUS_CONNECTING:   updateUI(STATUS_CONNECTING);
                Log.i("MyActivity", "STATUS_CONNECTING ");
            break;
            case STATUS_PLAYING:   updateUI(STATUS_PLAYING);
                Log.i("MyActivity", "STATUS_PLAYING ");
            break;
        }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
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


