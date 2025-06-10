package com.example.proyeknews.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.proyeknews.R;
import com.example.proyeknews.adapters.NewsAdapter;
import com.example.proyeknews.database.NewsDatabase;
import com.example.proyeknews.models.Article;
import com.example.proyeknews.models.NewsResponse;
import com.example.proyeknews.network.NewsApi;
import com.example.proyeknews.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView recyclerView;
    private NewsAdapter adapter;
    private List<Article> articles = new ArrayList<>();
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button refreshButton;
    private View noConnectionView;
    private Executor executor = Executors.newSingleThreadExecutor();
    private NewsDatabase database;
    private EditText searchEditText;
    private ImageButton clearSearchButton;
    private FloatingActionButton fabDarkMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_news);
        progressBar = view.findViewById(R.id.progress_bar);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        noConnectionView = view.findViewById(R.id.no_connection_view);
        searchEditText = view.findViewById(R.id.edit_search);
        clearSearchButton = view.findViewById(R.id.btn_clear_search);

        // Get the refresh button from the included layout
        refreshButton = noConnectionView.findViewById(R.id.btn_refresh);

        database = NewsDatabase.getInstance(requireContext());

        fabDarkMode = view.findViewById(R.id.fab_dark_mode);
        fabDarkMode.setOnClickListener(v -> toggleDarkMode());

        // Update FAB appearance based on current mode
        updateFabAppearance();

        setupRecyclerView();
        setupSearchFunctionality();
        setupSwipeRefresh();

        refreshButton.setOnClickListener(v -> loadNews());

        loadNews();


        return view;
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

    @Override
    public void onResume() {
        super.onResume();
        // Update FAB appearance when returning to this fragment
        updateFabAppearance();
    }
    private void updateFabAppearance() {
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

    private void setupSearchFunctionality() {
        // Clear button functionality
        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            clearSearchButton.setVisibility(View.GONE);
        });

        // Search text change listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                filterArticles(query);
                clearSearchButton.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        // Handle search action from keyboard
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString().trim();
                filterArticles(query);
                return true;
            }
            return false;
        });
    }

    private void filterArticles(String query) {
        List<Article> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            // If search is empty, show all articles
            filteredList.addAll(articles);
        } else {
            // Filter based on title
            for (Article article : articles) {
                if (article.getTitle() != null &&
                        article.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(article);
                }
            }
        }

        if (adapter != null) {
            adapter.updateArticles(filteredList);
        }

        // Debug log
        Log.d(TAG, "Filtering with query: '" + query + "', showing " + filteredList.size() + " articles");
    }

    private void setupRecyclerView() {
        adapter = new NewsAdapter(requireContext(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadNews);
    }

    private void loadNews() {
        if (isNetworkAvailable()) {
            progressBar.setVisibility(View.VISIBLE);
            noConnectionView.setVisibility(View.GONE);

            NewsApi api = RetrofitClient.getClient().create(NewsApi.class);
            Call<NewsResponse> call = api.getTopHeadlines("us", "technology", "00e0eea8c43f4c6cb02effaee4ab274b");

            call.enqueue(new Callback<NewsResponse>() {
                @Override
                public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                    if (!isAdded()) return; // Check if fragment is still attached

                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (response.isSuccessful() && response.body() != null) {
                        articles.clear();
                        List<Article> newArticles = response.body().getArticles();

                        if (newArticles != null && !newArticles.isEmpty()) {
                            articles.addAll(newArticles);
                            Log.d(TAG, "Loaded " + articles.size() + " articles from API");

                            // Apply current search filter
                            String currentQuery = searchEditText != null ?
                                    searchEditText.getText().toString().trim() : "";
                            filterArticles(currentQuery);

                            saveToDatabase(newArticles);
                        } else {
                            Log.d(TAG, "API returned empty article list");
                            showEmptyState("No articles available");
                        }
                    } else {
                        int errorCode = response.code();
                        Log.e(TAG, "API error: " + errorCode);
                        Toast.makeText(requireContext(), "Error loading articles: " + errorCode, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<NewsResponse> call, Throwable t) {
                    if (!isAdded()) return;

                    Log.e(TAG, "API failure: " + t.getMessage(), t);
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    noConnectionView.setVisibility(View.VISIBLE);
                    updateNoConnectionMessage("Failed to load news: " + t.getMessage());
                }
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
            noConnectionView.setVisibility(View.VISIBLE);
            updateNoConnectionMessage("No Internet Connection");
            loadFromDatabase();
        }
    }

    private void saveToDatabase(List<Article> articles) {
        executor.execute(() -> {
            if (database != null) {
                try {
                    // Get all currently bookmarked articles
                    List<Article> bookmarked = database.newsDao().getBookmarkedArticles();

                    // Create a map of bookmarked URLs for quick lookup
                    Map<String, Boolean> bookmarkMap = new HashMap<>();
                    for (Article bookmark : bookmarked) {
                        bookmarkMap.put(bookmark.getUrl(), true);
                    }

                    // Preserve bookmark status in new articles
                    for (Article article : articles) {
                        if (bookmarkMap.containsKey(article.getUrl())) {
                            article.setBookmarked(true);
                        }
                    }

                    // Now replace all articles but with bookmark status preserved
                    database.newsDao().deleteAllArticles();
                    database.newsDao().insertAll(articles);
                    Log.d(TAG, "Saved " + articles.size() + " articles to database");
                } catch (Exception e) {
                    Log.e(TAG, "Database error: " + e.getMessage(), e);
                }
            }
        });
    }

    private void loadFromDatabase() {
        executor.execute(() -> {
            try {
                final List<Article> savedArticles = database.newsDao().getAllArticles();
                Log.d(TAG, "Loaded " + (savedArticles != null ? savedArticles.size() : 0) + " articles from database");

                // Make sure we're still attached to an activity
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    articles.clear();
                    if (savedArticles != null && !savedArticles.isEmpty()) {
                        articles.addAll(savedArticles);

                        // Apply current search filter
                        String currentQuery = searchEditText != null ?
                                searchEditText.getText().toString().trim() : "";
                        filterArticles(currentQuery);

                        noConnectionView.setVisibility(View.GONE);
                    } else {
                        updateNoConnectionMessage("No saved articles found");
                        noConnectionView.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading from database: " + e.getMessage(), e);
            }
        });
    }

    private void updateNoConnectionMessage(String message) {
        TextView messageView = noConnectionView.findViewById(R.id.tv_message);
        if (messageView != null) {
            messageView.setText(message);
        }
    }

    private void showEmptyState(String message) {
        noConnectionView.setVisibility(View.VISIBLE);
        updateNoConnectionMessage(message);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }


}