package com.saianoe.basalt.activities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.saianoe.basalt.fragments.HomeFragment.mangaAdapter;
import static com.saianoe.basalt.others.Constants.defaultComparator;
import static com.saianoe.basalt.others.Constants.downloadPathString;
import static com.saianoe.basalt.others.Constants.downloadPathNotSet;
import static com.saianoe.basalt.others.Constants.downloadPermissionCode;
import static com.saianoe.basalt.others.Constants.downloadedDB;
import static com.saianoe.basalt.others.Constants.downloadedTable;
import static com.saianoe.basalt.others.Constants.downloadedTableColumn;
import static com.saianoe.basalt.others.Constants.enableDownloads;
import static com.saianoe.basalt.others.Constants.enableDownloadsDataSaver;
import static com.saianoe.basalt.others.Constants.feedUrl;
import static com.saianoe.basalt.others.Constants.forwardSlash;
import static com.saianoe.basalt.others.Constants.functionRequiresStorageAccess;
import static com.saianoe.basalt.others.Constants.homeDB;
import static com.saianoe.basalt.others.Constants.homeTable;
import static com.saianoe.basalt.others.Constants.homeTableColumns;
import static com.saianoe.basalt.others.Constants.homeUrl;
import static com.saianoe.basalt.others.Constants.home_manga_covers;
import static com.saianoe.basalt.others.Constants.home_manga_ids;
import static com.saianoe.basalt.others.Constants.home_manga_titles;
import static com.saianoe.basalt.others.Constants.intentFromChapter;
import static com.saianoe.basalt.others.Constants.intentFromManga;
import static com.saianoe.basalt.others.Constants.notificationChannelCode;
import static com.saianoe.basalt.others.Constants.notificationDownloadStatusList;
import static com.saianoe.basalt.others.Constants.one;
import static com.saianoe.basalt.others.Constants.prefs;
import static com.saianoe.basalt.others.Constants.prefsSettings;
import static com.saianoe.basalt.others.Constants.typeAttributes;
import static com.saianoe.basalt.others.Constants.typeData;
import static com.saianoe.basalt.others.Constants.typeDataSaver;
import static com.saianoe.basalt.others.Constants.typeDataSaver2;
import static com.saianoe.basalt.others.Constants.typeId;
import static com.saianoe.basalt.others.Constants.webGet;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.saianoe.basalt.R;
import com.saianoe.basalt.adapters.ChapterAdapter;
import com.saianoe.basalt.adapters.DownloadAdapter;
import com.saianoe.basalt.others.EasySQL;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import jp.wasabeef.picasso.transformations.BlurTransformation;

public class ChapterView extends AppCompatActivity implements ChapterAdapter.OnListItemClick{

    RecyclerView recyclerView;
    Map<String, String> mangaChapters;
    List<String> mangaChapterIds = new ArrayList<>(), mangaChapterTitles = new ArrayList<>();
    int globalPosition = 0;
    EasySQL es;
    ConstraintLayout downloadButton;
    DownloadAdapter downloadAdapter;
    boolean online = false;
    Uri uri, uriToDownload;
    DocumentFile uriDocumentFile, uriDocumentFileToDownload;
    List<String> mangaChapterPagesToDownload = new ArrayList<>();
    List<String> mangaChapterPageUrlsToDownload = new ArrayList<>();
    int downloadIndex = 0;
    NotificationManager notificationManager;
    Notification notification;
    Notification.BigTextStyle bigTextStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_view);

        prefs = getSharedPreferences(prefsSettings, Context.MODE_PRIVATE);

        Objects.requireNonNull(getSupportActionBar()).hide();
        es = new EasySQL(this);

        recyclerView = findViewById(R.id.chapter_list);
        TextView titleTextView = findViewById(R.id.chapter_title);
        ImageView thumbnailImageView = findViewById(R.id.chapter_thumbnail);
        ImageView bannerImageView = findViewById(R.id.imgbanner);
        downloadButton = findViewById(R.id.chapter_download);
        ConstraintLayout addToHomeButton = findViewById(R.id.add_to_home);
        ImageView addToHomeImageView = findViewById(R.id.add_to_home_image_view);
        TextView addToHomeTextView = findViewById(R.id.add_to_home_text_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        if (isAtHome()){
            addToHomeImageView.setImageResource(R.drawable.baseline_favorite_24);
            addToHomeTextView.setTextColor(getResources().getColor(R.color.blue));
            addToHomeTextView.setText(R.string.in_home_library);
        }

        if (prefs.getBoolean(enableDownloads, false)){
            downloadButton.setVisibility(View.VISIBLE);
        } else {
            downloadButton.setVisibility(View.GONE);
        }

        downloadButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if (!Environment.isExternalStorageManager()){
                    Toast.makeText(ChapterView.this, functionRequiresStorageAccess, Toast.LENGTH_SHORT).show();
                    Intent i = new Intent();
                    i.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(i, downloadPermissionCode);
                } else {
                    showDownloadDialog();
                }
            } else {
                if (ActivityCompat.checkSelfPermission(ChapterView.this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(ChapterView.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(ChapterView.this, functionRequiresStorageAccess, Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(ChapterView.this, new String[]{WRITE_EXTERNAL_STORAGE}, downloadPermissionCode);
                } else {
                    showDownloadDialog();
                }
            }
        });

        addToHomeButton.setOnClickListener(v -> {
            if (!isAtHome()){
                addToHomeImageView.setImageResource(R.drawable.baseline_favorite_24);
                addToHomeTextView.setTextColor(getResources().getColor(R.color.blue));
                addToHomeTextView.setText(R.string.in_home_library);
                Map<String, String> newValues = new HashMap<>();
                newValues.put(homeTableColumns[0], getIntent().getStringExtra(intentFromManga[0]));
                newValues.put(homeTableColumns[1], getIntent().getStringExtra(intentFromManga[1]));
                newValues.put(homeTableColumns[2], getIntent().getStringExtra(intentFromManga[2]));
                es.insertToTable(homeDB, homeTable, newValues);
            } else {
                addToHomeImageView.setImageResource(R.drawable.baseline_favorite_border_24);
                addToHomeTextView.setTextColor(getResources().getColor(R.color.gray));
                addToHomeTextView.setText(R.string.add_to_home);
                String where = es.whereClauseCreator(homeTableColumns[0], getIntent().getStringExtra(intentFromManga[0]));
                es.deleteFromTable(homeDB, homeTable, where);
                try {
                    int position = home_manga_ids.indexOf(getIntent().getStringExtra(intentFromManga[0]));
                    home_manga_ids.remove(position);
                    home_manga_titles.remove(position);
                    home_manga_covers.remove(position);
                    mangaAdapter.notifyItemRemoved(position);
                    mangaAdapter.notifyItemRangeChanged(position, mangaAdapter.getItemCount() - position);
                } catch (Exception ignored){
                }
            }
        });

        titleTextView.setText(getIntent().getStringExtra(intentFromManga[1]));
        titleTextView.setSelected(true);
        Picasso.get().load(getIntent().getStringExtra(intentFromManga[2])).into(thumbnailImageView);
        BlurTransformation blurTransformation = new BlurTransformation(this);
        Picasso.get().load(getIntent().getStringExtra(intentFromManga[2])).transform(blurTransformation).into(bannerImageView);

        mangaChapters = new TreeMap<>(defaultComparator);
    }

    void showDownloadDialog(){
        List<String> tempIds = new ArrayList<>();
        List<String> tempTitles = new ArrayList<>();
        for (Map.Entry<String, String> a : mangaChapters.entrySet()){
            tempIds.add(a.getValue());
            tempTitles.add("Chapter " + a.getKey());
        }

        LayoutInflater layoutInflater = LayoutInflater.from(ChapterView.this);
        View dialogView = layoutInflater.inflate(R.layout.rounded_alert_dialog_layout_download, null);
        final TextView texttitle = dialogView.findViewById(R.id.rounded_alert_dialog_title);
        RecyclerView rv = dialogView.findViewById(R.id.rounded_alert_dialog_recycler_view);
        LinearLayoutManager downloadDialogLinearLayoutManager = new LinearLayoutManager(ChapterView.this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(downloadDialogLinearLayoutManager);
        downloadAdapter = new DownloadAdapter(this, tempTitles, tempIds);
        rv.setAdapter(downloadAdapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(ChapterView.this)
                .setView(dialogView);
        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).getDecorView().setBackgroundColor(Color.TRANSPARENT);
        texttitle.setText(R.string.download_chapter_s);
        Button leftButton = dialogView.findViewById(R.id.rounded_alert_dialog_left_button);
        Button rightButton = dialogView.findViewById(R.id.rounded_alert_dialog_right_button);
        ConstraintLayout downloadAll = dialogView.findViewById(R.id.rounded_alert_dialog_download_all);
        downloadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> toDownload = downloadAdapter.allChecked();
                for (int a = 0; a < toDownload.size(); a++){
                    if (toDownload.get(a).equals(one)){
                        globalPosition = a;
                        notificationDownloadStatusList.add("Downloading " + getIntent().getStringExtra(intentFromManga[1]) + " - " + mangaChapterTitles.get(globalPosition));
                        startNotification();
                        new PageGetter().execute(feedUrl + mangaChapterIds.get(globalPosition));
                        break;
                    }
                }
                alertDialog.hide();
            }
        });
        leftButton.setText(R.string.cancel);
        rightButton.setText(R.string.download);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.hide();
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> toDownload = downloadAdapter.getChecked();
                for (int a = 0; a < toDownload.size(); a++){
                    if (toDownload.get(a).equals(one)){
                        globalPosition = a;
                        notificationDownloadStatusList.add("Downloading " + getIntent().getStringExtra(intentFromManga[1]) + " - " + mangaChapterTitles.get(globalPosition));
                        startNotification();
                        new PageGetter().execute(feedUrl + mangaChapterIds.get(globalPosition));
                        break;
                    }
                }
                alertDialog.hide();
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ChapterGetter().execute(homeUrl + getIntent().getStringExtra(intentFromManga[0]) + "/feed");
    }

    @Override
    public void onListItemClick(int position) {
        if (!online){
            List<Map<String, String>> values = es.getTableValues(downloadedDB, downloadedTable);
            mangaChapterIds.clear();
            mangaChapterTitles.clear();
            for (Map<String, String> value : values){
                for (Map.Entry<String, String> entry : value.entrySet()){
                    mangaChapterIds.add(entry.getValue().split("\\$")[1]);
                    mangaChapterTitles.add(entry.getValue().split("\\$")[2]);
                }
            }
        }
        Intent intent = new Intent(this, PageView.class);
        intent.putExtra(intentFromManga[0], getIntent().getStringExtra(intentFromManga[0]));
        intent.putExtra(intentFromChapter[0], position);
        intent.putExtra(intentFromChapter[1], mangaChapterIds.toArray(new String[0]));
        intent.putExtra(intentFromChapter[2], mangaChapterTitles.toArray(new String[0]));
        startActivity(intent);
    }

    @SuppressLint("StaticFieldLeak")
    class ChapterGetter extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(webGet);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                reader.close();

                online = true;
                return new JSONObject(stringBuilder.toString());
            } catch (Exception e){
                online = false;

                downloadButton.setVisibility(View.GONE);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                if (jsonObject != null){
                    mangaChapters.clear();
                    mangaChapterIds.clear();
                    mangaChapterTitles.clear();
                    JSONArray data = jsonObject.getJSONArray(typeData);
                    for (int a = 0; a < data.length(); a++){
                        JSONObject id = data.getJSONObject(a);
                        JSONObject attributes = id.getJSONObject(typeAttributes);
                        if (attributes.get("translatedLanguage").equals("en")){
                            mangaChapters.put(attributes.get("chapter").toString(), id.get(typeId).toString());
                        }
                    }

                    for (Map.Entry<String, String> entry : mangaChapters.entrySet()){
                        mangaChapterIds.add(entry.getValue());
                    }
                    for (Map.Entry<String, String> entry : mangaChapters.entrySet()){
                        mangaChapterTitles.add("Chapter " + entry.getKey());
                    }
                    ChapterAdapter chapterAdapter = new ChapterAdapter(ChapterView.this, getIntent().getStringExtra(intentFromManga[0]), mangaChapterTitles, ChapterView.this);
                    recyclerView.setAdapter(chapterAdapter);
                } else {
                    uri = Uri.parse(prefs.getString(downloadPathString, downloadPathNotSet));
                    uriDocumentFile = DocumentFile.fromTreeUri(ChapterView.this, uri);

                    if (uriDocumentFile.findFile(Objects.requireNonNull(getIntent().getStringExtra(intentFromManga[0]))) != null){
                        List<String> temporaryMangaChapterTitles = new ArrayList<>();

                        List<Map<String, String>> values = es.getTableValues(downloadedDB, downloadedTable);
                        for (Map<String, String> value : values){
                            for (Map.Entry<String, String> entry : value.entrySet()){
                                if (entry.getValue().split("\\$")[0].equals(getIntent().getStringExtra(intentFromManga[0]))){
                                    temporaryMangaChapterTitles.add(entry.getValue().split("\\$")[2]);
                                }
                            }
                        }

                        if (temporaryMangaChapterTitles.isEmpty()){
                            Toast.makeText(ChapterView.this, "No chapters downloaded.", Toast.LENGTH_SHORT).show();
                        }

                        ChapterAdapter chapterAdapter = new ChapterAdapter(ChapterView.this, getIntent().getStringExtra(intentFromManga[0]), temporaryMangaChapterTitles, ChapterView.this);
                        recyclerView.setAdapter(chapterAdapter);
                    } else {
                        Toast.makeText(ChapterView.this, "No chapters downloaded.", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception ignored){
            }
        }
    }

    boolean isAtHome(){
        List<Map<String, String>> values = es.getTableValues(homeDB, homeTable);
        List<String> savedMangaIds = new ArrayList<>();

        for (Map<String, String> value : values){
            for (Map.Entry<String, String> entry : value.entrySet()){
                if (entry.getKey().equals(homeTableColumns[0])){
                    savedMangaIds.add(entry.getValue());
                }
            }
        }

        return savedMangaIds.contains(getIntent().getStringExtra(intentFromManga[0]));
    }

    @SuppressLint("StaticFieldLeak")
    class PageGetter extends AsyncTask<String, Void, JSONObject>{
        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(webGet);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                reader.close();

                return new JSONObject(stringBuilder.toString());
            } catch (Exception ignored){
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                if (jsonObject != null){
                    mangaChapterPagesToDownload.clear();
                    mangaChapterPageUrlsToDownload.clear();
                    downloadIndex = 0;
                    String baseUrl = jsonObject.getString("baseUrl");
                    JSONObject chapter = jsonObject.getJSONObject("chapter");
                    String dataMode = prefs.getBoolean(enableDownloadsDataSaver, false) ? typeDataSaver : typeData;
                    String dataUrlMode = prefs.getBoolean(enableDownloadsDataSaver, false) ? typeDataSaver2 : typeData;
                    JSONArray data = chapter.getJSONArray(dataMode);
                    for (int a = 0; a < data.length(); a++){
                        String miniData = data.get(a).toString();
                        mangaChapterPagesToDownload.add(miniData);
                        mangaChapterPageUrlsToDownload.add(baseUrl + forwardSlash + dataUrlMode + "/" + chapter.get("hash") + "/" + miniData);
                    }
                    new ImageDownloader().execute(mangaChapterPagesToDownload, mangaChapterPageUrlsToDownload);
                }
            } catch (Exception ignored){
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class ImageDownloader extends AsyncTask<List<String>, Void, List<String>>{

        @SafeVarargs
        @Override
        protected final List<String> doInBackground(List<String>... strings) {
            try {
                URL url = new URL(strings[1].get(downloadIndex));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                uriToDownload = Uri.parse(prefs.getString(downloadPathString, downloadPathNotSet));
                try {
                    uriDocumentFileToDownload = DocumentFile.fromTreeUri(ChapterView.this, uriToDownload);
                    if (uriDocumentFileToDownload.findFile(Objects.requireNonNull(getIntent().getStringExtra(intentFromManga[0]))) == null){
                        uriDocumentFileToDownload.createDirectory(Objects.requireNonNull(getIntent().getStringExtra(intentFromManga[0])));
                    }
                    uriDocumentFileToDownload = uriDocumentFileToDownload.findFile(Objects.requireNonNull(getIntent().getStringExtra(intentFromManga[0])));
                    if (uriDocumentFileToDownload.findFile(mangaChapterIds.get(globalPosition)) == null){
                        uriDocumentFileToDownload.createDirectory(mangaChapterIds.get(globalPosition));
                    }
                    uriDocumentFileToDownload = uriDocumentFileToDownload.findFile(mangaChapterIds.get(globalPosition));
                    if (uriDocumentFileToDownload.findFile(downloadIndex + ".jpg") == null){
                        FileOutputStream os = (FileOutputStream) getContentResolver().openOutputStream(Objects.requireNonNull(uriDocumentFileToDownload.createFile("image/jpeg", downloadIndex + ".jpg")).getUri());
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                        os.flush();
                        os.close();
                    }
                } catch (Exception ignored){
                }
            } catch (Exception ignored){
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            if (downloadIndex < mangaChapterPageUrlsToDownload.size()){
                downloadIndex++;
                new ImageDownloader().execute(mangaChapterPagesToDownload, mangaChapterPageUrlsToDownload);
            } else {
                if (!es.doesValueExist(downloadedDB, downloadedTable, downloadedTableColumn, getIntent().getStringExtra(intentFromManga[0]) + "$" + mangaChapterIds.get(globalPosition) + "$" + mangaChapterTitles.get(globalPosition))){
                    Map<String, String> newValues = new HashMap<>();
                    newValues.put(downloadedTableColumn, getIntent().getStringExtra(intentFromManga[0]) + "$" + mangaChapterIds.get(globalPosition) + "$" + mangaChapterTitles.get(globalPosition));
                    es.insertToTable(downloadedDB, downloadedTable, newValues);
                }

                notificationDownloadStatusList.remove("Downloading " + getIntent().getStringExtra(intentFromManga[1]) + " - " + mangaChapterTitles.get(globalPosition));
                notificationDownloadStatusList.add("Downloaded " + getIntent().getStringExtra(intentFromManga[1]) + " - " + mangaChapterTitles.get(globalPosition));
                startNotification();
                downloadAgain();
            }
        }
    }

    void downloadAgain(){
        globalPosition++;
        if (globalPosition < downloadAdapter.getChecked().size()){
            if (downloadAdapter.getChecked().get(globalPosition).equals(one)){
                notificationDownloadStatusList.add("Downloading " + getIntent().getStringExtra(intentFromManga[1]) + " - " + mangaChapterTitles.get(globalPosition));
                startNotification();
                new PageGetter().execute(feedUrl + mangaChapterIds.get(globalPosition));
            }
        }
    }

    void startNotification(){
        if (notificationDownloadStatusList.size() > 3){
            notificationDownloadStatusList.remove(0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            try {
                notificationManager.cancel(notificationChannelCode);
            } catch (Exception ignored){
            }
            bigTextStyle = new Notification.BigTextStyle();
            NotificationChannel channel = new NotificationChannel(String.valueOf(notificationChannelCode), "basalt", NotificationManager.IMPORTANCE_LOW);
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            notification = new Notification.Builder(this, String.valueOf(notificationChannelCode))
                    .setSmallIcon(R.drawable.baseline_download_24)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setColor(getColor(R.color.transparent))
                    .setContentTitle("Download")
                    .setStyle(bigTextStyle)
                    .setOnlyAlertOnce(true)
                    .build();
            StringBuilder bigText = new StringBuilder();
            for (int i = 0; i < notificationDownloadStatusList.size(); i++) {
                if (i == (notificationDownloadStatusList.size() - 1)){
                    bigText.append(notificationDownloadStatusList.get(i));
                } else {
                    bigText.append(notificationDownloadStatusList.get(i));
                    bigText.append("\n");
                }
            }
            bigTextStyle.bigText(bigText);
            bigTextStyle.build();
            notificationManager.notify(notificationChannelCode, notification);
        } else {
            try {
                notificationManager.cancel(notificationChannelCode);
            } catch (Exception ignored){

            }
            StringBuilder bigText = new StringBuilder();
            for (int i = 0; i < notificationDownloadStatusList.size(); i++) {
                if (i == (notificationDownloadStatusList.size() - 1)){
                    bigText.append(notificationDownloadStatusList.get(i));
                } else {
                    bigText.append(notificationDownloadStatusList.get(i));
                    bigText.append("\n");
                }
            }
            bigTextStyle = new Notification.BigTextStyle();
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notification = new NotificationCompat.Builder(this, String.valueOf(notificationChannelCode))
                    .setSmallIcon(R.drawable.baseline_download_24)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setColor(getColor(R.color.transparent))
                    .setContentTitle("Download")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                    .setOnlyAlertOnce(true)
                    .build();
            notificationManager.notify(notificationChannelCode, notification);
        }
    }
}