package com.four_zreo.soulrepair.androidhive.musicplayer;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.four_zreo.soulrepair.R;
import java.util.ArrayList;
import java.util.HashMap;

public class PlayListActivity extends AppCompatActivity {
    public ArrayList<HashMap<String, String>> songsList = new ArrayList<>();
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);

        Toolbar toolbar = findViewById(R.id.toolbar_playlist);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ArrayList<HashMap<String, String>> songsListData = new ArrayList<>();
        SongsManager plm = new SongsManager();
        this.songsList = plm.getPlayList(this);
        
        lv = findViewById(android.R.id.list);
        
        if (this.songsList != null) {
            for (int i = 0; i < this.songsList.size(); i++) {
                HashMap<String, String> song = this.songsList.get(i);
                songsListData.add(song);
            }
            ListAdapter adapter = new SimpleAdapter(this, songsListData, R.layout.playlist_item, new String[]{"songTitle"}, new int[]{R.id.songTitle});
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent in = new Intent();
                    in.putExtra("songIndex", position);
                    setResult(100, in);
                    finish();
                }
            });
        }
    }
}
