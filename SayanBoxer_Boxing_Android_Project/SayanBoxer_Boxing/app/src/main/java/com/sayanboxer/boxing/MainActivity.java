package com.sayanboxer.boxing;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout main, content, tabs;
    private int selected=0, numRounds=3,workSeconds=120,restSeconds=30,frequency=8;
    private String mode="Beginner";
    private EditText roundsInput,workInput,restInput,freqInput;
    @Override public void onCreate(Bundle b){super.onCreate(b);
        getWindow().setStatusBarColor(Design.BG);getWindow().setNavigationBarColor(Design.BG);
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},20);}
        numRounds=BoxingData.prefs(this).getInt("cfg_rounds",3);
        workSeconds=BoxingData.prefs(this).getInt("cfg_work",120);
        restSeconds=BoxingData.prefs(this).getInt("cfg_rest",30);
        frequency=BoxingData.prefs(this).getInt("cfg_interval",8);
        mode=BoxingData.prefs(this).getString("cfg_mode","Beginner");
        show(0);
        if(!BoxingData.prefs(this).getBoolean("welcome",false)){
            new AlertDialog.Builder(this).setTitle("Welcome to Sayan Boxer 🥊")
                .setMessage("Train with spoken punch combinations. Start with light shadowboxing and warm up first. Stop exercising if you feel pain or dizziness. No login needed.")
                .setPositiveButton("Let's train",(d,w)->BoxingData.prefs(this).edit().putBoolean("welcome",true).apply()).show();
        }
    }
    @Override public void onResume(){super.onResume();if(main!=null&&selected==0)show(0);}
    private void show(int tab){selected=tab;
        main=Design.vertical(this);main.setBackgroundColor(Design.BG);Design.safeArea(main);setContentView(main);
        LinearLayout header=Design.horizontal(this);Design.pad(header,22,12,22,8);
        TextView icon=Design.text(this,"🥊",28,Design.RED,true);header.addView(icon,Design.lp(this,43,45,0));
        LinearLayout ident=Design.vertical(this);
        ident.addView(Design.text(this,"SAYAN BOXER",20,Design.WHITE,true));
        ident.addView(Design.text(this,"THE BOXING TRAINING APP",10,Design.MUTED,true));
        header.addView(ident,new LinearLayout.LayoutParams(0,-2,1));
        TextView tag=Design.text(this,"OFFLINE",11,Design.GREEN,true);
        tag.setBackground(Design.shape(Design.CARD2,Design.dp(this,12)));Design.pad(tag,9,7,9,7);
        header.addView(tag);main.addView(header,Design.lp(this,-1,68,0));
        ScrollView sc=Design.scroll(this);content=Design.vertical(this);Design.pad(content,20,4,20,24);
        sc.addView(content);main.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        tabs=Design.horizontal(this);tabs.setBackgroundColor(Design.CARD);Design.pad(tabs,10,10,10,13);main.addView(tabs,Design.lp(this,-1,75,0));
        String[] names={"⌂  Train","✚  Combos","▥  Stats","ⓘ  Guide"};
        for(int i=0;i<names.length;i++){final int choice=i;Button b=Design.button(this,names[i],selected==i?Design.RED:Design.CARD);b.setTextSize(12);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,Design.dp(this,45),1);
            p.leftMargin=Design.dp(this,3);p.rightMargin=Design.dp(this,3);tabs.addView(b,p);b.setOnClickListener(v->{capture();show(choice);});}
        if(tab==0)home();else if(tab==1)combos();else if(tab==2)stats();else guide();
    }
    private void add(LinearLayout v,int top){content.addView(v,Design.lp(this,-1,-2,top));}
    private TextView para(String s){TextView t=Design.text(this,s,14,Design.MUTED,false);t.setLineSpacing(Design.dp(this,3),1);return t;}
    private LinearLayout line(){LinearLayout r=Design.horizontal(this);return r;}
    private void home(){
        TextView heading=Design.text(this,"TRAIN LIKE A CHAMPION",26,Design.WHITE,true);content.addView(heading,Design.lp(this,-1,-2,15));
        content.addView(para("Your pocket boxing coach. Real rounds, random combinations, strong results."),Design.lp(this,-1,-2,9));
        LinearLayout hero=Design.vertical(this);Design.pad(hero,20,22,20,22);
        hero.setBackground(Design.border(0xFF302034,Design.dp(this,22),Design.RED));
        TextView heroTop=Design.text(this,"◉  READY FOR THE NEXT ROUND?",13,Design.ORANGE,true);hero.addView(heroTop);
        TextView punch=Design.text(this,"FIGHT SMART.\nPUNCH STRONG.",30,Design.WHITE,true);
        hero.addView(punch,Design.lp(this,-1,-2,16));
        hero.addView(Design.text(this,"Hands up. Chin down. Keep moving.",14,Design.MUTED,false),Design.lp(this,-1,-2,14));
        add(hero,18);
        if(WorkoutService.running){
            Button resume=Design.button(this,"▶  RETURN TO ACTIVE WORKOUT",Design.GREEN);resume.setTextColor(Design.BG);
            content.addView(resume,Design.lp(this,-1,54,17));resume.setOnClickListener(v->startActivity(new Intent(this,SessionActivity.class)));
        }
        content.addView(Design.section(this,"CHOOSE YOUR WORKOUT"));
        String[] modes={"Beginner","Intermediate","Advanced","HIIT"};
        String[] caption={"Basics • technique","Flow • rhythm","Power • defense","Speed • conditioning"};
        for(int i=0;i<4;i+=2){LinearLayout r=line();
            for(int j=i;j<i+2;j++){final String chosen=modes[j];LinearLayout block=Design.vertical(this);Design.pad(block,15,13,15,15);
                block.setBackground(Design.border(mode.equals(chosen)?0xFF482E35:Design.CARD,Design.dp(this,15),mode.equals(chosen)?Design.RED:Design.CARD2));
                TextView nm=Design.text(this,modes[j],16,Design.WHITE,true);block.addView(nm);
                TextView tiny=Design.text(this,caption[j],11,Design.MUTED,false);block.addView(tiny,Design.lp(this,-1,-2,6));
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,Design.dp(this,79),1);
                p.rightMargin=Design.dp(this,j%2==0?6:0);p.leftMargin=Design.dp(this,j%2==1?6:0);r.addView(block,p);
                block.setOnClickListener(v->{capture();mode=chosen;applyPreset(chosen);show(0);});
            }content.addView(r,Design.lp(this,-1,-2,11));
        }
        content.addView(Design.section(this,"ROUND SETTINGS"));
        LinearLayout cfg=Design.card(this);
        LinearLayout one=line();roundsInput=numberField(one,"Rounds",numRounds);workInput=numberField(one,"Round (sec)",workSeconds);cfg.addView(one);
        LinearLayout two=line();restInput=numberField(two,"Rest (sec)",restSeconds);freqInput=numberField(two,"Calls every (sec)",frequency);cfg.addView(two,Design.lp(this,-1,-2,18));
        add(cfg,7);
        TextView explain=para("Punch codes: 1 Jab  •  2 Cross  •  3 Lead Hook  •  4 Rear Hook\n5 Lead Uppercut  •  6 Rear Uppercut");
        content.addView(explain,Design.lp(this,-1,-2,18));
        Button go=Design.button(this,"▶   START BOXING WORKOUT",Design.RED);
        content.addView(go,Design.lp(this,-1,64,21));go.setOnClickListener(v->start());
        TextView note=para("No gym equipment required • Voice coaching works offline • Workout keeps running when the screen is locked");
        note.setGravity(Gravity.CENTER);content.addView(note,Design.lp(this,-1,-2,13));
    }
    private EditText numberField(LinearLayout parent,String label,int value){
        LinearLayout col=Design.vertical(this);col.addView(Design.label(this,label));
        EditText n=new EditText(this);n.setText(String.valueOf(value));n.setTextColor(Design.WHITE);n.setTextSize(19);n.setInputType(InputType.TYPE_CLASS_NUMBER);
        n.setSingleLine(true);n.setSelectAllOnFocus(true);n.setBackground(Design.shape(Design.CARD2,Design.dp(this,11)));Design.pad(n,12,10,12,10);
        col.addView(n,Design.lp(this,-1,49,7));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.rightMargin=Design.dp(this,7);parent.addView(col,p);return n;
    }
    private int safeNumber(EditText e,int fallback,int min,int max){if(e==null)return fallback;try{return Math.max(min,Math.min(max,Integer.parseInt(e.getText().toString().trim())));}catch(Exception ignored){return fallback;}}
    private void capture(){if(roundsInput==null)return;
        numRounds=safeNumber(roundsInput,numRounds,1,20);workSeconds=safeNumber(workInput,workSeconds,15,600);
        restSeconds=safeNumber(restInput,restSeconds,5,180);frequency=safeNumber(freqInput,frequency,3,45);
        BoxingData.prefs(this).edit().putInt("cfg_rounds",numRounds).putInt("cfg_work",workSeconds)
            .putInt("cfg_rest",restSeconds).putInt("cfg_interval",frequency).putString("cfg_mode",mode).apply();
        roundsInput=null;workInput=null;restInput=null;freqInput=null;
    }
    private void applyPreset(String kind){
        switch(kind){
            case "Beginner":numRounds=3;workSeconds=90;restSeconds=45;frequency=10;break;
            case "Intermediate":numRounds=5;workSeconds=120;restSeconds=40;frequency=8;break;
            case "Advanced":numRounds=8;workSeconds=180;restSeconds=30;frequency=6;break;
            case "HIIT":numRounds=6;workSeconds=45;restSeconds=15;frequency=4;break;
        }
        BoxingData.prefs(this).edit().putString("cfg_mode",mode).apply();
    }
    private void start(){capture();
        if(WorkoutService.running){new AlertDialog.Builder(this).setTitle("Workout already active")
            .setMessage("Finish your current workout before starting a new one.")
            .setPositiveButton("Open workout",(d,w)->startActivity(new Intent(this,SessionActivity.class))).setNegativeButton("Cancel",null).show();return;}
        Intent i=new Intent(this,WorkoutService.class).setAction(WorkoutService.START);
        i.putExtra("rounds",numRounds).putExtra("work",workSeconds).putExtra("rest",restSeconds)
            .putExtra("frequency",frequency).putExtra("mode",mode);
        try{startForegroundService(i);startActivity(new Intent(this,SessionActivity.class));}
        catch(Exception ex){new AlertDialog.Builder(this).setMessage("Could not start the workout service: "+ex.getMessage()).setPositiveButton("OK",null).show();}
    }
    private void combos(){
        content.addView(Design.title(this,"BUILD YOUR COMBOS"),Design.lp(this,-1,-2,18));
        content.addView(para("Enter your own boxing punch sequence. The coach will call random combinations during your training."),Design.lp(this,-1,-2,9));
        LinearLayout code=Design.card(this);code.addView(Design.text(this,"PUNCH NUMBER GUIDE",12,Design.ORANGE,true));
        code.addView(Design.text(this,"1  Jab     2  Cross     3  Lead Hook\n4  Rear Hook     5  Lead Uppercut\n6  Rear Uppercut",14,Design.WHITE,true),Design.lp(this,-1,-2,12));
        code.addView(para("Defense codes: SL Slip Left • SR Slip Right\nDL Duck Left • DR Duck Right • B Block • SP Step Back"),Design.lp(this,-1,-2,11));
        add(code,18);
        Button newCombo=Design.button(this,"＋  CREATE NEW COMBINATION",Design.RED);
        content.addView(newCombo,Design.lp(this,-1,55,17));newCombo.setOnClickListener(v->createCombo());
        ArrayList<String> list=BoxingData.combos(this);
        content.addView(Design.section(this,"SAVED COMBINATIONS  •  "+list.size()));
        if(list.isEmpty())content.addView(para("No combinations yet. Add one above to get started."));
        for(int i=0;i<list.size();i++){
            final int index=i;String c=list.get(i);
            LinearLayout row=Design.horizontal(this);row.setBackground(Design.shape(Design.CARD,Design.dp(this,14)));Design.pad(row,15,11,8,11);
            LinearLayout info=Design.vertical(this);info.addView(Design.text(this,c,19,Design.WHITE,true));
            info.addView(Design.text(this,BoxingData.spoken(c),12,Design.MUTED,false),Design.lp(this,-1,-2,5));
            row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            Button del=Design.button(this,"×",Design.CARD2);del.setTextSize(20);row.addView(del,Design.lp(this,44,45,0));
            del.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Remove combination?")
                .setMessage(c).setNegativeButton("Cancel",null).setPositiveButton("Remove",(d,w)->{
                    ArrayList<String> cs=BoxingData.combos(this);if(index<cs.size()){cs.remove(index);BoxingData.saveCombos(this,cs);show(1);}
                }).show());
            add(row,10);
        }
        Button reset=Design.button(this,"Restore default combinations",Design.CARD2);content.addView(reset,Design.lp(this,-1,50,21));
        reset.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Restore default combos?").setMessage("This replaces your saved combinations.")
            .setNegativeButton("Cancel",null).setPositiveButton("Restore",(d,w)->{
                ArrayList<String> defaults=new ArrayList<>();for(String c:BoxingData.DEFAULT)defaults.add(c);BoxingData.saveCombos(this,defaults);show(1);
            }).show());
    }
    private void createCombo(){
        EditText entry=new EditText(this);entry.setSingleLine(true);entry.setTextColor(Design.WHITE);entry.setHint("Example: 1-2-3-2 or 1-2-SL-2");entry.setHintTextColor(Design.MUTED);
        LinearLayout wrap=Design.vertical(this);Design.pad(wrap,22,5,22,0);wrap.addView(entry);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("New punch combination").setMessage("Use numbers 1–6 and optional SL, SR, DL, DR, B, SP, separated by hyphens.")
            .setView(wrap).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();
        dlg.setOnShowListener(a->dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String text=entry.getText().toString().trim().toUpperCase(Locale.ROOT).replace(" ","");
            if(!BoxingData.valid(text)){entry.setError("Example: 1-2-3-2 or 1-2-SL-2");return;}
            ArrayList<String> cs=BoxingData.combos(this);if(cs.contains(text)){entry.setError("Already saved");return;}
            cs.add(text);BoxingData.saveCombos(this,cs);dlg.dismiss();show(1);
        }));dlg.show();
    }
    private void stats(){
        content.addView(Design.title(this,"YOUR TRAINING STATS"),Design.lp(this,-1,-2,18));
        content.addView(para("Every round adds up. Your stats stay privately on this device."),Design.lp(this,-1,-2,9));
        int s=BoxingData.prefs(this).getInt("sessions",0);
        int total=BoxingData.prefs(this).getInt("seconds",0);
        int rounds=BoxingData.prefs(this).getInt("rounds_done",0);
        String[][] items={{String.valueOf(s),"Workouts"},{(total/60)+" min","Time trained"},{String.valueOf(rounds),"Rounds finished"}};
        for(String[] item:items){LinearLayout p=Design.card(this);
            p.addView(Design.text(this,item[0],32,Design.ORANGE,true));p.addView(para(item[1]),Design.lp(this,-1,-2,5));add(p,14);}
        content.addView(Design.section(this,"RECENT WORKOUTS"));
        try{JSONArray logs=new JSONArray(BoxingData.prefs(this).getString("history","[]"));
            if(logs.length()==0)content.addView(para("Your finished workouts will appear here. Start with your first round!"));
            for(int i=0;i<logs.length();i++){JSONObject e=logs.getJSONObject(i);LinearLayout card=Design.card(this);
                card.addView(Design.text(this,e.optString("mode","Boxing")+"  •  "+e.optInt("rounds")+" rounds",16,Design.WHITE,true));
                card.addView(para(e.optString("time")+"   •   "+Math.max(1,e.optInt("seconds")/60)+" min"),Design.lp(this,-1,-2,7));add(card,10);}
        }catch(Exception ignored){content.addView(para("History temporarily unavailable."));}
        Button clear=Design.button(this,"Clear workout history",Design.CARD2);content.addView(clear,Design.lp(this,-1,50,21));
        clear.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Delete training history?").setMessage("This cannot be undone.")
            .setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{BoxingData.prefs(this).edit().remove("history").remove("sessions").remove("seconds").remove("rounds_done").apply();show(2);}).show());
    }
    private void guide(){
        content.addView(Design.title(this,"HELP & BOXING GUIDE"),Design.lp(this,-1,-2,18));
        content.addView(para("Learn the basics and get the most from Sayan Boxer."),Design.lp(this,-1,-2,9));
        guideCard("01  •  GET STARTED","Choose Beginner, Intermediate, Advanced or HIIT. Adjust rounds, round duration, break and voice-call frequency. Tap START BOXING WORKOUT.");
        guideCard("02  •  PUNCH NUMBER SYSTEM","1 = Jab (lead straight)\n2 = Cross (rear straight)\n3 = Lead hook\n4 = Rear hook\n5 = Lead uppercut\n6 = Rear uppercut\n\nStart slowly and keep your guard up. Lead and rear depend on your stance.");
        guideCard("03  •  DEFENSE","SL = Slip left\nSR = Slip right\nDL = Duck left\nDR = Duck right\nB = Block\nSP = Step back");
        guideCard("04  •  VOICE COACH","Your coach calls a random combination every few seconds. For example, '1-2-3' becomes 'Jab, Cross, Lead Hook'. Spoken instructions use your phone's installed text-to-speech engine; make sure the media volume is audible.");
        guideCard("05  •  SAVE CUSTOM COMBOS","Open Combos, tap CREATE NEW COMBINATION and type 1-2-3-2 or 1-2-SL-2. Your sequences are saved on your phone. Intermediate and Advanced modes can use them.");
        guideCard("06  •  BACKGROUND TRAINING","During a session you can lock your phone or switch apps. The foreground workout service keeps time and gives voice prompts. Use the notification to pause, resume or stop training. Battery restrictions on some phones can interrupt it.");
        guideCard("07  •  EXAMPLE BEGINNER WORKOUT","Warm up gently for 5 minutes. Start with 3 rounds of 90 seconds, 45 seconds rest. Focus on good technique. Cool down afterward. No punching bag is required for shadowboxing.");
        guideCard("08  •  SAFETY","Exercise within your ability, keep clear space around you, and use protective equipment for bag work. Stop if you experience pain, faintness or breathing difficulty. Consult a qualified coach for technique and a clinician when appropriate.");
        LinearLayout developer=Design.card(this);
        developer.addView(Design.text(this,"🥊  DEVELOPED BY",12,Design.ORANGE,true));
        developer.addView(Design.text(this,"Sayan Boxer",24,Design.WHITE,true),Design.lp(this,-1,-2,7));
        developer.addView(para("Sayan Boxer Boxing Training • Version 1.0\nOffline • No login • No advertisements"),Design.lp(this,-1,-2,8));
        add(developer,23);
    }
    private void guideCard(String h,String body){LinearLayout c=Design.card(this);c.addView(Design.text(this,h,16,Design.ORANGE,true));c.addView(para(body),Design.lp(this,-1,-2,11));add(c,13);}
    @Override public void onBackPressed(){if(selected!=0){capture();show(0);}else super.onBackPressed();}
}
