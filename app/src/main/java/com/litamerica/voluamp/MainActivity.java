package com.litamerica.voluamp;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.media.audiofx.BassBoost;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.content.res.ColorStateList;

public class MainActivity extends Activity {
 final int BG=0xff0c1423, PANEL=0xff17253a, TEXT=0xfff5f8ff, MUTED=0xffa9bed4, BLUE=0xff45bbfa;
 LinearLayout root, content, tabs;
 AudioManager audio;
 Equalizer eq;
 BassBoost bassFx;
 SeekBar volume, bass, eqSlider;
 TextView volumeValue, status;
 SharedPreferences prefs;
 boolean effects=false;
 int bassValue=25, eqValue=0;
 String preset="Car AUX";
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  audio=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
  prefs=getSharedPreferences("voluamp",MODE_PRIVATE);
  bassValue=prefs.getInt("bass",25); eqValue=prefs.getInt("eq",0);
  preset=prefs.getString("preset","Car AUX");
  getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);
  root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(20),dp(20),dp(20),dp(20));scroll.addView(root);setContentView(scroll);
  TextView brand=text("◉  VoluAmp",27,TEXT,true);brand.setPadding(0,dp(6),0,dp(3));
  text("AMPLIFY YOUR SOUND  •  LIT AMERICA",11,MUTED,false);
  tabs=new LinearLayout(this);tabs.setOrientation(0);tabs.setPadding(0,dp(24),0,dp(14));root.addView(tabs);
  content=new LinearLayout(this);content.setOrientation(1);root.addView(content);
  show("Home");
 }
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
 GradientDrawable shape(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
 TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(0,dp(7),0,dp(7));root.addView(t);return t;}
 TextView add(LinearLayout parent,String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(0,dp(6),0,dp(6));parent.addView(t);return t;}
 LinearLayout panel(){LinearLayout p=new LinearLayout(this);p.setOrientation(1);p.setPadding(dp(18),dp(18),dp(18),dp(18));p.setBackground(shape(PANEL,20));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(9),0,dp(9));content.addView(p,lp);return p;}
 void button(LinearLayout parent,String title,Runnable run){TextView b=add(parent,title,15,TEXT,true);b.setGravity(Gravity.CENTER);b.setBackground(shape(0xff28415d,14));b.setPadding(dp(12),dp(15),dp(12),dp(15));LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)b.getLayoutParams();lp.setMargins(0,dp(7),0,dp(7));b.setLayoutParams(lp);b.setOnClickListener(v->run.run());}
 SeekBar slider(LinearLayout parent,int max,int progress,java.util.function.IntConsumer changed){SeekBar s=new SeekBar(this);s.setMax(max);s.setProgress(progress);s.setProgressTintList(ColorStateList.valueOf(BLUE));s.setThumbTintList(ColorStateList.valueOf(BLUE));parent.addView(s);s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar bar,int value,boolean user){if(user)changed.accept(value);}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});return s;}
 void show(String page){tabs.removeAllViews();content.removeAllViews();
  for(String name:new String[]{"Home","Sound","Presets","Settings"}){TextView t=new TextView(this);t.setText(name);t.setTextColor(page.equals(name)?BG:MUTED);t.setTextSize(12);t.setGravity(Gravity.CENTER);t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(3),dp(12),dp(3),dp(12));if(page.equals(name))t.setBackground(shape(BLUE,12));tabs.addView(t,new LinearLayout.LayoutParams(0,-2,1));t.setOnClickListener(v->show(name));}
  if(page.equals("Home"))home();else if(page.equals("Sound"))sound();else if(page.equals("Presets"))presets();else settings();
 }
 void home(){
  LinearLayout p=panel();add(p,"MEDIA VOLUME",13,MUTED,true);
  volumeValue=add(p,"",44,TEXT,true);volumeValue.setGravity(Gravity.CENTER);
  int max=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
  int current=audio.getStreamVolume(AudioManager.STREAM_MUSIC);
  volumeValue.setText(Math.round(current*100f/max)+"%");
  volume=slider(p,max,current,v->{audio.setStreamVolume(AudioManager.STREAM_MUSIC,v,0);volumeValue.setText(Math.round(v*100f/max)+"%");});
  add(p,"Controls Android media volume, not additional hardware amplification.",12,MUTED,false);
  LinearLayout q=panel();add(q,"CURRENT PRESET",12,MUTED,true);add(q,preset,22,TEXT,true);
  button(q,"Choose a preset",()->show("Presets"));
  LinearLayout fx=panel();add(fx,"AUDIO PROCESSING",15,TEXT,true);
  status=add(fx,effects?"Experimental effects enabled":"Experimental effects off",13,effects?0xff90e8bb:MUTED,false);
  button(fx,effects?"Turn experimental effects off":"Try experimental audio effects",()->{toggleEffects();show("Home");});
  add(fx,"Third-party apps, including Amazon Music, may ignore these effects.",12,MUTED,false);
 }
 void sound(){
  LinearLayout p=panel();add(p,"BASS BOOST",17,TEXT,true);
  TextView val=add(p,bassValue+"%",14,MUTED,false);
  bass=slider(p,100,bassValue,v->{bassValue=v;val.setText(v+"%");prefs.edit().putInt("bass",v).apply();applyEffects();});
  LinearLayout q=panel();add(q,"EQUALIZER LIFT (EXPERIMENTAL)",17,TEXT,true);
  TextView ev=add(q,eqValue+" dB",14,MUTED,false);
  eqSlider=slider(q,6,eqValue,v->{eqValue=v;ev.setText(v+" dB");prefs.edit().putInt("eq",v).apply();applyEffects();});
  add(q,"May cause clipping or distortion. Only works where Android supports global audio effects.",12,MUTED,false);
  button(q,effects?"Disable effects":"Enable effects",()->{toggleEffects();show("Sound");});
 }
 void presets(){
  LinearLayout p=panel();add(p,"ONE-TAP SOUND PRESETS",18,TEXT,true);
  String[] names={"Car AUX","Balanced","Bass","Podcast","Custom"};
  for(String name:names)button(p,(preset.equals(name)?"✓  ":"")+name,()->{
   preset=name;
   if(name.equals("Car AUX")){bassValue=20;eqValue=0;}
   if(name.equals("Balanced")){bassValue=0;eqValue=0;}
   if(name.equals("Bass")){bassValue=60;eqValue=0;}
   if(name.equals("Podcast")){bassValue=0;eqValue=0;}
   prefs.edit().putString("preset",preset).putInt("bass",bassValue).putInt("eq",eqValue).apply();
   applyEffects();show("Presets");
  });
  add(p,"Presets change sound-effect settings only when processing is supported.",12,MUTED,false);
 }
 void settings(){
  LinearLayout p=panel();add(p,"AUDIO COMPATIBILITY",18,TEXT,true);
  add(p,"Global effects: "+(effects?"Enabled (not guaranteed to affect other apps)":"Disabled"),14,MUTED,false);
  button(p,"Reset sound settings",()->{bassValue=25;eqValue=0;preset="Car AUX";prefs.edit().clear().apply();applyEffects();show("Settings");});
  LinearLayout q=panel();add(q,"SAFETY & LIMITATIONS",17,TEXT,true);
  add(q,"Start with your car stereo volume low. Reduce settings if you hear distortion.",14,MUTED,false);
  add(q,"Android may prevent VoluAmp from amplifying audio from Amazon Music, Spotify, and other apps. Software cannot increase the phone's physical AUX output power.",13,MUTED,false);
  add(q,"VoluAmp 2.0 prototype • A product of LIT America",12,MUTED,false);
 }
 void toggleEffects(){if(effects){releaseEffects();return;}
  try {eq=new Equalizer(0,0);eq.setEnabled(true);}catch(Exception e){eq=null;}
  try {bassFx=new BassBoost(0,0);bassFx.setEnabled(true);}catch(Exception e){bassFx=null;}
  effects=eq!=null||bassFx!=null;applyEffects();
  if(!effects)Toast.makeText(this,"System audio effects unavailable on this device",Toast.LENGTH_LONG).show();
 }
 void applyEffects(){
  try{if(bassFx!=null)bassFx.setStrength((short)(bassValue*10));}catch(Exception ignored){}
  try{if(eq!=null){short upper=eq.getBandLevelRange()[1];short level=(short)Math.min(upper,eqValue*1000);for(short i=0;i<eq.getNumberOfBands();i++)eq.setBandLevel(i,level);}}catch(Exception ignored){}
 }
 void releaseEffects(){if(eq!=null){eq.release();eq=null;}if(bassFx!=null){bassFx.release();bassFx=null;}effects=false;}
 @Override protected void onDestroy(){releaseEffects();super.onDestroy();}
}
