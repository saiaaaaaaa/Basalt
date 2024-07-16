package com.saianoe.basalt;

import static com.saianoe.basalt.others.Constants.homeUrl;
import static com.saianoe.basalt.others.Constants.home_manga_ids;
import static com.saianoe.basalt.others.Constants.notificationDownloadStatusList;
import static com.saianoe.basalt.others.Constants.notificationUpdateStatusList;
import static com.saianoe.basalt.others.Constants.prefs;
import static com.saianoe.basalt.others.Constants.prefsEditor;
import static com.saianoe.basalt.others.Constants.prefsSettings;
import static com.saianoe.basalt.others.Constants.typeAttributes;
import static com.saianoe.basalt.others.Constants.typeData;
import static com.saianoe.basalt.others.Constants.typeId;
import static com.saianoe.basalt.others.Constants.updateDB;
import static com.saianoe.basalt.others.Constants.updateNotificationChannelCode;
import static com.saianoe.basalt.others.Constants.updateTable;
import static com.saianoe.basalt.others.Constants.updateTableColumns;
import static com.saianoe.basalt.others.Constants.webGet;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.saianoe.basalt.activities.SettingsActivity;
import com.saianoe.basalt.fragments.HomeFragment;
import com.saianoe.basalt.fragments.SearchFragment;
import com.saianoe.basalt.others.EasySQL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    public static BottomNavigationView bnv;
    EasySQL es;
    NotificationManager notificationManager;
    Notification notification;
    Notification.BigTextStyle bigTextStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        bnv = findViewById(R.id.bottom_navigation_view);
        bnv.setOnNavigationItemSelectedListener(this);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.frame_layout, new HomeFragment()).commit();
        prefs = getSharedPreferences(prefsSettings, MODE_PRIVATE);
        prefsEditor = prefs.edit();
        es = new EasySQL(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.check_updates){
            for (String a : home_manga_ids){
                new GetChapterUpdates().execute(homeUrl + a);
            }
        }
        if (item.getItemId() == R.id.settings){
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (menuItem.getItemId() == R.id.tab_home){
            if (getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof SearchFragment){
                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                ft.replace(R.id.frame_layout, new HomeFragment()).commit();
            }
            return true;
        }
        if (menuItem.getItemId() == R.id.tab_search){
            if (getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof HomeFragment){
                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                ft.replace(R.id.frame_layout, new SearchFragment()).commit();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!(getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof HomeFragment)){
            bnv.setSelectedItemId(R.id.tab_home);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class GetChapterUpdates extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                String mid = "";
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
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    JSONObject data = jsonObject.getJSONObject(typeData);
                    JSONObject attributes = data.getJSONObject(typeAttributes);
                    JSONObject title = attributes.getJSONObject("title");
                    List<String> possibleLanguages = Arrays.asList("en", "ja", "ja-ro");
                    for (String language : possibleLanguages){
                        try {
                            mid = title.get(language).toString();
                            break;
                        } catch (Exception ignored){
                        }
                    }
                } catch (Exception ignored){
                }

                URL url = new URL(strings[0] + "/feed");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(webGet);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                reader.close();
                return stringBuilder + "BASALT>MANGA_ID>" + mid;
            } catch (Exception ignored){
                return null;
            }
        }

        @Override
        protected void onPostExecute(String str) {
            try {
                String[] split = str.split("BASALT>MANGA_ID>");
                String mid = split[1];
                JSONObject jsonObject = new JSONObject(split[0]);
                if (jsonObject != null){
                    JSONArray data = jsonObject.getJSONArray(typeData);
                    for (int a = 0; a < data.length(); a++){
                        JSONObject id = data.getJSONObject(a);
                        JSONObject attributes = id.getJSONObject(typeAttributes);
                        if (attributes.get("translatedLanguage").equals("en")){
                            String chid = id.get(typeId).toString();
                            String chtitle = "Chapter " + attributes.get("chapter");
                            if (!es.doesValueExist(updateDB, updateTable, updateTableColumns[1], chid + ":" + chtitle)){
                                Map<String, String> columns = new HashMap<>();
                                columns.put(updateTableColumns[0], mid);
                                columns.put(updateTableColumns[1], chid + ":" + chtitle);
                                es.insertToTable(updateDB, updateTable, columns);
                                notificationDownloadStatusList.add(mid + " - " + chtitle);
                                startNotification();
                            }
                        }
                    }
                }
            } catch (Exception ignored){
            }
        }
    }

    void startNotification(){
        if (notificationUpdateStatusList.size() > 3){
            notificationUpdateStatusList.remove(0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            try {
                notificationManager.cancel(updateNotificationChannelCode);
            } catch (Exception ignored){
            }
            bigTextStyle = new Notification.BigTextStyle();
            NotificationChannel channel = new NotificationChannel(String.valueOf(updateNotificationChannelCode), "basalt", NotificationManager.IMPORTANCE_LOW);
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            notification = new Notification.Builder(this, String.valueOf(updateNotificationChannelCode))
                    .setSmallIcon(R.drawable.baseline_update_24)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setColor(getColor(R.color.transparent))
                    .setContentTitle("Update")
                    .setStyle(bigTextStyle)
                    .setOnlyAlertOnce(true)
                    .build();
            StringBuilder bigText = new StringBuilder();
            for (int i = 0; i < notificationUpdateStatusList.size(); i++) {
                if (i == (notificationUpdateStatusList.size() - 1)){
                    bigText.append(notificationUpdateStatusList.get(i));
                } else {
                    bigText.append(notificationUpdateStatusList.get(i));
                    bigText.append("\n");
                }
            }
            bigTextStyle.bigText(bigText);
            bigTextStyle.build();
            notificationManager.notify(updateNotificationChannelCode, notification);
        } else {
            try {
                notificationManager.cancel(updateNotificationChannelCode);
            } catch (Exception ignored){

            }
            StringBuilder bigText = new StringBuilder();
            for (int i = 0; i < notificationUpdateStatusList.size(); i++) {
                if (i == (notificationUpdateStatusList.size() - 1)){
                    bigText.append(notificationUpdateStatusList.get(i));
                } else {
                    bigText.append(notificationUpdateStatusList.get(i));
                    bigText.append("\n");
                }
            }
            bigTextStyle = new Notification.BigTextStyle();
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notification = new NotificationCompat.Builder(this, String.valueOf(updateNotificationChannelCode))
                    .setSmallIcon(R.drawable.baseline_update_24)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setColor(getColor(R.color.transparent))
                    .setContentTitle("Update")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                    .setOnlyAlertOnce(true)
                    .build();
            notificationManager.notify(updateNotificationChannelCode, notification);
        }
    }
}