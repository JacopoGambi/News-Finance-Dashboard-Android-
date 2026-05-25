package com.example.newsfinance.di

import com.example.newsfinance.data.remote.api.CoinGeckoService
import com.example.newsfinance.data.remote.api.NewsApiService
import com.example.newsfinance.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class NewsRetrofit
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class CoinGeckoRetrofit
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class CoinGeckoOkHttp

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    // Client condiviso — usato da NewsAPI
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Client dedicato a CoinGecko — aggiunge l'header Demo API key
    @Provides
    @Singleton
    @CoinGeckoOkHttp
    fun provideCoinGeckoOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val apiKey = Constants.COINGECKO_API_KEY
            val request = if (apiKey.isNotBlank()) {
                chain.request().newBuilder()
                    .header("x-cg-demo-api-key", apiKey)
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @NewsRetrofit
    fun provideNewsRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.NEWS_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @CoinGeckoRetrofit
    fun provideCoinGeckoRetrofit(@CoinGeckoOkHttp okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.COINGECKO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideNewsApiService(@NewsRetrofit retrofit: Retrofit): NewsApiService =
        retrofit.create(NewsApiService::class.java)

    @Provides
    @Singleton
    fun provideCoinGeckoService(@CoinGeckoRetrofit retrofit: Retrofit): CoinGeckoService =
        retrofit.create(CoinGeckoService::class.java)
}
