package com.four_zreo.soulrepair;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.four_zreo.soulrepair.androidhive.musicplayer.AndroidBuildingMusicPlayerActivity;
import com.four_zreo.soulrepair.paul.arian.fileselector.FileSelectionActivity;

import java.io.File;

public class Musics_Activity extends AppCompatActivity {
    private RelativeLayout RAlreadyGonelayout;
    private View RMusic4_1layout;
    private View RMusic4_2layout;
    private RelativeLayout RHomelayout;
    private RelativeLayout RInvinciblelayout;
    private RelativeLayout RUnmistakablelayout;
    private File bMusicFolder;
    private ImageButton btnMusic1, btnMusic2, btnMusic3, btnMusic4, btnMusic4_2, btnMusic5;
    private View cardMusicBox, cardImport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.musics_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        initViews();
        setupDirectory();
        setupListeners();
    }

    private void initViews() {
        btnMusic1 = findViewById(R.id.imageButtonMusic1);
        btnMusic2 = findViewById(R.id.imageButtonMusic2);
        btnMusic3 = findViewById(R.id.imageButtonMusic3);
        btnMusic4 = findViewById(R.id.imageButtonMusic4);
        btnMusic4_2 = findViewById(R.id.imageButtonMusic4_2);
        btnMusic5 = findViewById(R.id.imageButtonMusic5);

        RUnmistakablelayout = findViewById(R.id.Unmistakable_BackStreetBoys);
        RAlreadyGonelayout = findViewById(R.id.Already_Gone_KellyClarkson);
        RInvinciblelayout = findViewById(R.id.Invincible_TinieTempah_feat_KellyRowland);
        RMusic4_1layout = findViewById(R.id.layout_music4_1);
        RMusic4_2layout = findViewById(R.id.layout_music4_2);
        RHomelayout = findViewById(R.id.Home_Daughtry);

        cardMusicBox = findViewById(R.id.cardMusicBox);
        cardImport = findViewById(R.id.cardImport);
    }

    private void setupDirectory() {
        this.bMusicFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoulRepair_MusicBox");
        if (!this.bMusicFolder.exists()) {
            this.bMusicFolder.mkdirs();
        }
    }

    private void setupListeners() {
        // YouTube play buttons
        btnMusic1.setOnClickListener(v -> watchYoutubeVideo("VG-toSj6dK4"));
        btnMusic2.setOnClickListener(v -> watchYoutubeVideo("TiQrpbOY4fg"));
        btnMusic3.setOnClickListener(v -> watchYoutubeVideo("r-_CsKFFfVw"));
        btnMusic4.setOnClickListener(v -> watchYoutubeVideo("kVnecr5a1Sc"));
        btnMusic4_2.setOnClickListener(v -> watchYoutubeVideo("u8xhJ6F5pAM"));
        btnMusic5.setOnClickListener(v -> watchYoutubeVideo("1gSOtFfA5o4"));

        // Row clicks (optional: same as buttons)
        RUnmistakablelayout.setOnClickListener(v -> btnMusic1.performClick());
        RAlreadyGonelayout.setOnClickListener(v -> btnMusic2.performClick());
        RInvinciblelayout.setOnClickListener(v -> btnMusic3.performClick());
        if (RMusic4_1layout != null) RMusic4_1layout.setOnClickListener(v -> btnMusic4.performClick());
        if (RMusic4_2layout != null) RMusic4_2layout.setOnClickListener(v -> btnMusic4_2.performClick());
        RHomelayout.setOnClickListener(v -> btnMusic5.performClick());

        // New Card Buttons
        cardMusicBox.setOnClickListener(v -> {
            Intent i = new Intent(this, AndroidBuildingMusicPlayerActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        cardImport.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                startFileSelection();
            } else {
                requestStoragePermission();
            }
        });
    }

    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 100);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    private void startFileSelection() {
        Intent i = new Intent(this, FileSelectionActivity.class);
        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startFileSelection();
        }
    }

    public void watchYoutubeVideo(String id) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + id));
            startActivity(intent);
        }
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
