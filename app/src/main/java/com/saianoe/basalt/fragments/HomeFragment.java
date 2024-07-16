package com.saianoe.basalt.fragments;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.saianoe.basalt.MainActivity.bnv;
import static com.saianoe.basalt.others.Constants.downloadedDB;
import static com.saianoe.basalt.others.Constants.downloadedTable;
import static com.saianoe.basalt.others.Constants.downloadedTableColumn;
import static com.saianoe.basalt.others.Constants.homeUrl;
import static com.saianoe.basalt.others.Constants.intentFromManga;
import static com.saianoe.basalt.others.Constants.notificationDownloadStatusList;
import static com.saianoe.basalt.others.Constants.notificationUpdateStatusList;
import static com.saianoe.basalt.others.Constants.tableColumnType;
import static com.saianoe.basalt.others.Constants.homeDB;
import static com.saianoe.basalt.others.Constants.homeTable;
import static com.saianoe.basalt.others.Constants.homeTableColumns;
import static com.saianoe.basalt.others.Constants.home_manga_covers;
import static com.saianoe.basalt.others.Constants.home_manga_ids;
import static com.saianoe.basalt.others.Constants.home_manga_titles;
import static com.saianoe.basalt.others.Constants.readDB;
import static com.saianoe.basalt.others.Constants.readTable;
import static com.saianoe.basalt.others.Constants.readTableColumns;
import static com.saianoe.basalt.others.Constants.typeAttributes;
import static com.saianoe.basalt.others.Constants.typeData;
import static com.saianoe.basalt.others.Constants.typeId;
import static com.saianoe.basalt.others.Constants.updateDB;
import static com.saianoe.basalt.others.Constants.updateNotificationChannelCode;
import static com.saianoe.basalt.others.Constants.updateTable;
import static com.saianoe.basalt.others.Constants.updateTableColumns;
import static com.saianoe.basalt.others.Constants.webGet;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.saianoe.basalt.R;
import com.saianoe.basalt.activities.ChapterView;
import com.saianoe.basalt.adapters.MangaAdapter;
import com.saianoe.basalt.others.EasySQL;
import com.saianoe.basalt.others.MangaSpaceDecoration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeFragment extends Fragment implements MangaAdapter.OnItemClickListener, MangaAdapter.OnItemLongClickListener{

    RecyclerView recyclerView;
    TextView textViewHint;
    public static MangaAdapter mangaAdapter;
    List<Map<String, String>> values;
    EasySQL es;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.home_list);
        textViewHint = view.findViewById(R.id.home_hint);
        recyclerView.addItemDecoration(new MangaSpaceDecoration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(requireActivity(), new String[] {android.Manifest.permission.POST_NOTIFICATIONS}, 0);
            }
        }

        es = new EasySQL(getContext());
        if (!es.doesTableExist(homeDB, homeTable)){
            Map<String, String> columns = new HashMap<>();
            columns.put(homeTableColumns[0], tableColumnType);
            columns.put(homeTableColumns[1], tableColumnType);
            columns.put(homeTableColumns[2], tableColumnType);
            es.createTable(homeDB, homeTable, columns);
        }
        if (!es.doesTableExist(readDB, readTable)){
            Map<String, String> columns = new HashMap<>();
            columns.put(readTableColumns[0], tableColumnType);
            columns.put(readTableColumns[1], tableColumnType);
            columns.put(readTableColumns[2], tableColumnType);
            es.createTable(readDB, readTable, columns);
        }
        if (!es.doesTableExist(downloadedDB, downloadedTable)){
            Map<String, String> columns = new HashMap<>();
            columns.put(downloadedTableColumn, tableColumnType);
            es.createTable(downloadedDB, downloadedTable, columns);
        }
        if (!es.doesTableExist(updateDB, updateTable)){
            Map<String, String> columns = new HashMap<>();
            columns.put(updateTableColumns[0], tableColumnType);
            columns.put(updateTableColumns[1], tableColumnType);
            es.createTable(updateDB, updateTable, columns);
        }

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        bnv.setSelectedItemId(R.id.tab_home);
    }

    void refresh(){
        try {
            recyclerView.setAdapter(null);
        } catch (Exception ignored){
        }

        es.deleteDuplicateRows(homeDB, homeTable, homeTableColumns);

        values = es.getTableValues(homeDB, homeTable);

        if (!values.isEmpty()){
            textViewHint.setVisibility(View.GONE);

            home_manga_ids.clear();
            home_manga_titles.clear();
            home_manga_covers.clear();

            for (Map<String, String> value : values){
                for (Map.Entry<String, String> entry : value.entrySet()){
                    if (entry.getKey().equals(homeTableColumns[0])){
                        home_manga_ids.add(entry.getValue());
                    }
                    if (entry.getKey().equals(homeTableColumns[1])){
                        home_manga_titles.add(entry.getValue());
                    }
                    if (entry.getKey().equals(homeTableColumns[2])){
                        home_manga_covers.add(entry.getValue());
                    }
                }
            }

            Collections.reverse(home_manga_ids);
            Collections.reverse(home_manga_titles);
            Collections.reverse(home_manga_covers);

            mangaAdapter = new MangaAdapter(home_manga_ids, home_manga_titles, home_manga_covers, this, this);
            recyclerView.setAdapter(mangaAdapter);
        } else {
            textViewHint.setVisibility(View.VISIBLE);
        }
    }



    @Override
    public void onItemClick(int position) {
        Intent i = new Intent(getContext(), ChapterView.class);
        i.putExtra(intentFromManga[0], mangaAdapter.getItemID(position));
        i.putExtra(intentFromManga[1], mangaAdapter.getTitle(position));
        i.putExtra(intentFromManga[2], mangaAdapter.getCoverURL(position));
        startActivity(i);
    }

    @Override
    public void onItemLongClick(int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View dialogView = layoutInflater.inflate(R.layout.rounded_alert_dialog_layout, null);
        final TextView texttitle = dialogView.findViewById(R.id.rounded_alert_dialog_title);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(dialogView);
        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).getDecorView().setBackgroundColor(Color.TRANSPARENT);
        texttitle.setText(R.string.remove_from_home_library);
        Button leftButton = dialogView.findViewById(R.id.rounded_alert_dialog_left_button);
        Button rightButton = dialogView.findViewById(R.id.rounded_alert_dialog_right_button);
        leftButton.setText(R.string.no);
        rightButton.setText(R.string.yes);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.hide();
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.hide();
            }
        });

        alertDialog.show();
    }
}