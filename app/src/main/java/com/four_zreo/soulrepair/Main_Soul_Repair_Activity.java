package com.four_zreo.soulrepair;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class Main_Soul_Repair_Activity extends AppCompatActivity {
    private ImageButton btnColors;
    private ImageButton btnMusics;
    private TextView textColors;
    private TextView textMusics;

    private View cardColors;
    private View cardMusics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__soul__repair_);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnColors = findViewById(R.id.imageButton_Colors);
        btnMusics = findViewById(R.id.imageButton_Musics);
        textColors = findViewById(R.id.textViewColors);
        textMusics = findViewById(R.id.textViewMusics);
        cardColors = findViewById(R.id.cardColors);
        cardMusics = findViewById(R.id.cardMusics);

        btnColors.setBackgroundResource(R.drawable.soul_repair_layout_colors1000);
        btnMusics.setBackgroundResource(R.drawable.soul_repair_layout_musics1000);
        
        textColors.setText(R.string.Colors);
        textMusics.setText(R.string.Musics);
    }

    private void setupListeners() {
        View.OnClickListener colorsListener = v -> {
            Intent i = new Intent(Main_Soul_Repair_Activity.this, Colors_Activity.class);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        };

        View.OnClickListener musicsListener = v -> {
            Intent i = new Intent(Main_Soul_Repair_Activity.this, Musics_Activity.class);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        };

        btnColors.setOnClickListener(colorsListener);
        cardColors.setOnClickListener(colorsListener);
        btnMusics.setOnClickListener(musicsListener);
        cardMusics.setOnClickListener(musicsListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.About);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            showAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        View btnClose = dialogView.findViewById(R.id.dialog_button_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
