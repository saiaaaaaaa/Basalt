package com.saianoe.basalt.activities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.saianoe.basalt.others.Constants.atSeparator;
import static com.saianoe.basalt.others.Constants.negOne;
import static com.saianoe.basalt.others.Constants.nullString;
import static com.saianoe.basalt.others.Constants.outDataHome;
import static com.saianoe.basalt.others.Constants.outDataRead;
import static com.saianoe.basalt.others.Constants.tableColumnType;
import static com.saianoe.basalt.others.Constants.dataExportCode;
import static com.saianoe.basalt.others.Constants.dataImportCode;
import static com.saianoe.basalt.others.Constants.dataSaver;
import static com.saianoe.basalt.others.Constants.defaultComparator;
import static com.saianoe.basalt.others.Constants.downloadPathCode;
import static com.saianoe.basalt.others.Constants.downloadPathNotSet;
import static com.saianoe.basalt.others.Constants.downloadPathString;
import static com.saianoe.basalt.others.Constants.downloadPermissionCode;
import static com.saianoe.basalt.others.Constants.enableDownloads;
import static com.saianoe.basalt.others.Constants.enableDownloadsDataSaver;
import static com.saianoe.basalt.others.Constants.functionRequiresStorageAccess;
import static com.saianoe.basalt.others.Constants.homeDB;
import static com.saianoe.basalt.others.Constants.homeTable;
import static com.saianoe.basalt.others.Constants.homeTableColumns;
import static com.saianoe.basalt.others.Constants.intentTypeData;
import static com.saianoe.basalt.others.Constants.prefs;
import static com.saianoe.basalt.others.Constants.prefsEditor;
import static com.saianoe.basalt.others.Constants.readDB;
import static com.saianoe.basalt.others.Constants.readTable;
import static com.saianoe.basalt.others.Constants.readTableColumns;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.saianoe.basalt.R;
import com.saianoe.basalt.others.EasySQL;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    SwitchMaterial dataSaverSwitch, enableDownloadsSwitch, enableDownloadsDataSaverSwitch;
    TextView downloadPath, dataSaverTextView, enableDownloadsTextView, enableDownloadsDataSaverTextView, downloadPathTextView;
    LinearLayout dataSaverContainer, enableDownloadsContainer, enableDownloadsDataSaverContainer, downloadPathContainer, importDataContainer, exportDataContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dataSaverSwitch = findViewById(R.id.data_saver_switch);
        enableDownloadsSwitch = findViewById(R.id.enable_downloads_switch);
        enableDownloadsDataSaverSwitch = findViewById(R.id.enable_downloads_data_saver_switch);
        downloadPath = findViewById(R.id.download_path);
        dataSaverTextView = findViewById(R.id.data_saver_text_view);
        enableDownloadsTextView = findViewById(R.id.enable_downloads_text_view);
        enableDownloadsDataSaverTextView = findViewById(R.id.enable_downloads_data_saver_text_view);
        downloadPathTextView = findViewById(R.id.download_path_text_view);
        dataSaverContainer = findViewById(R.id.data_saver_container);
        enableDownloadsContainer = findViewById(R.id.enable_downloads_container);
        enableDownloadsDataSaverContainer = findViewById(R.id.enable_downloads_data_saver_container);
        downloadPathContainer = findViewById(R.id.download_path_container);
        importDataContainer = findViewById(R.id.data_import_container);
        exportDataContainer = findViewById(R.id.data_export_container);

        downloadPath.setSelected(true);

        exportDataContainer.setOnClickListener(v -> {
            EasySQL es = new EasySQL(this);
            es.deleteDuplicateRows(homeDB, homeTable, homeTableColumns);
            es.deleteDuplicateRows(readDB, readTable, readTableColumns);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if (!Environment.isExternalStorageManager()){
                    Toast.makeText(this, functionRequiresStorageAccess, Toast.LENGTH_SHORT).show();
                    Intent i = new Intent();
                    i.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(i, downloadPermissionCode);
                } else {
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(i, dataExportCode);
                }
            } else {
                if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, functionRequiresStorageAccess, Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, downloadPermissionCode);
                } else {
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(i, dataExportCode);
                }
            }
        });

        importDataContainer.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if (!Environment.isExternalStorageManager()){
                    Toast.makeText(this, functionRequiresStorageAccess, Toast.LENGTH_SHORT).show();
                    Intent i = new Intent();
                    i.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(i, downloadPermissionCode);
                } else {
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    i.setType(intentTypeData);
                    startActivityForResult(i, dataImportCode);
                }
            } else {
                if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, functionRequiresStorageAccess, Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, downloadPermissionCode);
                } else {
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    i.setType(intentTypeData);
                    startActivityForResult(i, dataImportCode);
                }
            }
        });

        dataSaverSwitch.setOnClickListener(v -> {
            prefsEditor.putBoolean(dataSaver, dataSaverSwitch.isChecked());
            prefsEditor.apply();
        });

        enableDownloadsSwitch.setOnClickListener(v -> {
            prefsEditor.putBoolean(enableDownloads, enableDownloadsSwitch.isChecked());
            prefsEditor.apply();
            if (enableDownloadsSwitch.isChecked()){
                enableDownloadsDataSaverContainer.setVisibility(View.VISIBLE);
                downloadPathContainer.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.POST_NOTIFICATIONS}, 0);
                    }
                }
            } else {
                enableDownloadsDataSaverContainer.setVisibility(View.GONE);
                downloadPathContainer.setVisibility(View.GONE);
            }
        });

        enableDownloadsDataSaverSwitch.setOnClickListener(v -> {
            prefsEditor.putBoolean(enableDownloadsDataSaver, enableDownloadsDataSaverSwitch.isChecked());
            prefsEditor.apply();
        });

        dataSaverContainer.setOnClickListener(v -> {
            dataSaverSwitch.setChecked(!dataSaverSwitch.isChecked());
            prefsEditor.putBoolean(dataSaver, dataSaverSwitch.isChecked());
            prefsEditor.apply();
        });

        enableDownloadsContainer.setOnClickListener(v -> {
            enableDownloadsSwitch.setChecked(!enableDownloadsSwitch.isChecked());
            prefsEditor.putBoolean(enableDownloads, enableDownloadsSwitch.isChecked());
            prefsEditor.apply();
            if (enableDownloadsSwitch.isChecked()){
                enableDownloadsDataSaverContainer.setVisibility(View.VISIBLE);
                downloadPathContainer.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.POST_NOTIFICATIONS}, 0);
                    }
                }
            } else {
                enableDownloadsDataSaverContainer.setVisibility(View.GONE);
                downloadPathContainer.setVisibility(View.GONE);
            }
        });

        enableDownloadsDataSaverContainer.setOnClickListener(v -> {
            enableDownloadsDataSaverSwitch.setChecked(!enableDownloadsDataSaverSwitch.isChecked());
            prefsEditor.putBoolean(enableDownloadsDataSaver, enableDownloadsDataSaverSwitch.isChecked());
            prefsEditor.apply();
        });

        downloadPathContainer.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(i, downloadPathCode);
        });
    }

    byte[] dataBytes(){
        StringBuilder data = new StringBuilder();
        EasySQL es = new EasySQL(this);
        List<Map<String, String>> homevalues = es.getTableValues(homeDB, homeTable);
        List<Map<String, String>> readvalues = es.getTableValues(readDB, readTable);

        List<String> home_ids = new ArrayList<>();
        List<String> home_titles = new ArrayList<>();
        List<String> home_covers = new ArrayList<>();
        List<String> read_ids = new ArrayList<>();
        List<String> read_ch_ids = new ArrayList<>();
        List<String> read_ch_titles = new ArrayList<>();

        for (Map<String, String> value : homevalues){
            for (Map.Entry<String, String> entry : value.entrySet()){
                if (entry.getKey().equals(homeTableColumns[0])){
                    home_ids.add(entry.getValue());
                }
                if (entry.getKey().equals(homeTableColumns[2])){
                    home_covers.add(entry.getValue());
                }
                if (entry.getKey().equals(homeTableColumns[1])){
                    home_titles.add(entry.getValue());
                }
            }
        }
        for (Map<String, String> value : readvalues){
            for (Map.Entry<String, String> entry : value.entrySet()){
                if (entry.getKey().equals(readTableColumns[0])){
                    read_ids.add(entry.getValue());
                }
                if (entry.getKey().equals(readTableColumns[1])){
                    read_ch_ids.add(entry.getValue());
                }
                if (entry.getKey().equals(readTableColumns[2])){
                    read_ch_titles.add(entry.getValue());
                }
            }
        }

        data.append(outDataHome + "\n");
        for (int a = 0; a < home_ids.size(); a++){
            data.append(home_ids.get(a)).append(atSeparator);
            data.append(home_covers.get(a)).append(atSeparator);
            data.append(home_titles.get(a)).append("\n");
        }
        data.append(outDataRead + "\n");
        for (int a = 0; a < read_ids.size(); a++){
            data.append(read_ids.get(a)).append(atSeparator);
            data.append(read_ch_ids.get(a)).append(atSeparator);
            data.append(read_ch_titles.get(a)).append("\n");
        }

        return data.toString().getBytes();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshSettings();
    }

    void refreshSettings(){
        dataSaverSwitch.setChecked(prefs.getBoolean(dataSaver, false));
        enableDownloadsSwitch.setChecked(prefs.getBoolean(enableDownloads, false));
        enableDownloadsDataSaverSwitch.setChecked(prefs.getBoolean(enableDownloadsDataSaver, false));
        downloadPath.setText(prefs.getString(downloadPathString, downloadPathNotSet));

        if (enableDownloadsSwitch.isChecked()){
            enableDownloadsDataSaverContainer.setVisibility(View.VISIBLE);
            downloadPathContainer.setVisibility(View.VISIBLE);
        }
    }

    /** @noinspection deprecation*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == downloadPathCode){
            assert data != null;
            this.getContentResolver().takePersistableUriPermission(Objects.requireNonNull(data.getData()), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                prefsEditor.putString(downloadPathString, data.getData().toString());
                prefsEditor.apply();
            } catch (Exception ignored){
            }
        }
        if (resultCode == RESULT_OK && requestCode == dataExportCode){
            assert data != null;
            this.getContentResolver().takePersistableUriPermission(Objects.requireNonNull(data.getData()), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                Uri uri = data.getData();
                DocumentFile uriDocumentFile = DocumentFile.fromTreeUri(this, uri);
                assert uriDocumentFile != null;
                Date date = Calendar.getInstance().getTime();
                String timestamp = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() + "-Basalt_exported_data";
                FileOutputStream os = (FileOutputStream) this.getContentResolver().openOutputStream(Objects.requireNonNull(uriDocumentFile.createFile("text/plain", timestamp)).getUri());
                Objects.requireNonNull(os).write(dataBytes());
                os.flush();
                os.close();
            } catch (Exception ignored){
            }
        }
        if (resultCode == RESULT_OK && requestCode == dataImportCode){
            assert data != null;
            this.getContentResolver().takePersistableUriPermission(Objects.requireNonNull(data.getData()), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                Uri uri = data.getData();
                InputStream is = this.getContentResolver().openInputStream(uri);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder content = new StringBuilder();
                for (String line; (line = br.readLine()) != null;){
                    content.append(line).append("\n");
                }
                String[] lines = content.toString().split("\n");
                List<String> all = new ArrayList<>(Arrays.asList(lines));
                List<String> home = new ArrayList<>();
                for (int a = 0; a < all.size(); a++){
                    if (!all.get(a).equals(outDataRead)){
                        home.add(all.get(a));
                    } else {
                        break;
                    }
                }
                all.removeAll(home);
                home.remove(outDataHome);
                all.remove(outDataRead);
                EasySQL es = getEasySQL(home, all);
                es.deleteDuplicateRows(homeDB, homeTable, homeTableColumns);
                es.deleteDuplicateRows(readDB, readTable, readTableColumns);
            } catch (Exception ignored){
            }
        }
    }

    private @NonNull EasySQL getEasySQL(List<String> home, List<String> all) {
        EasySQL es = new EasySQL(this);
        for (String a : home){
            String[] split = a.split(atSeparator);
            Map<String, String> values = new HashMap<>();
            values.put(homeTableColumns[0], split[0]);
            values.put(homeTableColumns[2], split[1]);
            values.put(homeTableColumns[1], split[2]);
            es.insertToTable(homeDB, homeTable, values);
        }
        for (String a : all){
            String[] split = a.split(atSeparator);
            Map<String, String> values = new HashMap<>();
            values.put(readTableColumns[0], split[0]);
            values.put(readTableColumns[1], split[1]);
            values.put(readTableColumns[2], split[2]);
            es.insertToTable(readDB, readTable, values);
        }
        return es;
    }
}