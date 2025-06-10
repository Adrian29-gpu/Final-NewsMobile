package com.example.proyeknews.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.proyeknews.models.Article;

@Database(entities = {Article.class}, version = 4, exportSchema = false)
public abstract class NewsDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "news_db";
    private static NewsDatabase instance;

    public abstract NewsDao newsDao();

    // Define migrations for handling existing installations
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add isBookmarked column to articles table if it doesn't exist
            database.execSQL("ALTER TABLE articles ADD COLUMN isBookmarked INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Fix source field storage format
            database.execSQL("CREATE TABLE articles_temp (" +
                    "url TEXT PRIMARY KEY NOT NULL, " +
                    "source_id TEXT, " +
                    "source_name TEXT, " +
                    "author TEXT, " +
                    "title TEXT, " +
                    "description TEXT, " +
                    "urlToImage TEXT, " +
                    "publishedAt TEXT, " +
                    "content TEXT, " +
                    "isBookmarked INTEGER NOT NULL DEFAULT 0)");

            // Try to copy data with best effort
            try {
                database.execSQL("INSERT INTO articles_temp (url, author, title, description, urlToImage, publishedAt, content, isBookmarked) " +
                        "SELECT url, author, title, description, urlToImage, publishedAt, content, isBookmarked FROM articles");
            } catch (Exception e) {
                // If schema mismatch, just proceed
            }

            database.execSQL("DROP TABLE articles");
            database.execSQL("ALTER TABLE articles_temp RENAME TO articles");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Ensure we have the correct schema and preserve bookmark data
            database.execSQL("DROP TABLE IF EXISTS articles_new");
            database.execSQL("CREATE TABLE articles_new (" +
                    "url TEXT PRIMARY KEY NOT NULL, " +
                    "source_id TEXT, " +
                    "source_name TEXT, " +
                    "author TEXT, " +
                    "title TEXT, " +
                    "description TEXT, " +
                    "urlToImage TEXT, " +
                    "publishedAt TEXT, " +
                    "content TEXT, " +
                    "isBookmarked INTEGER NOT NULL DEFAULT 0)");

            // Copy existing data with bookmarks preserved
            database.execSQL("INSERT OR IGNORE INTO articles_new " +
                    "SELECT url, source_id, source_name, author, title, description, urlToImage, publishedAt, content, isBookmarked FROM articles");

            database.execSQL("DROP TABLE articles");
            database.execSQL("ALTER TABLE articles_new RENAME TO articles");
        }
    };

    public static synchronized NewsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            NewsDatabase.class,
                            DATABASE_NAME)
                    .fallbackToDestructiveMigration() // Will wipe data if migrations fail
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build();
        }
        return instance;
    }
}