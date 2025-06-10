package com.example.proyeknews;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.proyeknews.database.NewsDatabase;
import com.example.proyeknews.models.Article;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NewsDetailActivity extends AppCompatActivity {

    private Article article;
    private NewsDatabase database;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Button bookmarkButton;
    private Button readMoreButton;
    private boolean isBookmarked = false;
    private FloatingActionButton fabDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Set up back button
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Initialize views
        ImageView imageView = findViewById(R.id.image_news);
        TextView titleText = findViewById(R.id.text_title);
        TextView sourceText = findViewById(R.id.text_source);
        TextView dateText = findViewById(R.id.text_date);
        TextView contentText = findViewById(R.id.text_content);
        bookmarkButton = findViewById(R.id.btn_bookmark);
        readMoreButton = findViewById(R.id.btn_read_more);

        // Initialize dark mode FAB if it exists in layout
        try {
            fabDarkMode = findViewById(R.id.fab_dark_mode);
            if (fabDarkMode != null) {
                fabDarkMode.setOnClickListener(v -> toggleDarkMode());
                updateFabAppearance();
            }
        } catch (Exception e) {
            // FAB might not exist in layout yet
            e.printStackTrace();
        }

        database = NewsDatabase.getInstance(this);

        // Get article from intent
        article = (Article) getIntent().getSerializableExtra("article");

        if (article != null) {
            titleText.setText(article.getTitle());
            sourceText.setText(article.getSource().getName());
            dateText.setText(article.getPublishedAt());
            contentText.setText(article.getContent());

            if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
                Glide.with(this)
                        .load(article.getUrlToImage())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .into(imageView);
            }

            checkIfBookmarked();

            bookmarkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleBookmark();
                }
            });

            // Setup Read More button to open the article URL in a web browser
            readMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openArticleInBrowser();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update FAB appearance when returning to this activity
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
        SharedPreferences prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        prefs.edit().putInt("night_mode", newMode).apply();

        // Apply the new night mode
        AppCompatDelegate.setDefaultNightMode(newMode);
        recreate(); // Important to recreate the activity to apply theme changes
    }

    private void updateFabAppearance() {
        if (fabDarkMode == null) return;

        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // We're in dark mode
            fabDarkMode.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.fab_background_dark)));
            fabDarkMode.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.fab_icon_dark)));
        } else {
            // We're in light mode
            fabDarkMode.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.fab_background_light)));
            fabDarkMode.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.fab_icon_light)));
        }
    }

    private void openArticleInBrowser() {
        if (article != null && article.getUrl() != null && !article.getUrl().isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.getUrl()));
            startActivity(browserIntent);
        } else {
            Toast.makeText(this, "Article URL not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkIfBookmarked() {
        executor.execute(() -> {
            Article savedArticle = database.newsDao().getArticleById(article.getUrl());
            isBookmarked = (savedArticle != null && savedArticle.isBookmarked());
            runOnUiThread(() -> updateBookmarkButton());
        });
    }

    private void toggleBookmark() {
        executor.execute(() -> {
            try {
                // Start a transaction
                database.runInTransaction(() -> {
                    // First toggle the bookmark state
                    boolean newBookmarkState = !isBookmarked;
                    article.setBookmarked(newBookmarkState);

                    // Save to database
                    database.newsDao().insertOrUpdate(article);

                    // Update local state after successful database operation
                    isBookmarked = newBookmarkState;
                });

                runOnUiThread(() -> {
                    updateBookmarkButton();
                    Toast.makeText(NewsDetailActivity.this,
                            isBookmarked ? "Added to bookmarks" : "Removed from bookmarks",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(NewsDetailActivity.this,
                            "Error saving bookmark: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateBookmarkButton() {
        bookmarkButton.setText(isBookmarked ? "Remove Bookmark" : "Add Bookmark");
    }
}