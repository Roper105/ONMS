package com.rlc.onms.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout; // DrawerLayout için gerekli lib
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.rlc.onms.Configs.BuildConfig;
import com.rlc.onms.R;
import com.rlc.onms.Fragments.SaraFragment;
import com.rlc.onms.Fragments.SettingsFragment;
import com.rlc.onms.Fragments.TicketFragment;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout; // DrawerLayout
    private NavigationView navigationView;
    private Toolbar toolbar;

    private String version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // DrawerLayout, NavigationView ve Toolbar
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Versiyon Bilgisi
        String versionName = BuildConfig.VERSION_NAME; // app/build.gradle dosyasından alınır

        MenuItem versionItem = navigationView.getMenu().findItem(R.id.nav_version);
        versionItem.setTitle("Versiyon: " + versionName);
        version = getString(R.string.app_name) + "_" + versionName + ".apk";

        // Menü simgesini toolbar'da gösterin
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_close, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            String defaultFragment = getDefaultFragment();
            if ("Şehirler Arası".equals(defaultFragment)) {
                loadFragment(new SaraFragment(), "Şehirler Arası", R.id.nav_sara);
            } else {
                loadFragment(new TicketFragment(), "Ticket Asistan", R.id.nav_ticket);
            }
        }

        // Navigation dinleyicisi
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_sara:
                    loadFragment(new SaraFragment(), "Şehirler Arası", R.id.nav_sara);
                    break;

                case R.id.nav_ticket:
                    loadFragment(new TicketFragment(), "Ticket Asistan", R.id.nav_ticket);
                    break;

                case R.id.nav_settings:
                    loadFragment(new SettingsFragment(), "Ayarlar", R.id.nav_settings);
                    break;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private String getDefaultFragment() {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        return preferences.getString("default_fragment", "Ticket Asistan"); // Varsayılan fragment "TicketFragment"
    }

    // Fragment yükleme ve Toolbar başlığını güncelleme işlemi birleştirildi
    private void loadFragment(Fragment fragment, String tag, int menuItemId) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit();

        // Navigation Drawer'daki öğeyi işaretleme
        navigationView.setCheckedItem(menuItemId);

        // Toolbar başlığını güncelleme
        updateToolbarTitleByFragment(tag);
    }

    // Toolbar başlığını fragment adına göre güncelle
    private void updateToolbarTitleByFragment(String tag) {
        if (getSupportActionBar() != null) {
            switch (tag) {
                case "Şehirler Arası":
                    getSupportActionBar().setTitle("Şehirler Arası");
                    break;

                case "Ticket Asistan":
                    getSupportActionBar().setTitle("Ticket Asistan");
                    break;
                case "Ayarlar":
                    getSupportActionBar().setTitle("Ayarlar");
                    break;

                // Diğer fragmentlar
            }
        }
    }
}


