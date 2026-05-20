package com.example.newsfinance.di

import android.content.Context
import androidx.room.Room
import com.example.newsfinance.data.local.AppDatabase
import com.example.newsfinance.data.local.dao.ArticleDao
import com.example.newsfinance.data.local.dao.CryptoDao
import com.example.newsfinance.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Modulo Hilt per il layer di persistenza locale (Room).
 * Fornisce AppDatabase singleton e i DAO.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        Constants.DATABASE_NAME
    ).build()

    @Provides
    fun provideArticleDao(db: AppDatabase): ArticleDao = db.articleDao()

    @Provides
    fun provideCryptoDao(db: AppDatabase): CryptoDao = db.cryptoDao()
}
