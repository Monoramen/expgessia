package app.expgessia.di

import android.content.Context
import app.expgessia.data.AppDatabase
import app.expgessia.data.dao.CharacteristicDao
import app.expgessia.data.dao.DailyStatsDao
import app.expgessia.data.dao.TaskCompletionDao
import app.expgessia.data.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserDao(database: AppDatabase) = database.userDao()

    @Provides
    fun provideCharacteristicDao(appDatabase: AppDatabase): CharacteristicDao {
        return appDatabase.characteristicDao()
    }

    @Provides
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao {
        return appDatabase.taskDao()
    }

    @Provides
    fun provideTaskCompletionDao(appDatabase: AppDatabase): TaskCompletionDao {
        return appDatabase.taskCompletionDao()
    }

    @Provides
    fun provideDailyStatsDao(appDatabase: AppDatabase): DailyStatsDao {
        return appDatabase.dailyStatsDao()
    }

}