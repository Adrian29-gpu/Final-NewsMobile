package com.example.proyeknews;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.proyeknews.fragments.BookmarkFragment;
import com.example.proyeknews.fragments.HomeFragment;
import com.example.proyeknews.utils.NetworkConnectionLiveData;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private NetworkConnectionLiveData networkConnectionLiveData;
    private View noConnectionLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate started");

        try {
            // Initialize network connection monitoring
            noConnectionLayout = findViewById(R.id.layout_no_connection);
            Button btnRefresh = noConnectionLayout.findViewById(R.id.btn_refresh);
            btnRefresh.setOnClickListener(v -> {
                // Refresh content if network is available
                if (networkConnectionLiveData.getValue() != null && networkConnectionLiveData.getValue()) {
                    noConnectionLayout.setVisibility(View.GONE);
                    refreshCurrentFragment();
                }
            });

            networkConnectionLiveData = new NetworkConnectionLiveData(this);
            networkConnectionLiveData.observe(this, isConnected -> {
                if (isConnected) {
                    // Connected to network, hide no connection layout
                    noConnectionLayout.setVisibility(View.GONE);
                } else {
                    // No connection, show no connection layout
                    noConnectionLayout.setVisibility(View.VISIBLE);
                }
            });

            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            if (bottomNav == null) {
                throw new NullPointerException("BottomNavigationView not found");
            }
            Log.d(TAG, "BottomNavigationView initialized");

            bottomNav.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_bookmarks) {
                    selectedFragment = new BookmarkFragment();
                }

                if (selectedFragment != null) {
                    Log.d(TAG, "Switching to fragment: " + selectedFragment.getClass().getSimpleName());
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                } else {
                    Log.e(TAG, "Selected fragment is null");
                }

                return true;
            });

            if (savedInstanceState == null) {
                Log.d(TAG, "Loading default fragment: HomeFragment");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void refreshCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .detach(currentFragment)
                    .attach(currentFragment)
                    .commit();
        }
    }
}