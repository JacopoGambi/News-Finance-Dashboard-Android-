package com.example.newsfinance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.newsfinance.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room per gli articoli preferiti.
 */
@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)

    @Delete
    suspend fun deleteArticle(article: ArticleEntity)

    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getAllFavoriteArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM articles WHERE url = :url)")
    fun isArticleFavorite(url: String): Flow<Boolean>
}
