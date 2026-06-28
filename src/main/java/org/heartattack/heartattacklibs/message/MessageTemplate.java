package org.heartattack.heartattacklibs.message;

import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;

public final class MessageTemplate {
    private final List<String> lines = new ArrayList<>();
    private String key;
    private String actionbar;
    private String title;
    private String subtitle;
    private int fadeIn = 10;
    private int stay = 40;
    private int fadeOut = 10;
    private Sound sound;
    private float volume = 1.0f;
    private float pitch = 1.0f;

    public String key() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> lines() {
        return lines;
    }

    public String actionbar() {
        return actionbar;
    }

    public void setActionbar(String actionbar) {
        this.actionbar = actionbar;
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String subtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int fadeIn() {
        return fadeIn;
    }

    public void setFadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
    }

    public int stay() {
        return stay;
    }

    public void setStay(int stay) {
        this.stay = stay;
    }

    public int fadeOut() {
        return fadeOut;
    }

    public void setFadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
    }

    public Sound sound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    public float volume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float pitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
