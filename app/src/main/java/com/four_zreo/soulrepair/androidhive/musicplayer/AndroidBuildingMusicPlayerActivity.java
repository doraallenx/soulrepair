package com.four_zreo.soulrepair.androidhive.musicplayer;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.four_zreo.soulrepair.R;
import com.four_zreo.soulrepair.paul.arian.fileselector.FileSelectionActivity;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class AndroidBuildingMusicPlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {
    private ImageButton btnBackward, btnForward, btnNext, btnPrevious, btnPlaylist, btnRepeat, btnShuffle;
    private View btnAddCharacter;
    private FloatingActionButton btnPlay;
    private ImageView imCover;
    private MediaPlayer mp;
    private TextView songCurrentDurationLabel, songTitleLabel, songTotalDurationLabel;
    private SongsManager songManager;
    private SeekBar songProgressBar;
    private Utilities utils;
    private Drawable customThumbNormal, customThumbPressed;
    
    private Handler mHandler = new Handler();
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<>();
    
    private int seekForwardTime = 5000;
    private int seekBackwardTime = 5000;
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    public boolean m_bThreadStop = false;

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            if (songsList != null && !m_bThreadStop && mp != null && mp.isPlaying()) {
                long totalDuration = mp.getDuration();
                long currentDuration = mp.getCurrentPosition();
                songTotalDurationLabel.setText(utils.milliSecondsToTimer(totalDuration));
                songCurrentDurationLabel.setText(utils.milliSecondsToTimer(currentDuration));
                int progress = utils.getProgressPercentage(currentDuration, totalDuration);
                songProgressBar.setProgress(progress);
                mHandler.postDelayed(this, 100);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);

        initToolbar();
        initViews();
        loadSavedThumb(); // 載入上次儲存的圖片
        setupMediaPlayer();
        setupListeners();

        if (songsList != null && !songsList.isEmpty()) {
            playSong(0);
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // 停用系統預設標題，因為我們在 XML 中使用了自定義置中的 TextView
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPlaylist = findViewById(R.id.btnPlaylist);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnAddCharacter = findViewById(R.id.btnAddCharacter);
        
        songProgressBar = findViewById(R.id.songProgressBar);
        songTitleLabel = findViewById(R.id.songTitle);
        songCurrentDurationLabel = findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = findViewById(R.id.songTotalDurationLabel);
        imCover = findViewById(R.id.album_art);
    }

    private void setupMediaPlayer() {
        mp = new MediaPlayer();
        songManager = new SongsManager();
        utils = new Utilities();
        songProgressBar.setOnSeekBarChangeListener(this);
        // Standard Android Seekbar look handled by XML drawable
        mp.setOnCompletionListener(this);
        songsList = songManager.getPlayList(this);
        if (songsList != null && !songsList.isEmpty()) {
            currentSongIndex = 0;
            songTitleLabel.setText(songsList.get(0).get("songTitle"));
        } else {
            songTitleLabel.setText(getString(R.string.no_songs_found));
        }
    }

    private void setupListeners() {
        btnPlay.setOnClickListener(v -> {
            if (songsList == null || songsList.isEmpty()) {
                Toast.makeText(this, getString(R.string.no_songs_found), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mp.isPlaying()) {
                mp.pause();
                btnPlay.setImageResource(R.drawable.ic_play_arrow);
            } else {
                try {
                    if (mp.getCurrentPosition() > 0) {
                        mp.start();
                        btnPlay.setImageResource(R.drawable.ic_pause);
                        updateProgressBar();
                    } else {
                        playSong(currentSongIndex);
                    }
                } catch (Exception e) {
                    playSong(currentSongIndex >= 0 ? currentSongIndex : 0);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (songsList != null && !songsList.isEmpty()) {
                currentSongIndex = (currentSongIndex + 1) % songsList.size();
                playSong(currentSongIndex);
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (songsList != null && !songsList.isEmpty()) {
                currentSongIndex = (currentSongIndex - 1 + songsList.size()) % songsList.size();
                playSong(currentSongIndex);
            }
        });

        btnRepeat.setOnClickListener(v -> {
            isRepeat = !isRepeat;
            isShuffle = isRepeat ? false : isShuffle;
            
            // 介面回饋
            if (isRepeat) {
                btnRepeat.setAlpha(1.0f);
            } else {
                btnRepeat.setAlpha(0.4f);
            }
            btnShuffle.setAlpha(0.4f);
            
            Toast.makeText(this, isRepeat ? getString(R.string.repeat_on) : getString(R.string.repeat_off), Toast.LENGTH_SHORT).show();
        });

        btnShuffle.setOnClickListener(v -> {
            isShuffle = !isShuffle;
            isRepeat = isShuffle ? false : isRepeat;
            
            // 介面回饋
            if (isShuffle) {
                btnShuffle.setAlpha(1.0f);
            } else {
                btnShuffle.setAlpha(0.4f);
            }
            btnRepeat.setAlpha(0.4f);
            
            Toast.makeText(this, isShuffle ? getString(R.string.shuffle_on) : getString(R.string.shuffle_off), Toast.LENGTH_SHORT).show();
        });

        btnPlaylist.setOnClickListener(v -> {
            Intent i = new Intent(this, PlayListActivity.class);
            startActivityForResult(i, 100);
        });

        btnAddCharacter.setOnClickListener(v -> showCharacterSelectionDialog());

        // 為大封面增加點擊功能：點一下即隨機換圖
        imCover.setOnClickListener(v -> updateCoverImage());
    }

    private void showCharacterSelectionDialog() {
        String[] options = {getString(R.string.pick_from_gallery), getString(R.string.standard_dot)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.seekbar_dot_title));
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), 200);
            } else {
                customThumbNormal = null;
                customThumbPressed = null;
                getSharedPreferences("Settings", MODE_PRIVATE).edit().putBoolean("useCustomThumb", false).apply();
                songProgressBar.setThumb(getResources().getDrawable(R.drawable.seekbar_thumb));
            }
        });
        builder.show();
    }

    public void playSong(int songIndex) {
        if (songsList == null || songsList.isEmpty() || songIndex < 0 || songIndex >= songsList.size()) {
            return;
        }
        currentSongIndex = songIndex;
        try {
            mp.reset();
            String songPath = songsList.get(songIndex).get("songPath");
            if (songPath != null && songPath.startsWith("content://")) {
                mp.setDataSource(this, Uri.parse(songPath));
            } else if (songPath != null) {
                File file = new File(songPath);
                if (file.exists()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        mp.setDataSource(fis.getFD());
                    }
                } else {
                    mp.setDataSource(songPath);
                }
            }
            mp.prepare();
            mp.start();
            
            songTitleLabel.setText(songsList.get(songIndex).get("songTitle"));
            btnPlay.setImageResource(R.drawable.ic_pause);
            
            updateCoverImage();
            
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);
            updateProgressBar();
        } catch (Exception e) {
            Log.e("MusicPlayer", "Error playing song: " + e.getMessage(), e);
            Toast.makeText(this, "無法播放音訊: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCoverImage() {
        String[] covers = {"blue_hybrid_black.jpg", "whole_colors.jpg", "white.jpg", "whitecrystal.jpg", "skyblue.jpg"};
        String selectedCover = covers[new Random().nextInt(covers.length)];
        
        try {
            Bitmap b = BitmapFactory.decodeStream(getAssets().open(selectedCover));
            imCover.setImageBitmap(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {}

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        if (customThumbPressed != null) {
            seekBar.setThumb(customThumbPressed);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        if (customThumbNormal != null) {
            seekBar.setThumb(customThumbNormal);
        }
        if (mp != null && (mp.isPlaying() || mp.getCurrentPosition() > 0)) {
            try {
                int totalDuration = mp.getDuration();
                int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
                mp.seekTo(currentPosition);
                updateProgressBar();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        if (songsList == null || songsList.isEmpty()) return;

        if (isRepeat) {
            playSong(currentSongIndex);
        } else if (isShuffle) {
            currentSongIndex = new Random().nextInt(songsList.size());
            playSong(currentSongIndex);
        } else {
            btnNext.performClick();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            currentSongIndex = data.getExtras().getInt("songIndex");
            playSong(currentSongIndex);
        } else if (requestCode == 200 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
                
                // 提高自定義圖示的清晰度與尺寸上限 (從 48dp 提升到 64dp)
                float density = getResources().getDisplayMetrics().density;
                int maxSize = (int) (64 * density); 
                
                int width = selectedImage.getWidth();
                int height = selectedImage.getHeight();
                
                int newWidth, newHeight;
                if (width > height) {
                    newWidth = maxSize;
                    newHeight = (int) (maxSize * ((float) height / width));
                } else {
                    newHeight = maxSize;
                    newWidth = (int) (maxSize * ((float) width / height));
                }
                
                // 使用更高的品質進行縮放，避免失真
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(selectedImage, newWidth, newHeight, true);
                
                // 使用智能去背演算法移除背景
                Bitmap transparentBitmap = removeBackgroundSmart(scaledBitmap);
                
                // 保存並套用
                saveThumbToInternalStorage(transparentBitmap);
                prepareThumbDrawables(transparentBitmap);

                songProgressBar.setThumb(customThumbNormal);
                // 確保 SeekBar 不會裁切掉較大的自選圖片
                songProgressBar.setThumbOffset(newWidth / 2); 

                getSharedPreferences("Settings", MODE_PRIVATE).edit().putBoolean("useCustomThumb", true).apply();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 載入上次儲存的自定義圖片
     */
    private void loadSavedThumb() {
        boolean useCustom = getSharedPreferences("Settings", MODE_PRIVATE).getBoolean("useCustomThumb", false);
        if (useCustom) {
            try {
                java.io.File file = new java.io.File(getFilesDir(), "custom_thumb.png");
                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    prepareThumbDrawables(bitmap);
                    songProgressBar.setThumb(customThumbNormal);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void prepareThumbDrawables(Bitmap bitmap) {
        customThumbNormal = new BitmapDrawable(getResources(), bitmap);
        int pressedWidth = (int) (bitmap.getWidth() * 1.25);
        int pressedHeight = (int) (bitmap.getHeight() * 1.25);
        Bitmap pressedBitmap = Bitmap.createScaledBitmap(bitmap, pressedWidth, pressedHeight, true);
        customThumbPressed = new BitmapDrawable(getResources(), pressedBitmap);
    }

    private void saveThumbToInternalStorage(Bitmap bitmap) {
        try {
            java.io.File file = new java.io.File(getFilesDir(), "custom_thumb.png");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 進階智能去背：使用 Flood Fill 演算法
     * 僅移除從邊緣連通的背景色，保留角色內部的相同顏色（如哆啦A夢的白色肚子）
     */
    private Bitmap removeBackgroundSmart(Bitmap bitmap) {
        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        
        int bgColor = bitmap.getPixel(0, 0);
        int threshold = 40;

        boolean[][] visited = new boolean[width][height];
        java.util.Queue<android.graphics.Point> queue = new java.util.LinkedList<>();

        // 從四個邊緣開始掃描
        for (int x = 0; x < width; x++) {
            addIfBackground(bitmap, x, 0, bgColor, threshold, visited, queue);
            addIfBackground(bitmap, x, height - 1, bgColor, threshold, visited, queue);
        }
        for (int y = 0; y < height; y++) {
            addIfBackground(bitmap, 0, y, bgColor, threshold, visited, queue);
            addIfBackground(bitmap, width - 1, y, bgColor, threshold, visited, queue);
        }

        // Flood Fill BFS
        while (!queue.isEmpty()) {
            android.graphics.Point p = queue.poll();
            output.setPixel(p.x, p.y, Color.TRANSPARENT);

            int[] dx = {1, -1, 0, 0};
            int[] dy = {0, 0, 1, -1};

            for (int i = 0; i < 4; i++) {
                int nx = p.x + dx[i];
                int ny = p.y + dy[i];
                addIfBackground(bitmap, nx, ny, bgColor, threshold, visited, queue);
            }
        }
        return output;
    }

    private void addIfBackground(Bitmap bitmap, int x, int y, int bgColor, int threshold, boolean[][] visited, java.util.Queue<android.graphics.Point> queue) {
        if (x < 0 || x >= bitmap.getWidth() || y < 0 || y >= bitmap.getHeight() || visited[x][y]) return;

        int color = bitmap.getPixel(x, y);
        int rDiff = Math.abs(Color.red(color) - Color.red(bgColor));
        int gDiff = Math.abs(Color.green(color) - Color.green(bgColor));
        int bDiff = Math.abs(Color.blue(color) - Color.blue(bgColor));

        if (rDiff < threshold && gDiff < threshold && bDiff < threshold) {
            visited[x][y] = true;
            queue.add(new android.graphics.Point(x, y));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        m_bThreadStop = true;
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }
}