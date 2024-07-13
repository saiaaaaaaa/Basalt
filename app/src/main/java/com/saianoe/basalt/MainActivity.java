package com.saianoe.basalt;

import static com.saianoe.basalt.others.Constants.prefs;
import static com.saianoe.basalt.others.Constants.prefsEditor;
import static com.saianoe.basalt.others.Constants.prefsSettings;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.saianoe.basalt.activities.SettingsActivity;
import com.saianoe.basalt.fragments.HomeFragment;
import com.saianoe.basalt.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    public static BottomNavigationView bnv;

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
}