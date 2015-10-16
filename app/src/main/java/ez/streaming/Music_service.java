package ez.streaming;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;


import java.io.IOException;

public class Music_service extends Service implements MediaPlayer.OnErrorListener {

    public MediaPlayer mediaPlayer = new MediaPlayer();
    String url = "rtsp://iptv.cybertap.com.ar:1935/fmvida/fmvida.stream";
    //String url = "http://72.13.93.91:80";

    private static final String ACTION_PLAY = "com.example.action.PLAY";

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.i("MyActivity", "Service Bind ");
        return null;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        Log.i("MyActivity", "Service On Error ");
        mediaPlayer.reset();
        return true;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MyActivity", "Service onStartCommand ");

        /*
        // Se construye la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Servicio en segundo plano")
                .setContentText("Procesando...");

        // Crear Intent para iniciar una actividad al presionar la notificación
                Intent notificationIntent = new Intent(this, MainActivity.class);


                //creo que aca esta la papa ****************************************************************************
                notificationIntent.setFlags( Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );


                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
                builder.setContentIntent(pendingIntent);

        // Poner en primer plano
                startForeground(1, builder.build());
                */

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_media_play)
                        .setContentTitle("Fm Vida 103.5")
                        .setContentText("Reproduciendo...");
        // Creates an explicit intent for an Activity in your app
        final Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.

        // Adds the back stack for the Intent (but not the Intent itself)
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        int mId = 1;
        // mId allows you to update the notification later on.
        startForeground(mId, mBuilder.build());

        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
             mediaPlayer.setDataSource(url);
        } catch (IOException e) {
             Log.i("MyActivity", "error en el setdatasrouce " + e.getMessage() + e.getLocalizedMessage());
        }
        mediaPlayer.prepareAsync(); // prepare async to not block main thread
        Toast.makeText(getApplicationContext(), "Conectando...", Toast.LENGTH_LONG).show();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                Log.i("MyActivity", "Por start");
                mediaPlayer.start();
                Toast.makeText(getApplicationContext(), "Conectado", Toast.LENGTH_SHORT).show();
            }
            });
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("MyActivity", "Service On destroy ");
        mediaPlayer.release();
        mediaPlayer = null;
        Toast.makeText(getApplicationContext(), "Pausado", Toast.LENGTH_SHORT).show();
    }
}