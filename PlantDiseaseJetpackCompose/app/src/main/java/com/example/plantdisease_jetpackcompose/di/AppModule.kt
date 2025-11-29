package com.example.plantdisease_jetpackcompose.di

import android.content.Context
import com.example.plantdisease_jetpackcompose.data.ml.TFLiteClassifier
import com.example.plantdisease_jetpackcompose.data.repository.ImageClassifierRepositoryImpl
import com.example.plantdisease_jetpackcompose.domain.repository.ImageClassifierRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTFLiteClassifier(
        @ApplicationContext context: Context
    ): TFLiteClassifier = TFLiteClassifier(context)

    @Provides
    @Singleton
    fun provideImageClassifierRepository(
        classifier: TFLiteClassifier
    ): ImageClassifierRepository =
        ImageClassifierRepositoryImpl(classifier)
}