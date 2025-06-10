package com.example.proyeknews.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyeknews.R;
import com.example.proyeknews.adapters.NewsAdapter;
import com.example.proyeknews.database.NewsDatabase;
import com.example.proyeknews.models.Article;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BookmarkFragment extends Fragment {

    private static final String TAG = "BookmarkFragment";
    private RecyclerView recyclerView;
    private NewsAdapter adapter;
    private List<Article> bookmarkedArticles = new ArrayList<>();
    private TextView emptyText;
    private Executor executor = Executors.newSingleThreadExecutor();
    private NewsDatabase database;
    private FloatingActionButton fabDarkMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);

        recyclerView = view.findViewById(R.id.recycler_bookmarks);
        emptyText = view.findViewById(R.id.text_empty_bookmarks);

        database = NewsDatabase.getInstance(requireContext());

        fabDarkMode = view.findViewById(R.id.fab_dark_mode);
        fabDarkMode.setOnClickListener(v -> toggleDarkMode());

        // Update FAB appearance based on current mode
        updateFabAppearance();

        setupRecyclerView();
        // We'll load bookmarks in onResume instead

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Loading bookmarks");
        loadBookmarks();

        // Update FAB appearance when returning to this fragment
        if (fabDarkMode != null) {
            updateFabAppearance();
        }
    }

    private void toggleDarkMode() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        int newMode = (currentMode == AppCompatDelegate.MODE_NIGHT_YES)
                ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_YES;

        // Save the preference before changing mode
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        prefs.edit().putInt("night_mode", newMode).apply();

        // Apply the new night mode
        AppCompatDelegate.setDefaultNightMode(newMode);
    }

    private void updateFabAppearance() {
        if (fabDarkMode == null) return;

        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // We're in dark mode
            fabDarkMode.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.fab_background_dark)));
            fabDarkMode.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.fab_icon_dark)));
        } else {
            // We're in light mode
            fabDarkMode.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.fab_background_light)));
            fabDarkMode.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.fab_icon_light)));
        }
    }

    private void setupRecyclerView() {
        // Create a new adapter instance each time to avoid stale data
        adapter = new NewsAdapter(requireContext(), bookmarkedArticles, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadBookmarks() {
        executor.execute(() -> {
            List<Article> bookmarks = database.newsDao().getBookmarkedArticles();
            Log.d(TAG, "Loaded " + (bookmarks != null ? bookmarks.size() : 0) + " bookmarks from database");

            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    // Clear and update with fresh data
                    bookmarkedArticles.clear();
                    if (bookmarks != null) {
                        bookmarkedArticles.addAll(bookmarks);
                    }

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    updateEmptyState();
                    Log.d(TAG, "UI updated with " + bookmarkedArticles.size() + " bookmarks");
                });
            }
        });
    }

    private void updateEmptyState() {
        if (bookmarkedArticles.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}