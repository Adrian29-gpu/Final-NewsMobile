package com.example.proyeknews.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyeknews.NewsDetailActivity;
import com.example.proyeknews.R;
import com.example.proyeknews.models.Article;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<Article> articles;
    private boolean isBookmarkList;

    public NewsAdapter(Context context, List<Article> articles) {
        this.context = context;
        this.articles = articles;
        this.isBookmarkList = false;
    }

    public NewsAdapter(Context context, List<Article> articles, boolean isBookmarkList) {
        this.context = context;
        this.articles = articles;
        this.isBookmarkList = isBookmarkList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        Article article = articles.get(position);

        holder.titleText.setText(article.getTitle());
        holder.sourceText.setText(article.getSource().getName());

        if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
            Glide.with(context)
                    .load(article.getUrlToImage())
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NewsDetailActivity.class);
                intent.putExtra("article", article);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    /**
     * Updates the adapter with a new list of articles
     *
     * @param newArticles The new articles to display
     */
    public void updateArticles(List<Article> newArticles) {
        this.articles.clear();
        this.articles.addAll(newArticles);
        notifyDataSetChanged();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleText;
        TextView sourceText;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_news);
            titleText = itemView.findViewById(R.id.text_title);
            sourceText = itemView.findViewById(R.id.text_source);
        }
    }
}