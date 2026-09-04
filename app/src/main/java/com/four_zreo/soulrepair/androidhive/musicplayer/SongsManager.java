package com.four_zreo.soulrepair.androidhive.musicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SongsManager {
    private static final String TAG = "SongsManager";
    private int nFileListNumers = 0;
    final String MEDIA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoulRepair_MusicBox";
    File bMusicFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoulRepair_MusicBox");

    public SongsManager() {
        try {
            if (!this.bMusicFolder.exists()) {
                this.bMusicFolder.mkdirs();
            }
            if (this.bMusicFolder.listFiles() != null) {
                this.nFileListNumers = this.bMusicFolder.listFiles().length;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing bMusicFolder", e);
        }
    }

    public int getPlaySongsNumber() {
        return this.nFileListNumers;
    }

    public void ReScanFolder() {
        Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        intent.setData(Uri.fromFile(this.bMusicFolder));
    }

    public void UpdatePlaySongsNumber() {
        if (this.bMusicFolder != null && this.bMusicFolder.listFiles() != null) {
            this.nFileListNumers = this.bMusicFolder.listFiles().length;
        }
    }

    public ArrayList<HashMap<String, String>> getPlayList() {
        return getPlayList(null);
    }

    public ArrayList<HashMap<String, String>> getPlayList(Context context) {
        ArrayList<HashMap<String, String>> songsList = new ArrayList<>();
        Set<String> addedPaths = new HashSet<>();

        // 1. Query system MediaStore (covers /storage/emulated/0/Music and all device audio)
        if (context != null) {
            try {
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DISPLAY_NAME
                };
                String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
                ContentResolver cr = context.getContentResolver();
                try (Cursor cursor = cr.query(uri, projection, selection, null, MediaStore.Audio.Media.TITLE + " ASC")) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                        int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                        int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                        int nameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                        do {
                            long id = cursor.getLong(idCol);
                            String path = cursor.getString(dataCol);
                            if (path != null && !addedPaths.contains(path)) {
                                Uri contentUri = android.content.ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                                String title = cursor.getString(titleCol);
                                if (title == null || title.trim().isEmpty()) {
                                    String displayName = nameCol != -1 ? cursor.getString(nameCol) : "";
                                    if (displayName != null && displayName.contains(".")) {
                                        title = displayName.substring(0, displayName.lastIndexOf('.'));
                                    } else {
                                        title = displayName;
                                    }
                                }
                                if (title == null || title.trim().isEmpty()) {
                                    File f = new File(path);
                                    title = f.getName();
                                }
                                HashMap<String, String> song = new HashMap<>();
                                song.put("songTitle", title);
                                song.put("songPath", contentUri.toString());
                                song.put("filePath", path);
                                songsList.add(song);
                                addedPaths.add(path);
                            }
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error querying MediaStore", e);
            }
        }

        // 2. Also check SoulRepair_MusicBox directory
        File home = new File(this.MEDIA_PATH);
        if (home.exists() && home.listFiles(new FileExtensionFilter()) != null) {
            for (File file : home.listFiles(new FileExtensionFilter())) {
                if (!addedPaths.contains(file.getPath())) {
                    HashMap<String, String> song = new HashMap<>();
                    String name = file.getName();
                    String title = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
                    song.put("songTitle", title);
                    song.put("songPath", file.getPath());
                    songsList.add(song);
                    addedPaths.add(file.getPath());
                }
            }
        }

        // 3. Also check App External Files directory
        if (context != null) {
            File appMusicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            if (appMusicDir != null && appMusicDir.exists() && appMusicDir.listFiles(new FileExtensionFilter()) != null) {
                for (File file : appMusicDir.listFiles(new FileExtensionFilter())) {
                    if (!addedPaths.contains(file.getPath())) {
                        HashMap<String, String> song = new HashMap<>();
                        String name = file.getName();
                        String title = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
                        song.put("songTitle", title);
                        song.put("songPath", file.getPath());
                        songsList.add(song);
                        addedPaths.add(file.getPath());
                    }
                }
            }
        }

        this.nFileListNumers = songsList.size();
        return songsList;
    }

    class FileExtensionFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            String lower = name.toLowerCase();
            return lower.endsWith(".mp3") || lower.endsWith(".m4a") || lower.endsWith(".wav") || lower.endsWith(".ogg") || lower.endsWith(".flac") || lower.endsWith(".aac");
        }
    }
}
