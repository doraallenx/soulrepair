package com.four_zreo.soulrepair.androidhive.musicplayer;

/* JADX INFO: loaded from: classes.dex */
public class Utilities {
    public String milliSecondsToTimer(long milliseconds) {
        String secondsString;
        String finalTimerString = "";
        int hours = (int) (milliseconds / 3600000);
        int minutes = ((int) (milliseconds % 3600000)) / 60000;
        int seconds = (int) (((milliseconds % 3600000) % 60000) / 1000);
        if (hours > 0) {
            finalTimerString = String.valueOf(hours) + ":";
        }
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = new StringBuilder().append(seconds).toString();
        }
        return String.valueOf(finalTimerString) + minutes + ":" + secondsString;
    }

    public int getProgressPercentage(long currentDuration, long totalDuration) {
        if (totalDuration <= 0) return 0;
        return (int) ((((double) currentDuration) / totalDuration) * 100);
    }

    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = (int) ((((double) progress) / 100.0d) * ((double) (totalDuration / 1000)));
        return currentDuration * 1000;
    }
}
