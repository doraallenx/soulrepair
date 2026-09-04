package com.four_zreo.soulrepair;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Colors_OpenBlackHybridBlue_Activity extends AppCompatActivity {
    Bitmap bitmap;
    ImageView imageview;
    FloatingActionButton fabBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 在 setContentView 之前設定全螢幕，這是 API 7 的標準做法
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageviewlayout);

        this.fabBack = findViewById(R.id.fab_back);
        this.fabBack.setOnClickListener(v -> onBackPressed());

        this.imageview = (ImageView) findViewById(R.id.imageViewGeneralUse);
        
        // 載入圖片
        AssetManager assets = getAssets();
        InputStream buffer = null;
        try {
            buffer = new BufferedInputStream(assets.open("blue_hybrid_black.jpg"));
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
