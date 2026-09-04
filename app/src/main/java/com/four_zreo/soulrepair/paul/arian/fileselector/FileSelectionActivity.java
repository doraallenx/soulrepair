package com.four_zreo.soulrepair.paul.arian.fileselector;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.four_zreo.soulrepair.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FileSelectionActivity extends AppCompatActivity {
    private static final String TAG = "FileSelection";
    private static final int REQUEST_SYSTEM_PICKER = 300;

    private ListView listView;
    private View emptyView;
    private View btnImportSubmit;
    private TextView txtImportButtonLabel;
    private TextView btnSelectAll;
    private TextView txtScanHeader;
    private TextView folderpath;
    private View cardBrowseSystem;

    private final List<AudioItem> audioItemList = new ArrayList<>();
    private AudioAdapter adapter;
    private boolean isAllSelected = false;

    public static class AudioItem {
        public String title;
        public String path;
        public Uri uri;
        public long sizeBytes;
        public String format;
        public boolean isChecked;

        public AudioItem(String title, String path, Uri uri, long sizeBytes, String format) {
            this.title = title;
            this.path = path;
            this.uri = uri;
            this.sizeBytes = sizeBytes;
            this.format = format;
            this.isChecked = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selection);

        initViews();
        setupToolbar();
        setupListeners();
        loadAudioFiles();
    }

    private void initViews() {
        listView = findViewById(R.id.directorySelectionList);
        emptyView = findViewById(R.id.emptyView);
        btnImportSubmit = findViewById(R.id.btnImportSubmit);
        txtImportButtonLabel = findViewById(R.id.txtImportButtonLabel);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        txtScanHeader = findViewById(R.id.txtScanHeader);
        folderpath = findViewById(R.id.folderpath);
        cardBrowseSystem = findViewById(R.id.cardBrowseSystem);

        adapter = new AudioAdapter();
        listView.setAdapter(adapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_import);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        btnSelectAll.setOnClickListener(v -> {
            isAllSelected = !isAllSelected;
            for (AudioItem item : audioItemList) {
                item.isChecked = isAllSelected;
            }
            btnSelectAll.setText(isAllSelected ? "取消全選" : "全選");
            adapter.notifyDataSetChanged();
            updateImportButtonState();
        });

        cardBrowseSystem.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "選擇音樂檔案"), REQUEST_SYSTEM_PICKER);
        });

        btnImportSubmit.setOnClickListener(v -> importSelectedAudios());
    }

    private void loadAudioFiles() {
        audioItemList.clear();
        Set<String> addedPaths = new HashSet<>();

        // 1. Query MediaStore (System audio library)
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE
            };
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            ContentResolver cr = getContentResolver();
            try (Cursor cursor = cr.query(uri, projection, selection, null, MediaStore.Audio.Media.TITLE + " ASC")) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                    int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                    int nameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                    int sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);

                    do {
                        long id = cursor.getLong(idCol);
                        String path = cursor.getString(dataCol);
                        if (path != null && !addedPaths.contains(path)) {
                            Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                            String title = cursor.getString(titleCol);
                            String displayName = nameCol != -1 ? cursor.getString(nameCol) : "";
                            if (title == null || title.trim().isEmpty()) {
                                title = displayName != null && displayName.contains(".") 
                                        ? displayName.substring(0, displayName.lastIndexOf('.')) 
                                        : displayName;
                            }
                            if (title == null || title.trim().isEmpty()) {
                                title = new File(path).getName();
                            }
                            long size = sizeCol != -1 ? cursor.getLong(sizeCol) : 0;
                            String format = extractFormat(displayName != null && !displayName.isEmpty() ? displayName : path);

                            audioItemList.add(new AudioItem(title, path, contentUri, size, format));
                            addedPaths.add(path);
                        }
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying MediaStore", e);
        }

        // 2. Direct scan in /Music folder as fallback
        try {
            File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            if (musicDir != null && musicDir.exists()) {
                File[] files = musicDir.listFiles((dir, name) -> {
                    String lower = name.toLowerCase();
                    return lower.endsWith(".mp3") || lower.endsWith(".m4a") || lower.endsWith(".wav") || lower.endsWith(".flac") || lower.endsWith(".ogg") || lower.endsWith(".aac");
                });
                if (files != null) {
                    for (File f : files) {
                        if (!addedPaths.contains(f.getAbsolutePath())) {
                            String name = f.getName();
                            String title = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
                            String format = extractFormat(name);
                            audioItemList.add(new AudioItem(title, f.getAbsolutePath(), Uri.fromFile(f), f.length(), format));
                            addedPaths.add(f.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scanning music folder", e);
        }

        adapter.notifyDataSetChanged();
        updateUIState();
    }

    private String extractFormat(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toUpperCase(Locale.US);
        }
        return "AUDIO";
    }

    private void updateUIState() {
        if (audioItemList.isEmpty()) {
            listView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            txtScanHeader.setText("裝置上的音訊檔案 (0 首)");
        } else {
            listView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            txtScanHeader.setText("裝置上的音訊檔案 (" + audioItemList.size() + " 首)");
        }
        updateImportButtonState();
    }

    private void updateImportButtonState() {
        int selectedCount = 0;
        for (AudioItem item : audioItemList) {
            if (item.isChecked) {
                selectedCount++;
            }
        }

        if (selectedCount > 0) {
            btnImportSubmit.setVisibility(View.VISIBLE);
            txtImportButtonLabel.setText("匯入選取的歌曲 (" + selectedCount + " 首)");
            btnImportSubmit.setAlpha(1.0f);
        } else {
            txtImportButtonLabel.setText("匯入選取的歌曲 (0)");
            btnImportSubmit.setAlpha(0.6f);
        }
    }

    private void importSelectedAudios() {
        List<AudioItem> toImport = new ArrayList<>();
        for (AudioItem item : audioItemList) {
            if (item.isChecked) {
                toImport.add(item);
            }
        }

        if (toImport.isEmpty()) {
            Toast.makeText(this, "請先勾選欲匯入的歌曲", Toast.LENGTH_SHORT).show();
            return;
        }

        File musicBoxDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoulRepair_MusicBox");
        try {
            if (!musicBoxDir.exists()) musicBoxDir.mkdirs();
        } catch (Exception ignored) {}

        File appMusicDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        try {
            if (appMusicDir != null && !appMusicDir.exists()) appMusicDir.mkdirs();
        } catch (Exception ignored) {}

        int successCount = 0;
        for (AudioItem item : toImport) {
            String fileName = item.title + "." + item.format.toLowerCase();
            if (item.path != null && new File(item.path).exists()) {
                fileName = new File(item.path).getName();
            }

            boolean ok = copyAudioToDestination(item, new File(musicBoxDir, fileName));
            if (appMusicDir != null) {
                copyAudioToDestination(item, new File(appMusicDir, fileName));
            }
            if (ok) {
                successCount++;
            }
        }

        Toast.makeText(this, "✅ 已成功匯入 " + successCount + " 首歌曲至 Music Box！", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private boolean copyAudioToDestination(AudioItem item, File dst) {
        InputStream in = null;
        try {
            if (item.uri != null && "content".equals(item.uri.getScheme())) {
                in = getContentResolver().openInputStream(item.uri);
            } else if (item.path != null && new File(item.path).exists()) {
                in = new FileInputStream(item.path);
            }
            if (in == null && item.uri != null) {
                in = getContentResolver().openInputStream(item.uri);
            }
            if (in == null) return false;

            if (dst.exists()) dst.delete();
            if (dst.getParentFile() != null && !dst.getParentFile().exists()) {
                dst.getParentFile().mkdirs();
            }

            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed copying " + item.title, e);
            return false;
        } finally {
            if (in != null) {
                try { in.close(); } catch (Exception ignored) {}
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SYSTEM_PICKER && resultCode == RESULT_OK && data != null) {
            File musicBoxDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoulRepair_MusicBox");
            if (!musicBoxDir.exists()) musicBoxDir.mkdirs();
            File appMusicDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            if (appMusicDir != null && !appMusicDir.exists()) appMusicDir.mkdirs();

            int importedCount = 0;
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri fileUri = data.getClipData().getItemAt(i).getUri();
                    if (copyUriToMusicBox(fileUri, musicBoxDir, appMusicDir)) {
                        importedCount++;
                    }
                }
            } else if (data.getData() != null) {
                Uri fileUri = data.getData();
                if (copyUriToMusicBox(fileUri, musicBoxDir, appMusicDir)) {
                    importedCount++;
                }
            }

            if (importedCount > 0) {
                Toast.makeText(this, "✅ 成功匯入 " + importedCount + " 首歌曲！", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "未匯入任何檔案", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean copyUriToMusicBox(Uri uri, File musicBoxDir, File appMusicDir) {
        String displayName = "imported_audio_" + System.currentTimeMillis() + ".mp3";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    displayName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception ignored) {}

        File dst = new File(musicBoxDir, displayName);
        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(dst)) {
            if (in == null) return false;
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            if (appMusicDir != null) {
                File appDst = new File(appMusicDir, displayName);
                try (InputStream in2 = getContentResolver().openInputStream(uri);
                     OutputStream out2 = new FileOutputStream(appDst)) {
                    if (in2 != null) {
                        while ((len = in2.read(buf)) > 0) {
                            out2.write(buf, 0, len);
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error copying from SAF picker", e);
            return false;
        }
    }

    private class AudioAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return audioItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return audioItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(FileSelectionActivity.this).inflate(R.layout.item_import_audio, parent, false);
                holder = new ViewHolder();
                holder.card = convertView.findViewById(R.id.cardAudioItem);
                holder.txtTitle = convertView.findViewById(R.id.txtAudioTitle);
                holder.txtFormat = convertView.findViewById(R.id.txtAudioFormat);
                holder.txtDetails = convertView.findViewById(R.id.txtAudioDetails);
                holder.checkBox = convertView.findViewById(R.id.chkAudioSelect);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AudioItem item = audioItemList.get(position);
            holder.txtTitle.setText(item.title);
            holder.txtFormat.setText(item.format);

            String sizeText = formatFileSize(item.sizeBytes);
            String folder = item.path != null ? new File(item.path).getParent() : "";
            if (folder != null && folder.length() > 20) {
                folder = "..." + folder.substring(folder.length() - 18);
            }
            holder.txtDetails.setText(sizeText + (folder != null && !folder.isEmpty() ? " • " + folder : ""));
            holder.checkBox.setChecked(item.isChecked);

            holder.card.setOnClickListener(v -> {
                item.isChecked = !item.isChecked;
                holder.checkBox.setChecked(item.isChecked);
                updateImportButtonState();
            });

            return convertView;
        }

        private String formatFileSize(long bytes) {
            if (bytes <= 0) return "-- MB";
            if (bytes < 1024 * 1024) {
                return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
            }
            return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
        }

        class ViewHolder {
            View card;
            TextView txtTitle;
            TextView txtFormat;
            TextView txtDetails;
            CheckBox checkBox;
        }
    }
}
