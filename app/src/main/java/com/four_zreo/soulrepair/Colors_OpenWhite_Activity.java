package com.four_zreo.soulrepair;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Colors_OpenWhite_Activity extends AppCompatActivity {
    Bitmap bitmap;
    ImageView imageview;
    FloatingActionButton fabBack;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageviewlayout);

        this.fabBack = findViewById(R.id.fab_back);
        this.fabBack.setOnClickListener(v -> onBackPressed());

        this.imageview = (ImageView) findViewById(R.id.imageViewGeneralUse);
        
        // 點擊圖片或左下角標籤切換
        View.OnClickListener toggleListener = v -> toggleImage();
        this.imageview.setOnClickListener(toggleListener);
        
        View cardChangePic = findViewById(R.id.card_change_pic);
        if (cardChangePic != null) {
            cardChangePic.setVisibility(View.VISIBLE); // 只在這個 Activity 顯示
            cardChangePic.setOnClickListener(toggleListener);
        }

        loadMainImage();
    }

    private void loadMainImage() {
        AssetManager assets = getAssets();
        InputStream buffer = null;
        try {
            buffer = new BufferedInputStream(assets.open("white.jpg"));
            this.bitmap = BitmapFactory.decodeStream(buffer);
            this.imageview.setImageBitmap(this.bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (buffer != null) {
                try { buffer.close(); } catch (IOException ignored) {}
            }
        }
    }

    private void toggleImage() {
        // 在相關的療癒圖片間切換
        String[] whiteVariations = {"white.jpg", "whitecrystal.jpg"};
        this.index = (this.index + 1) % whiteVariations.length;
        
        AssetManager assets = getAssets();
        try {
            InputStream buffer = new BufferedInputStream(assets.open(whiteVariations[this.index]));
            Bitmap newBitmap = BitmapFactory.decodeStream(buffer);
            this.imageview.setImageBitmap(newBitmap);
            if (this.bitmap != null) this.bitmap.recycle();
            this.bitmap = newBitmap;
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.bitmap != null) {
            this.bitmap.recycle();
            this.bitmap = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
