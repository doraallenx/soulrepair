package com.four_zreo.soulrepair;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

public class Colors_Activity extends AppCompatActivity {
    ImageButton btnBalckHybridBlue;
    ImageButton btnSkyBlue;
    ImageButton btnWhite;
    ImageButton btnWholeColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.colors_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBalckHybridBlue = findViewById(R.id.imageBlueBlackPreview);
        btnWholeColors = findViewById(R.id.imageWholeColorPreview);
        btnWhite = findViewById(R.id.imageWhitePreview);
        btnSkyBlue = findViewById(R.id.imageSkyBluePreview);
    }

    private void setupListeners() {
        btnBalckHybridBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Colors_Activity.this, Colors_OpenBlackHybridBlue_Activity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        btnWholeColors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Colors_Activity.this, Colors_OpenWholeColors_Activity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        btnWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Colors_Activity.this, Colors_OpenWhite_Activity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        btnSkyBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Colors_Activity.this, Colors_OpenLightBlue_Activity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
