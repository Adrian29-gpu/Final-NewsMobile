package com.example.proyeknews.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.proyeknews.models.Article;

import java.util.List;

@Dao
public interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Article> articles);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(Article article);

    @Update
    void updateArticle(Article article);

    @Query("SELECT * FROM articles")
    List<Article> getAllArticles();

    @Query("SELECT * FROM articles WHERE isBookmarked = 1")
    List<Article> getBookmarkedArticles();

    @Query("SELECT * FROM articles WHERE url = :articleUrl LIMIT 1")
    Article getArticleById(String articleUrl);

    @Query("DELETE FROM articles")
    void deleteAllArticles();
}