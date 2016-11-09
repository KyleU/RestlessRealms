package restless.realms.client.audio;

import java.util.HashMap;
import java.util.Map;

import restless.realms.client.ClientManager;
import restless.realms.client.console.ConsoleUtils;

import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.SoundController;
import com.allen_sauer.gwt.voices.client.handler.PlaybackCompleteEvent;
import com.allen_sauer.gwt.voices.client.handler.SoundHandler;
import com.allen_sauer.gwt.voices.client.handler.SoundLoadStateChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class AudioManager {
    private static Map<String, Sound> sounds;
    private static Map<String, Sound> music;
    private static SoundController soundController;
    private static boolean soundsEnabled;
    private static Image soundsImage;
    private static boolean musicEnabled;
    private static Image musicImage;
    private static String activeMusic;
    
    
    public static void initImages() {
        sounds = new HashMap<String, Sound>();
        music = new HashMap<String, Sound>();

        soundsEnabled = true;        
        soundsImage = new Image("img/icon/audio.png", 0, 0, 28, 29);
        soundsImage.setTitle("Sound Effects");
        soundsImage.setStylePrimaryName("enablesound");
        soundsImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(soundsEnabled) {
                    soundsImage.setVisibleRect(0, 29, 28, 29);
                } else {
                    soundsImage.setVisibleRect(0, 0, 28, 29);
                }
                soundsEnabled = !soundsEnabled;
            }
        });
        RootPanel.get("pageheader").add(soundsImage);

        musicEnabled = false;
        musicImage = new Image("img/icon/audio.png", 28, 29, 28, 29);
        musicImage.setTitle("Music");
        musicImage.setStylePrimaryName("enablemusic");
        musicImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(musicEnabled) {
                    musicImage.setVisibleRect(28, 29, 28, 29);
                } else {
                    musicImage.setVisibleRect(28, 0, 28, 29);
                }
                musicEnabled = !musicEnabled;
                if(musicEnabled) {
                    String perspective = ClientManager.getActivePerspective();
                    if(perspective.equals("play")) {
                        playMusic("theme");
                    } else if(perspective.equals("adventure") || perspective.equals("combat")) {
                        playMusic("adventure");
                    } else {
                        assert false : perspective;
                    }
                } else {
                    if(activeMusic != null) {
                        Sound sound = music.get(activeMusic);
                        sound.stop();
                        activeMusic = null;
                    }
                }
            }
        });
        RootPanel.get("pageheader").add(musicImage);

        soundController = new SoundController();
    }
    
    public static void playMusic(String key) {
        if(musicEnabled) {
            if(!key.equals(activeMusic)) {
                if(activeMusic != null) {
                    Sound sound = music.get(activeMusic);
                    sound.stop();
                }
                activeMusic = key;
                Sound sound = music.get(key);
                if(sound == null) {
                    sound = soundController.createSound(Sound.MIME_TYPE_AUDIO_MPEG, "audio/" + key + ".mp3", true);
                    final Sound finalSound = sound;
                    sound.addEventHandler(new SoundHandler() {
                        @Override
                        public void onSoundLoadStateChange(SoundLoadStateChangeEvent event) {
                        }
                        
                        @Override
                        public void onPlaybackComplete(PlaybackCompleteEvent event) {
                            finalSound.play();
                        }
                    });
                    music.put(key, sound);
                }
                assert sound != null;
                try {
                    sound.play();
                } catch(Exception e) {
                    ConsoleUtils.error("Error playing sound.", e);
                }
            }
        }
    }
    
    public static void play(String key) {
        if(soundsEnabled) {
            Sound sound = sounds.get(key);
            if(sound == null) {
                try {
                    sound = soundController.createSound(Sound.MIME_TYPE_AUDIO_MPEG, "audio/" + key + ".mp3");
                    sounds.put(key, sound);
                } catch(Exception e) {
                    ConsoleUtils.error("Error loading sound.", e);
                }                
            }
            assert sound != null;
            try {
                sound.play();
            } catch(Exception e) {
                ConsoleUtils.error("Error playing sound.", e);
            }
        }
    }
}
