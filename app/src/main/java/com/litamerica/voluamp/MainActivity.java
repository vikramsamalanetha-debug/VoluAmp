package com.litamerica.voluamp;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.BassBoost;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import java.io.IOException;

public class MainActivity extends Activity {
 private static final int PICK_AUDIO = 5;
 private LinearLayout root;
 private TextView status;
 private SeekBar gain, bass;
 private AudioManager audio;
 private Equalizer globalEq, playerEq;
 private BassBoost globalBass, playerBass;
 private MediaPlayer player;
 private float boost = 0f;
 private boolean globalOn = false;
 @Override public void onCreate(Bundle state) { super.onCreate(state);
  audio = (AudioManager)getSystemService(AUDIO_SERVICE);
  root = new LinearLayout(this); root.setOrientation(1); root.setPadding(28,32,28,20); root.setBackgroundColor(Color.rgb(14,22,34));
  ScrollView scroll = new ScrollView(this); scroll.addView(root); setContentView(scroll);
  label("VoluAmp",26,Color.WHITE);
  label("LIT America • Audio enhancer",14,0xff91a7c1);
  label("Phone media volume",18,Color.WHITE);
  SeekBar volume = seek(0, audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), audio.getStreamVolume(AudioManager.STREAM_MUSIC));
  volume.setOnSeekBarChangeListener(listener(v -> audio.setStreamVolume(AudioManager.STREAM_MUSIC,v,0)));
  button("Try system audio effects (experimental)",v -> toggleGlobal());
  label("Android may prevent effects from applying to other music apps. If you hear no change, this is a device restriction.",14,0xffb8c5d6);
  label("Bass enhancement",18,Color.WHITE);
  bass = seek(0,100,25); bass.setOnSeekBarChangeListener(listener(v -> updateBass()));
  label("Built-in player — local music",18,Color.WHITE);
  button("Choose an audio file",v -> { Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT); i.setType("audio/*"); i.addCategory(Intent.CATEGORY_OPENABLE); startActivityForResult(i,PICK_AUDIO); });
  label("Player EQ lift (0–8 dB where supported; may distort)",16,Color.WHITE);
  gain = seek(0,8,0); gain.setOnSeekBarChangeListener(listener(v -> { boost=v; updatePlayerGain(); }));
  button("Play / Pause",v -> { if(player!=null) { if(player.isPlaying()) player.pause(); else player.start(); } });
  button("Stop",v -> stopPlayer());
  status = label("Ready. Connect your phone to the car AUX input and start at low volume.",14,0xff9dddbb);
  label("Safety: Set car stereo low before adjusting. If you hear crackling, lower EQ lift. An AUX signal may require a hardware line amplifier.",13,0xffe6c38b);
 }
 private TextView label(String text,int sp,int color) { TextView t=new TextView(this); t.setText(text); t.setTextSize(sp); t.setTextColor(color); t.setPadding(0,14,0,12); root.addView(t); return t; }
 private SeekBar seek(int min,int max,int value) { SeekBar s = new SeekBar(this); s.setMax(max); s.setMin(min); s.setProgress(value); root.addView(s); return s; }
 private void button(String text,View.OnClickListener action) { Button b=new Button(this); b.setText(text); b.setOnClickListener(action); root.addView(b); }
 interface Changed { void changed(int value); }
 private SeekBar.OnSeekBarChangeListener listener(Changed fn) { return new SeekBar.OnSeekBarChangeListener() { public void onProgressChanged(SeekBar b,int v,boolean user){fn.changed(v);} public void onStartTrackingTouch(SeekBar b){} public void onStopTrackingTouch(SeekBar b){} }; }
 private void toggleGlobal() { if(globalOn){ releaseGlobal(); status.setText("System EQ disabled."); return; }
  try { globalEq = new Equalizer(0,0); globalEq.setEnabled(true); globalBass = new BassBoost(0,0); globalBass.setEnabled(true); globalOn=true; updateBass(); status.setText("Experimental effects enabled. Other apps may ignore them."); }
  catch(Exception e) { releaseGlobal(); status.setText("System-wide effects unavailable on this device: " + e.getClass().getSimpleName()); }
 }
 private void updateBass() { short strength=(short)(bass.getProgress()*10); try { if(globalBass!=null) globalBass.setStrength(strength); if(playerBass!=null) playerBass.setStrength(strength); } catch(Exception ignored) {} }
 private void updatePlayerGain() { if(player!=null) {
  try { if(playerEq!=null) { short max=playerEq.getBandLevelRange()[1]; short level=(short)Math.min(max,Math.round(boost*1000)); for(short b=0;b<playerEq.getNumberOfBands();b++) playerEq.setBandLevel(b,level); } } catch(Exception ignored) {} }
 }
 @Override protected void onActivityResult(int request,int result,Intent data) { super.onActivityResult(request,result,data);
  if(request==PICK_AUDIO && result==RESULT_OK && data!=null) { Uri uri=data.getData(); stopPlayer();
   try { player=new MediaPlayer(); player.setDataSource(this,uri); player.setAudioStreamType(AudioManager.STREAM_MUSIC); player.prepare();
    try { playerEq=new Equalizer(0,player.getAudioSessionId()); playerEq.setEnabled(true); } catch(Exception ignored) {}
    try { playerBass=new BassBoost(0,player.getAudioSessionId()); playerBass.setEnabled(true); } catch(Exception ignored) {}
    updatePlayerGain(); updateBass(); player.start(); status.setText("Playing local audio. EQ lift may vary by device and can cause distortion.");
   } catch(IOException | RuntimeException e) { stopPlayer(); status.setText("Could not play this audio file."); }
  }
 }
 private void stopPlayer() { if(playerEq!=null){playerEq.release();playerEq=null;} if(playerBass!=null){playerBass.release();playerBass=null;} if(player!=null){player.release();player=null;} }
 private void releaseGlobal(){ if(globalEq!=null){globalEq.release();globalEq=null;} if(globalBass!=null){globalBass.release();globalBass=null;} globalOn=false; }
 @Override protected void onDestroy(){stopPlayer();releaseGlobal();super.onDestroy();}
}
