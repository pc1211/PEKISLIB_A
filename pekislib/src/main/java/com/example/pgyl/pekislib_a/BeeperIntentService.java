package com.example.pgyl.pekislib_a;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;

public class BeeperIntentService extends IntentService {

    public BeeperIntentService() {
        super(BeeperIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final int DURATION_TONE_CDMA_ALERT_AUTOREDIAL_LITE = 434;   //  7 * 62 ms

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int initVolume = audioManager.getStreamVolume(audioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE);
        synchronized (toneGenerator) {
            try {
                toneGenerator.wait(DURATION_TONE_CDMA_ALERT_AUTOREDIAL_LITE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            toneGenerator.release();
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, initVolume, 0);
        }
    }
}
