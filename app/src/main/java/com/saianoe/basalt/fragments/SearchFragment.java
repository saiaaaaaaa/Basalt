package com.saianoe.basalt.fragments;

import static com.saianoe.basalt.others.Constants.coverPictureUrl;
import static com.saianoe.basalt.others.Constants.coverUrl;
import static com.saianoe.basalt.others.Constants.forwardSlash;
import static com.saianoe.basalt.others.Constants.homeDB;
import static com.saianoe.basalt.others.Constants.homeTable;
import static com.saianoe.basalt.others.Constants.homeTableColumns;
import static com.saianoe.basalt.others.Constants.homeUrl;
import static com.saianoe.basalt.others.Constants.intentFromManga;
import static com.saianoe.basalt.others.Constants.picRes;
import static com.saianoe.basalt.others.Constants.searchUrl;
import static com.saianoe.basalt.others.Constants.typeAttributes;
import static com.saianoe.basalt.others.Constants.typeCoverArt;
import static com.saianoe.basalt.others.Constants.typeData;
import static com.saianoe.basalt.others.Constants.typeFilename;
import static com.saianoe.basalt.others.Constants.typeId;
import static com.saianoe.basalt.others.Constants.typeRelationships;
import static com.saianoe.basalt.others.Constants.typeType;
import static com.saianoe.basalt.others.Constants.webGet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.saianoe.basalt.R;
import com.saianoe.basalt.activities.ChapterView;
import com.saianoe.basalt.adapters.MangaAdapter;
import com.saianoe.basalt.others.EasySQL;
import com.saianoe.basalt.others.MangaSpaceDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SearchFragment extends Fragment implements MangaAdapter.OnItemClickListener, MangaAdapter.OnItemLongClickListener{

    RecyclerView recyclerView;
    TextView textViewHint;
    ProgressBar progressBar;
    final List<String> mangaIdList = new ArrayList<>();
    final List<String> mangaTitleList = new ArrayList<>();
    final List<String> mangaCoverList = new ArrayList<>();
    MangaAdapter mangaAdapter;
    EasySQL es;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText searchBar = view.findViewById(R.id.search_edit_text);
        recyclerView = view.findViewById(R.id.search_list);
        textViewHint = view.findViewById(R.id.search_hint);
        progressBar = view.findViewById(R.id.search_progress);
        recyclerView.addItemDecoration(new MangaSpaceDecoration());

        es = new EasySQL(getContext());

        textViewHint.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new MangaHelper().execute(homeUrl);

        searchBar.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                textViewHint.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(null);
                String search = searchUrl + searchBar.getText().toString().trim().replace(' ', '+');
                new MangaHelper().execute(search);
                InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
            }
            return false;
        });
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
        List<Map<String, String>> values = es.getTableValues(homeDB, homeTable);

        List<String> ids = new ArrayList<>();

        for (Map<String, String> value : values){
            for (Map.Entry<String, String> entry : value.entrySet()){
                if (entry.getKey().equals(homeTableColumns[0])){
                    ids.add(entry.getValue());
                }
            }
        }

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

        if (ids.contains(mangaAdapter.getItemID(position))){
            texttitle.setText(R.string.this_is_already_in_your_home_library);
            rightButton.setText(R.string.ok);
            rightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.hide();
                }
            });
        } else {
            texttitle.setText(R.string.add_to_home_library);
            leftButton.setText(R.string.no);
            leftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.hide();
                }
            });
            rightButton.setText(R.string.yes);
            rightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Map<String, String> newValues = new HashMap<>();
                    newValues.put(homeTableColumns[0], mangaAdapter.getItemID(position));
                    newValues.put(homeTableColumns[1], mangaAdapter.getTitle(position));
                    newValues.put(homeTableColumns[2], mangaAdapter.getCoverURL(position));
                    es.insertToTable(homeDB, homeTable, newValues);
                    alertDialog.hide();
                }
            });
        }

        alertDialog.show();
    }

    class MangaHelper extends AsyncTask<String, Void, JSONObject> {
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
            if (jsonObject == null){
                textViewHint.setVisibility(View.VISIBLE);
                textViewHint.setText(R.string.bad_connection);
                progressBar.setVisibility(View.GONE);
            } else {
                try {
                    mangaIdList.clear();
                    mangaTitleList.clear();
                    mangaCoverList.clear();
                    JSONArray dataArray = jsonObject.getJSONArray(typeData);
                    for (int a = 0; a < dataArray.length(); a++){
                        JSONObject data = dataArray.getJSONObject(a);
                        mangaIdList.add(data.get(typeId).toString());
                        JSONObject attributes = data.getJSONObject(typeAttributes);
                        JSONObject title = attributes.getJSONObject("title");
                        List<String> possibleLanguages = Arrays.asList("en", "ja", "ja-ro");
                        for (String language : possibleLanguages){
                            try {
                                mangaTitleList.add(title.get(language).toString());
                                break;
                            } catch (Exception ignored){
                            }
                        }
                        JSONArray relationships = data.getJSONArray(typeRelationships);
                        for (int b = 0; b < relationships.length(); b++){
                            JSONObject relationship = relationships.getJSONObject(b);
                            if (relationship.get(typeType).equals(typeCoverArt)){
                                mangaCoverList.add(relationship.get(typeId).toString());
                            }
                        }
                    }
                    new CoverHelper().execute(mangaCoverList);
                    if (mangaTitleList.isEmpty()){
                        textViewHint.setVisibility(View.VISIBLE);
                        textViewHint.setText(R.string.no_results_found);
                    }
                } catch (Exception ignored){
                }
            }
        }
    }

    class CoverHelper extends AsyncTask<List<String>, Void, List<String>>{

        @SafeVarargs
        @Override
        protected final List<String> doInBackground(List<String>... lists) {
            try {
                List<String> realCovers = new ArrayList<>();
                for (int a = 0; a < lists[0].size(); a++){
                    URL url = new URL(coverUrl + lists[0].get(a));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod(webGet);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    reader.close();

                    JSONObject data = new JSONObject(stringBuilder.toString()).getJSONObject(typeData);
                    JSONObject attributes = data.getJSONObject(typeAttributes);
                    realCovers.add(coverPictureUrl + mangaIdList.get(a) + forwardSlash + attributes.get(typeFilename) + picRes);
                }
                return realCovers;
            } catch (Exception ignored){
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            if (strings != null){
                progressBar.setVisibility(View.GONE);
                mangaAdapter = new MangaAdapter(mangaIdList, mangaTitleList, strings, SearchFragment.this, SearchFragment.this);
                recyclerView.setAdapter(mangaAdapter);
            }
        }
    }
}