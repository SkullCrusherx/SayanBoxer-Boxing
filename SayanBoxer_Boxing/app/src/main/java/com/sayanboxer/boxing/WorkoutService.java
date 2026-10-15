package com.sayanboxer.boxing;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class WorkoutService extends Service implements TextToSpeech.OnInitListener {
    public static final String START="com.sayanboxer.boxing.START", TOGGLE="com.sayanboxer.boxing.TOGGLE", STOP="com.sayanboxer.boxing.STOP", SKIP="com.sayanboxer.boxing.SKIP";
    public static volatile boolean running=false, paused=false, resting=false;
    public static volatile int round=1, rounds=3, remaining=0, elapsed=0, completedRounds=0;
    public static volatile String currentCombo="GET READY", mode="Beginner";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Random rng=new Random();
    private int workSec=120, restSec=30, intervalSec=8, lastShown=-1;
    private long phaseEndsAt, pauseAt, nextCallAt, startedAt, pausedTotal;
    private String[] combos=BoxingData.DEFAULT;
    private TextToSpeech speech;
    private boolean voiceReady=false, logged=false;
    private ToneGenerator tones;
    private PowerManager.WakeLock wakeLock;
    private NotificationManager notificationManager;
    private static final String CHANNEL="sayan_boxer_training";
    private final Runnable heartbeat=new Runnable(){@Override public void run(){tick();if(running)handler.postDelayed(this,200);}};

    @Override public void onCreate(){super.onCreate();
        notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel=new NotificationChannel(CHANNEL,"Boxing workout",NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Active Sayan Boxer workout timer and coach");notificationManager.createNotificationChannel(channel);
        try{tones=new ToneGenerator(AudioManager.STREAM_MUSIC,90);}catch(Exception ignored){}
        speech=new TextToSpeech(this,this);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent==null)return START_NOT_STICKY;
        String action=intent.getAction();
        if(START.equals(action))begin(intent);
        else if(TOGGLE.equals(action))toggle();
        else if(SKIP.equals(action))skip();
        else if(STOP.equals(action))finish(false);
        return START_NOT_STICKY;
    }
    private void begin(Intent i){
        if(running)finish(false);
        rounds=Math.max(1,Math.min(20,i.getIntExtra("rounds",3)));
        workSec=Math.max(15,Math.min(600,i.getIntExtra("work",120)));
        restSec=Math.max(5,Math.min(180,i.getIntExtra("rest",30)));
        intervalSec=Math.max(3,Math.min(45,i.getIntExtra("frequency",8)));
        mode=i.getStringExtra("mode");if(mode==null)mode="Custom";
        ArrayList<String> source=BoxingData.combos(this);
        if(source.isEmpty())for(String c:BoxingData.DEFAULT)source.add(c);
        combos=source.toArray(new String[0]);
        if("Beginner".equals(mode)){combos=new String[]{"1-2","1-1-2","1-2-3","2-3-2"};}
        else if("HIIT".equals(mode)){combos=new String[]{"1-2-1-2","1-2-3-2","3-2-3","1-2-5-2","1-2-3-2-1"};}
        round=1;completedRounds=0;resting=false;paused=false;logged=false;running=true;
        currentCombo="GUARD UP!";remaining=workSec;elapsed=0;pausedTotal=0;
        startedAt=SystemClock.elapsedRealtime();phaseEndsAt=startedAt+workSec*1000L;nextCallAt=startedAt+3000;
        PowerManager power=(PowerManager)getSystemService(POWER_SERVICE);
        try{wakeLock=power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SayanBoxer:VoiceCoach");wakeLock.acquire(4*60*60*1000L);}catch(Exception ignored){}
        if(Build.VERSION.SDK_INT>=29){startForeground(10,notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);}else startForeground(10,notification());
        bell();say("Round one. Fight!");handler.removeCallbacks(heartbeat);handler.post(heartbeat);
    }
    private void tick(){
        if(!running||paused)return;
        long now=SystemClock.elapsedRealtime();
        elapsed=(int)((now-startedAt-pausedTotal)/1000L);
        remaining=Math.max(0,(int)Math.ceil((phaseEndsAt-now)/1000.0));
        if(now>=phaseEndsAt){advance();return;}
        if(!resting&&now>=nextCallAt){
            String next=combos[rng.nextInt(combos.length)];currentCombo=next;
            say(BoxingData.spoken(next));nextCallAt=now+intervalSec*1000L;
        }
        if(remaining!=lastShown && remaining%3==0){lastShown=remaining;notificationManager.notify(10,notification());}
    }
    private void advance(){
        bell();long now=SystemClock.elapsedRealtime();
        if(!resting){
            completedRounds=round;
            if(round>=rounds){finish(true);return;}
            resting=true;remaining=restSec;phaseEndsAt=now+restSec*1000L;currentCombo="REST • BREATHE";
            say("Rest. Breathe and keep moving.");
        } else {
            round++;resting=false;remaining=workSec;phaseEndsAt=now+workSec*1000L;
            currentCombo="GUARD UP!";nextCallAt=now+2500;
            say("Round "+round+". Fight!");
        }
        lastShown=-1;notificationManager.notify(10,notification());
    }
    private void toggle(){
        if(!running)return;
        if(!paused){paused=true;pauseAt=SystemClock.elapsedRealtime();currentCombo="PAUSED";say("Workout paused");}
        else{long duration=SystemClock.elapsedRealtime()-pauseAt;phaseEndsAt+=duration;nextCallAt+=duration;pausedTotal+=duration;paused=false;currentCombo=resting?"REST • BREATHE":"GUARD UP!";say("Resume training");}
        notificationManager.notify(10,notification());
    }
    private void skip(){if(running){if(paused)toggle();advance();}}
    private void finish(boolean complete){
        if(!running){stopSelf();return;}
        int trainedSeconds=(int)((SystemClock.elapsedRealtime()-startedAt-pausedTotal)/1000L);
        running=false;paused=false;resting=false;handler.removeCallbacks(heartbeat);
        if(!logged){BoxingData.log(this,mode,trainedSeconds,completedRounds);logged=true;}
        if(complete){say("Workout complete. Great work, champion!");}
        try{stopForeground(STOP_FOREGROUND_REMOVE);}catch(Exception ignored){}
        if(wakeLock!=null&&wakeLock.isHeld())wakeLock.release();
        stopSelf();
    }
    private void bell(){if(tones!=null)try{tones.startTone(ToneGenerator.TONE_PROP_BEEP,450);}catch(Exception ignored){}}
    private void say(String s){if(voiceReady&&speech!=null&&!s.isEmpty())speech.speak(s,TextToSpeech.QUEUE_FLUSH,null,"boxing_coach");}
    @Override public void onInit(int status){if(status==TextToSpeech.SUCCESS&&speech!=null){int set=speech.setLanguage(Locale.US);voiceReady=set!=TextToSpeech.LANG_MISSING_DATA&&set!=TextToSpeech.LANG_NOT_SUPPORTED;speech.setSpeechRate(1.07f);}}
    private PendingIntent pending(String action,int code){Intent i=new Intent(this,WorkoutService.class).setAction(action);return PendingIntent.getService(this,code,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    private Notification notification(){
        Intent screen=new Intent(this,SessionActivity.class);
        PendingIntent content=PendingIntent.getActivity(this,15,screen,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        String subtitle=paused?"Paused":resting?"Rest time":"Train hard";
        String title="Round "+round+"/"+rounds+" • "+String.format(Locale.US,"%02d:%02d",remaining/60,remaining%60);
        return new Notification.Builder(this,CHANNEL).setSmallIcon(com.sayanboxer.boxing.R.drawable.ic_glove)
            .setContentTitle(title).setContentText(subtitle+" • "+currentCombo).setContentIntent(content)
            .setOngoing(true).setOnlyAlertOnce(true).setCategory(Notification.CATEGORY_SERVICE)
            .addAction(new Notification.Action.Builder(null,paused?"Resume":"Pause",pending(TOGGLE,12)).build())
            .addAction(new Notification.Action.Builder(null,"Stop",pending(STOP,13)).build()).build();
    }
    @Override public void onDestroy(){handler.removeCallbacks(heartbeat);if(wakeLock!=null&&wakeLock.isHeld())wakeLock.release();
        if(speech!=null){speech.stop();speech.shutdown();}if(tones!=null)tones.release();running=false;super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}
