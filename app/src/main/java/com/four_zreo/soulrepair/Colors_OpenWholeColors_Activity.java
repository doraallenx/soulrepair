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

public class Colors_OpenWholeColors_Activity extends AppCompatActivity {
    Bitmap bitmap;
    ImageView imageview;
    FloatingActionButton fabBack;

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
        
        AssetManager assets = getAssets();
        InputStream buffer = null;
        try {
            buffer = new BufferedInputStream(assets.open("whole_colors.jpg"));
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
