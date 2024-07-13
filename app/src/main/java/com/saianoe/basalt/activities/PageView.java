package com.saianoe.basalt.activities;

import static com.saianoe.basalt.others.Constants.chapterEND;
import static com.saianoe.basalt.others.Constants.chapterSTART;
import static com.saianoe.basalt.others.Constants.dataSaver;
import static com.saianoe.basalt.others.Constants.downloadPathString;
import static com.saianoe.basalt.others.Constants.downloadPathNotSet;
import static com.saianoe.basalt.others.Constants.feedUrl;
import static com.saianoe.basalt.others.Constants.forwardSlash;
import static com.saianoe.basalt.others.Constants.intentFromChapter;
import static com.saianoe.basalt.others.Constants.intentFromManga;
import static com.saianoe.basalt.others.Constants.prefs;
import static com.saianoe.basalt.others.Constants.prefsSettings;
import static com.saianoe.basalt.others.Constants.readDB;
import static com.saianoe.basalt.others.Constants.readTable;
import static com.saianoe.basalt.others.Constants.readTableColumns;
import static com.saianoe.basalt.others.Constants.typeData;
import static com.saianoe.basalt.others.Constants.typeDataSaver;
import static com.saianoe.basalt.others.Constants.typeDataSaver2;
import static com.saianoe.basalt.others.Constants.webGet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.documentfile.provider.DocumentFile;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.saianoe.basalt.R;
import com.saianoe.basalt.others.EasySQL;
import com.saianoe.basalt.others.Fullscreen;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PageView extends AppCompatActivity {

    ViewPager viewPager;
    TextView pageCounter;
    final List<String> pageImageUrls = new ArrayList<>();
    String[] chapterIds = new String[1];
    String[] chapterTitles = new String[1];
    int originalPosition = 0;
    boolean hidden = false;
    SeekBar slider;
    ConstraintLayout sliderContainer;
    EasySQL es;
    int pager = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_view);

        prefs = getSharedPreferences(prefsSettings, MODE_PRIVATE);
        es = new EasySQL(this);

        slider = findViewById(R.id.pageslidepicker);
        sliderContainer = findViewById(R.id.slidercontainer);

        hide();
        hidden = true;

        pageCounter = findViewById(R.id.page_counter);
        viewPager = findViewById(R.id.vp);

        chapterIds = getIntent().getStringArrayExtra(intentFromChapter[1]);
        chapterTitles = getIntent().getStringArrayExtra(intentFromChapter[2]);
        originalPosition = getIntent().getIntExtra(intentFromChapter[0], 0);

        try {
            Uri uri = Uri.parse(prefs.getString(downloadPathString, downloadPathNotSet));
            DocumentFile uriDocumentFile = DocumentFile.fromTreeUri(this, uri);

            assert uriDocumentFile != null;
            if (uriDocumentFile.findFile(Objects.requireNonNull(getIntent().getStringExtra(intentFromManga[0]))) != null){
                uriDocumentFile = uriDocumentFile.findFile(Objects.requireNonNull(getIntent().getStringExtra(intentFromManga[0])));
                assert uriDocumentFile != null;
                if (uriDocumentFile.findFile(chapterIds[originalPosition]) != null){
                    uriDocumentFile = uriDocumentFile.findFile(chapterIds[originalPosition]);
                    if (pager == 1 || pager == -1){
                        int chpos = originalPosition - pager;
                        if (!inReadDB(chapterIds[chpos])){
                            Map<String, String> newReadValues = new HashMap<>();
                            newReadValues.put(readTableColumns[0], getIntent().getStringExtra(intentFromManga[0]));
                            newReadValues.put(readTableColumns[1], chapterIds[chpos]);
                            newReadValues.put(readTableColumns[2], chapterTitles[chpos]);
                            es.insertToTable(readDB, readTable, newReadValues);
                        }
                        pager = 0;
                    }
                    pageImageUrls.clear();
                    pageImageUrls.add(chapterSTART);
                    for (int a = 0; a < Objects.requireNonNull(uriDocumentFile).listFiles().length; a++){
                        pageImageUrls.add(uriDocumentFile.listFiles()[a].getUri().toString());
                    }
                    pageImageUrls.add(chapterEND);
                    String page = 1 + forwardSlash + (pageImageUrls.size() - 2);
                    pageCounter.setText(page);
                    PageAdapter pageAdapter = new PageAdapter(PageView.this, pageImageUrls, true);
                    viewPager.setAdapter(pageAdapter);
                    viewPager.setCurrentItem(1);
                    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            int localPosition = (position - 1) + 1;
                            if (localPosition >= pageImageUrls.size() - 2){
                                localPosition = pageImageUrls.size() - 2;
                            }
                            if (localPosition <= 0){
                                localPosition = 1;
                            }
                            String currentPage = localPosition + forwardSlash + (pageImageUrls.size() - 2);
                            slider.setProgress(localPosition - 1);
                            pageCounter.setText(currentPage);
                            if (localPosition == (pageImageUrls.size() - 2)){
                                pager = 1;
                            }
                        }

                        @Override
                        public void onPageSelected(int position) {

                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });
                    slider.setMax(pageImageUrls.size() - 3);
                    slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            int localPosition = progress + 1;
                            if (localPosition >= pageImageUrls.size() - 2){
                                localPosition = pageImageUrls.size() - 2;
                            }
                            if (localPosition <= 0){
                                localPosition = 1;
                            }
                            String currentPage = localPosition + forwardSlash + (pageImageUrls.size() - 2);
                            pageCounter.setText(currentPage);
                            if (localPosition == (pageImageUrls.size() - 2)){
                                pager = 1;
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            viewPager.setCurrentItem(seekBar.getProgress() + 1, false);
                        }
                    });
                    Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                } else {
                    Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                    new PageHelper().execute(feedUrl + chapterIds[originalPosition]);
                }
            } else {
                Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                new PageHelper().execute(feedUrl + chapterIds[originalPosition]);
            }
        } catch (Exception ignored){
            Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
            new PageHelper().execute(feedUrl + chapterIds[originalPosition]);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("original_position", originalPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        originalPosition = savedInstanceState.getInt("original_position");
        try {
            Uri uri = Uri.parse(prefs.getString(downloadPathString, downloadPathNotSet));
            DocumentFile uriDocumentFile = DocumentFile.fromTreeUri(this, uri);

            assert uriDocumentFile != null;
            if (uriDocumentFile.findFile(Objects.requireNonNull(getIntent().getStringExtra(intentFromManga[0]))) != null){
                uriDocumentFile = uriDocumentFile.findFile(Objects.requireNonNull(getIntent().getStringExtra(intentFromManga[0])));
                assert uriDocumentFile != null;
                if (uriDocumentFile.findFile(chapterIds[originalPosition]) != null){
                    uriDocumentFile = uriDocumentFile.findFile(chapterIds[originalPosition]);
                    if (pager == 1 || pager == -1){
                        int chpos = originalPosition - pager;
                        if (!inReadDB(chapterIds[chpos])){
                            Map<String, String> newReadValues = new HashMap<>();
                            newReadValues.put(readTableColumns[0], getIntent().getStringExtra(intentFromManga[0]));
                            newReadValues.put(readTableColumns[1], chapterIds[chpos]);
                            newReadValues.put(readTableColumns[2], chapterTitles[chpos]);
                            es.insertToTable(readDB, readTable, newReadValues);
                        }
                        pager = 0;
                    }
                    pageImageUrls.clear();
                    pageImageUrls.add(chapterSTART);
                    for (int a = 0; a < Objects.requireNonNull(uriDocumentFile).listFiles().length; a++){
                        pageImageUrls.add(uriDocumentFile.listFiles()[a].getUri().toString());
                    }
                    pageImageUrls.add(chapterEND);
                    String page = 1 + forwardSlash + (pageImageUrls.size() - 2);
                    pageCounter.setText(page);
                    PageAdapter pageAdapter = new PageAdapter(PageView.this, pageImageUrls, true);
                    viewPager.setAdapter(pageAdapter);
                    viewPager.setCurrentItem(1);
                    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            int localPosition = (position - 1) + 1;
                            if (localPosition >= pageImageUrls.size() - 2){
                                localPosition = pageImageUrls.size() - 2;
                            }
                            if (localPosition <= 0){
                                localPosition = 1;
                            }
                            String currentPage = localPosition + forwardSlash + (pageImageUrls.size() - 2);
                            slider.setProgress(localPosition - 1);
                            pageCounter.setText(currentPage);
                            if (localPosition == (pageImageUrls.size() - 2)){
                                pager = 1;
                            }
                        }

                        @Override
                        public void onPageSelected(int position) {

                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });
                    slider.setMax(pageImageUrls.size() - 3);
                    slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            int localPosition = progress + 1;
                            if (localPosition >= pageImageUrls.size() - 2){
                                localPosition = pageImageUrls.size() - 2;
                            }
                            if (localPosition <= 0){
                                localPosition = 1;
                            }
                            String currentPage = localPosition + forwardSlash + (pageImageUrls.size() - 2);
                            pageCounter.setText(currentPage);
                            if (localPosition == (pageImageUrls.size() - 2)){
                                pager = 1;
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            viewPager.setCurrentItem(seekBar.getProgress() + 1, false);
                        }
                    });
                    Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                } else {
                    Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                    new PageHelper().execute(feedUrl + chapterIds[originalPosition]);
                }
            } else {
                Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                new PageHelper().execute(feedUrl + chapterIds[originalPosition]);
            }
        } catch (Exception ignored){
            Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
            new PageHelper().execute(feedUrl + chapterIds[originalPosition]);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pager == 1 || pager == -1){
            if (!inReadDB(chapterIds[originalPosition])){
                Map<String, String> newReadValues = new HashMap<>();
                newReadValues.put(readTableColumns[0], getIntent().getStringExtra(intentFromManga[0]));
                newReadValues.put(readTableColumns[1], chapterIds[originalPosition]);
                newReadValues.put(readTableColumns[2], chapterTitles[originalPosition]);
                es.insertToTable(readDB, readTable, newReadValues);
            }
            pager = 0;
        }
    }

    boolean inReadDB(String chapterId){
        List<Map<String, String>> readMangaValues = es.getTableValues(readDB, readTable);

        List<String> temporaryReadMangaIds = new ArrayList<>();
        List<String> temporaryReadMangaChapterIds = new ArrayList<>();

        for (Map<String, String> value : readMangaValues){
            for (Map.Entry<String, String> entry : value.entrySet()){
                if (entry.getKey().equals(readTableColumns[0])){
                    temporaryReadMangaIds.add(entry.getValue());
                }
                if (entry.getKey().equals(readTableColumns[1])){
                    temporaryReadMangaChapterIds.add(entry.getValue());
                }
            }
        }

        for (int a = 0; a < temporaryReadMangaIds.size(); a++){
            if (temporaryReadMangaIds.get(a).equals(getIntent().getStringExtra(intentFromManga[0])) &&
                    temporaryReadMangaChapterIds.get(a).equals(chapterId)){
                return true;
            }
        }
        return false;
    }

    void hide(){
        Fullscreen.enableFullscreen(this);
        sliderContainer.setVisibility(View.GONE);
    }

    void show(){
        Fullscreen.disableFullscreen(this);
        sliderContainer.setVisibility(View.VISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    class PageHelper extends AsyncTask<String, Void, JSONObject> {

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
                    if (pager == 1 || pager == -1){
                        int chpos = originalPosition - pager;
                        if (!inReadDB(chapterIds[chpos])){
                            Map<String, String> newReadValues = new HashMap<>();
                            newReadValues.put(readTableColumns[0], getIntent().getStringExtra(intentFromManga[0]));
                            newReadValues.put(readTableColumns[1], chapterIds[chpos]);
                            newReadValues.put(readTableColumns[2], chapterTitles[chpos]);
                            es.insertToTable(readDB, readTable, newReadValues);
                        }
                        pager = 0;
                    }
                    pageImageUrls.clear();
                    String baseUrl = jsonObject.getString("baseUrl");
                    JSONObject chapter = jsonObject.getJSONObject("chapter");
                    String dataMode = prefs.getBoolean(dataSaver, false) ? typeDataSaver : typeData;
                    String dataUrlMode = prefs.getBoolean(dataSaver, false) ? typeDataSaver2 : typeData;
                    JSONArray data = chapter.getJSONArray(dataMode);
                    pageImageUrls.add(chapterSTART);
                    for (int a = 0; a < data.length(); a++){
                        pageImageUrls.add(baseUrl + forwardSlash + dataUrlMode + forwardSlash + chapter.get("hash") + forwardSlash + data.get(a));
                    }
                    pageImageUrls.add(chapterEND);
                    String page = 1 + forwardSlash + (pageImageUrls.size() - 2);
                    pageCounter.setText(page);
                    PageAdapter pageAdapter = new PageAdapter(PageView.this, pageImageUrls, false);
                    viewPager.setAdapter(pageAdapter);
                    viewPager.setCurrentItem(1);
                    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            int localPosition = (position - 1) + 1;
                            if (localPosition >= pageImageUrls.size() - 2){
                                localPosition = pageImageUrls.size() - 2;
                            }
                            if (localPosition <= 0){
                                localPosition = 1;
                            }
                            String currentPage = localPosition + forwardSlash + (pageImageUrls.size() - 2);
                            slider.setProgress(localPosition - 1);
                            pageCounter.setText(currentPage);
                            if (localPosition == (pageImageUrls.size() - 2)){
                                pager = 1;
                            }
                        }

                        @Override
                        public void onPageSelected(int position) {

                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });
                    slider.setMax(pageImageUrls.size() - 3);
                    slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            int localPosition = progress + 1;
                            if (localPosition >= pageImageUrls.size() - 2){
                                localPosition = pageImageUrls.size() - 2;
                            }
                            if (localPosition <= 0){
                                localPosition = 1;
                            }
                            String currentPage = localPosition + forwardSlash + (pageImageUrls.size() - 2);
                            pageCounter.setText(currentPage);
                            if (localPosition == (pageImageUrls.size() - 2)){
                                pager = 1;
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            viewPager.setCurrentItem(seekBar.getProgress() + 1, false);
                        }
                    });
                }
            } catch (Exception ignored){
            }
        }
    }

    class PageAdapter extends PagerAdapter {

        final List<String> imageUrls;
        final Context context;
        final LayoutInflater li;
        final boolean downloaded;

        public PageAdapter(Context context, List<String> imageUrls, boolean downloaded) {
            this.context = context;
            this.imageUrls = imageUrls;
            li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.downloaded = downloaded;
        }

        @Override
        public int getCount() {
            return imageUrls.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == ((LinearLayout) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View itemView = li.inflate(R.layout.item_page, container, false);
            TextView textView = itemView.findViewById(R.id.item_page_text_view);
            TextView textView1 = itemView.findViewById(R.id.item_page_text_view_prev);
            Button button = itemView.findViewById(R.id.item_page_next);
            Button button1 = itemView.findViewById(R.id.item_page_prev);
            ConstraintLayout pageLayout = itemView.findViewById(R.id.pagelayout);
            ImageView imageView = itemView.findViewById(R.id.item_page_image_view);
            if (imageUrls.get(position).equals(chapterEND) || imageUrls.get(position).equals(chapterSTART)){
                if (imageUrls.get(position).equals(chapterEND)){
                    textView.setVisibility(View.VISIBLE);
                    if (originalPosition != chapterIds.length - 1){
                        button.setVisibility(View.VISIBLE);
                        button.setOnClickListener(v -> {
                            imageUrls.clear();
                            textView.setVisibility(View.GONE);
                            button.setVisibility(View.GONE);
                            viewPager.setAdapter(null);
                            originalPosition++;
                            try {
                                Uri uri = Uri.parse(prefs.getString(downloadPathString, downloadPathNotSet));
                                DocumentFile uriDocumentFile = DocumentFile.fromTreeUri(PageView.this, uri);

                                assert uriDocumentFile != null;
                                if (uriDocumentFile.findFile(Objects.requireNonNull(getIntent().getStringExtra(intentFromManga[0]))) != null){
                                    uriDocumentFile = uriDocumentFile.findFile(Objects.requireNonNull(getIntent().getStringExtra(intentFromManga[0])));
                                    assert uriDocumentFile != null;
                                    if (uriDocumentFile.findFile(chapterIds[originalPosition]) != null){
                                        uriDocumentFile = uriDocumentFile.findFile(chapterIds[originalPosition]);
                                        if (pager == 1 || pager == -1){
                                            int chpos = originalPosition - pager;
                                            if (!inReadDB(chapterIds[chpos])){
                                                Map<String, String> newReadValues = new HashMap<>();
                                                newReadValues.put(readTableColumns[0], getIntent().getStringExtra(intentFromManga[0]));
                                                newReadValues.put(readTableColumns[1], chapterIds[chpos]);
                                                newReadValues.put(readTableColumns[2], chapterTitles[chpos]);
                                                es.insertToTable(readDB, readTable, newReadValues);
                                            }
                                            pager = 0;
                                        }
                                        imageUrls.clear();
                                        imageUrls.add(chapterSTART);
                                        for (int a = 0; a < Objects.requireNonNull(uriDocumentFile).listFiles().length; a++){
                                            imageUrls.add(uriDocumentFile.listFiles()[a].getUri().toString());
                                        }
                                        imageUrls.add(chapterEND);
                                        String page = 1 + forwardSlash + (pageImageUrls.size() - 2);
                                        pageCounter.setText(page);
                                        PageAdapter pageAdapter = new PageAdapter(PageView.this, pageImageUrls, true);
                                        viewPager.setAdapter(pageAdapter);
                                        viewPager.setCurrentItem(1);
                                        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                            @Override
                                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                                int localPosition = (position - 1) + 1;
                                                if (localPosition >= pageImageUrls.size() - 2){
                                                    localPosition = pageImageUrls.size() - 2;
                                                }
                                                if (localPosition <= 0){
                                                    localPosition = 1;
                                                }
                                                String currentPage = localPosition + forwardSlash + (pageImageUrls.size() - 2);
                                                slider.setProgress(localPosition - 1);
                                                pageCounter.setText(currentPage);
                                                if (localPosition == (pageImageUrls.size() - 2)){
                                                    pager = 1;
                                                }
                                            }

                                            @Override
                                            public void onPageSelected(int position) {

                                            }

                                            @Override
                                            public void onPageScrollStateChanged(int state) {

                                            }
                                        });
                                        slider.setMax(pageImageUrls.size() - 3);
                                        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                            @Override
                                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                int localPosition = progress + 1;
                                                if (localPosition >= pageImageUrls.size() - 2){
                                                    localPosition = pageImageUrls.size() - 2;
                                                }
                                                if (localPosition <= 0){
                                                    localPosition = 1;
                                                }
                                                String currentPage = localPosition + forwardSlash + (pageImageUrls.size() - 2);
                                                pageCounter.setText(currentPage);
                                                if (localPosition == (pageImageUrls.size() - 2)){
                                                    pager = 1;
                                                }
                                            }

                                            @Override
                                            public void onStartTrackingTouch(SeekBar seekBar) {

                                            }

                                            @Override
                                            public void onStopTrackingTouch(SeekBar seekBar) {
                                                viewPager.setCurrentItem(seekBar.getProgress() + 1, false);
                                            }
                                        });
                                        Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                                    } else {
                                        Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                                        new PageHelper().execute(feedUrl + chapterIds[originalPosition]);
                                    }
                                } else {
                                    Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                                    new PageHelper().execute(feedUrl + chapterIds[originalPosition]);
                                }
                            } catch (Exception ignored){
                                Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                                new PageHelper().execute(feedUrl + chapterIds[originalPosition]);
                            }
                        });
                    }
                }
                if (imageUrls.get(position).equals(chapterSTART)){
                    textView1.setVisibility(View.VISIBLE);
                    if (originalPosition != 0){
                        button1.setVisibility(View.VISIBLE);
                        button1.setOnClickListener(v -> {
                            imageUrls.clear();
                            textView.setVisibility(View.GONE);
                            button.setVisibility(View.GONE);
                            viewPager.setAdapter(null);
                            originalPosition--;
                            if (pager == 1){
                                pager = -1;
                            }
                            try {
                                Uri uri = Uri.parse(prefs.getString(downloadPathString, downloadPathNotSet));
                                DocumentFile uriDocumentFile = DocumentFile.fromTreeUri(PageView.this, uri);

                                assert uriDocumentFile != null;
                                if (uriDocumentFile.findFile(Objects.requireNonNull(getIntent().getStringExtra(intentFromManga[0]))) != null){
                                    uriDocumentFile = uriDocumentFile.findFile(Objects.requireNonNull(getIntent().getStringExtra(intentFromManga[0])));
                                    assert uriDocumentFile != null;
                                    if (uriDocumentFile.findFile(chapterIds[originalPosition]) != null){
                                        uriDocumentFile = uriDocumentFile.findFile(chapterIds[originalPosition]);
                                        if (pager == 1 || pager == -1){
                                            int chpos = originalPosition - pager;
                                            if (!inReadDB(chapterIds[chpos])){
                                                Map<String, String> newReadValues = new HashMap<>();
                                                newReadValues.put(readTableColumns[0], getIntent().getStringExtra(intentFromManga[0]));
                                                newReadValues.put(readTableColumns[1], chapterIds[chpos]);
                                                newReadValues.put(readTableColumns[2], chapterTitles[chpos]);
                                                es.insertToTable(readDB, readTable, newReadValues);
                                            }
                                            pager = 0;
                                        }
                                        imageUrls.clear();
                                        imageUrls.add(chapterSTART);
                                        for (int a = 0; a < Objects.requireNonNull(uriDocumentFile).listFiles().length; a++){
                                            imageUrls.add(uriDocumentFile.listFiles()[a].getUri().toString());
                                        }
                                        imageUrls.add(chapterEND);
                                        String page = 1 + forwardSlash + (pageImageUrls.size() - 2);
                                        pageCounter.setText(page);
                                        PageAdapter pageAdapter = new PageAdapter(PageView.this, pageImageUrls, true);
                                        viewPager.setAdapter(pageAdapter);
                                        viewPager.setCurrentItem(1);
                                        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                            @Override
                                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                                int localPosition = (position - 1) + 1;
                                                if (localPosition >= pageImageUrls.size() - 2){
                                                    localPosition = pageImageUrls.size() - 2;
                                                }
                                                if (localPosition <= 0){
                                                    localPosition = 1;
                                                }
                                                String currentPage = localPosition + forwardSlash + (pageImageUrls.size() - 2);
                                                slider.setProgress(localPosition - 1);
                                                pageCounter.setText(currentPage);
                                                if (localPosition == (pageImageUrls.size() - 2)){
                                                    pager = 1;
                                                }
                                            }

                                            @Override
                                            public void onPageSelected(int position) {

                                            }

                                            @Override
                                            public void onPageScrollStateChanged(int state) {

                                            }
                                        });
                                        slider.setMax(pageImageUrls.size() - 3);
                                        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                            @Override
                                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                int localPosition = progress + 1;
                                                if (localPosition >= pageImageUrls.size() - 2){
                                                    localPosition = pageImageUrls.size() - 2;
                                                }
                                                if (localPosition <= 0){
                                                    localPosition = 1;
                                                }
                                                String currentPage = localPosition + forwardSlash + (pageImageUrls.size() - 2);
                                                pageCounter.setText(currentPage);
                                                if (localPosition == (pageImageUrls.size() - 2)){
                                                    pager = 1;
                                                }
                                            }

                                            @Override
                                            public void onStartTrackingTouch(SeekBar seekBar) {

                                            }

                                            @Override
                                            public void onStopTrackingTouch(SeekBar seekBar) {
                                                viewPager.setCurrentItem(seekBar.getProgress() + 1, false);
                                            }
                                        });
                                        Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                                    } else {
                                        Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                                        new PageHelper().execute(feedUrl + chapterIds[originalPosition]);
                                    }
                                } else {
                                    Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                                    new PageHelper().execute(feedUrl + chapterIds[originalPosition]);
                                }
                            } catch (Exception ignored){
                                Objects.requireNonNull(getSupportActionBar()).setTitle(chapterTitles[originalPosition]);
                                new PageHelper().execute(feedUrl + chapterIds[originalPosition]);
                            }
                        });
                    }
                }
            } else {
                int maxWidth = 1080;
                int maxHeight = 1524;
                if (downloaded){
                    Uri uri = Uri.parse(imageUrls.get(position));
                    Picasso.get().load(uri).resize(maxWidth, maxHeight).centerInside().into(imageView);
                } else {
                    Picasso.get().load(imageUrls.get(position)).resize(maxWidth, maxHeight).centerInside().into(imageView);
                }
            }
            imageView.setOnClickListener(v -> {
                if (hidden){
                    show();
                    hidden = false;
                } else {
                    hide();
                    hidden = true;
                }
            });
            pageLayout.setOnClickListener(v -> {
                if (hidden){
                    show();
                    hidden = false;
                } else {
                    hide();
                    hidden = true;
                }
            });
            Objects.requireNonNull(container).addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((LinearLayout) object);
        }
    }
}