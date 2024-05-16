package app;

import java.io.File;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import app.Logger.Severity;

/**
 * Mixer
 */
public class Mixer {

    private Clip clip = null;
    private static String soundURL[] = {
        "resources/audio/coin.wav",
        "resources/audio/doll.wav",
        "resources/audio/explosion.wav",
        "resources/audio/hit.wav",
        "resources/audio/running.wav"
    };


    private void setFile(int i) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(soundURL[i]));
            if (ais == null) {
                Logger.log(Severity.WARNING, "Mixer", "Could not get sound file: " + soundURL[i]);
            }
            clip = AudioSystem.getClip();
            if (clip == null) {
                Logger.log(Severity.WARNING, "Mixer", "Could not get clip.");
            }
            clip.open(ais);
        } catch (Exception e) {
            Logger.log(Severity.WARNING, "Mixer", "Could not load file: " + e);
        }
    }

    public void stop() {
        if (clip == null) return;
        clip.stop();
    }

    public void loop() {
        if (clip == null) return;
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void play(int i) {
        setFile(i);
        if (clip == null) return;
        clip.start();
    }
}
