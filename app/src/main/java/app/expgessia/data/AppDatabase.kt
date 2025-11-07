package app.expgessia.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import app.expgessia.data.converter.DateConverter
import app.expgessia.data.dao.CharacteristicDao
import app.expgessia.data.dao.DailyStatsDao
import app.expgessia.data.dao.TaskDao
import app.expgessia.data.dao.TaskInstanceDao
import app.expgessia.data.dao.UserCharacteristicDao
import app.expgessia.data.dao.UserDao
import app.expgessia.data.entity.CharacteristicEntity
import app.expgessia.data.entity.DailyStatsEntity
import app.expgessia.data.entity.TaskEntity
import app.expgessia.data.entity.TaskInstanceEntity
import app.expgessia.data.entity.UserCharacteristicEntity
import app.expgessia.data.entity.UserEntity
import java.io.BufferedReader
import java.io.InputStreamReader

@Database(
    entities = [UserEntity::class,
        CharacteristicEntity::class, TaskEntity::class, DailyStatsEntity::class, TaskInstanceEntity::class, UserCharacteristicEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun characteristicDao(): CharacteristicDao

    abstract fun taskDao(): TaskDao

    abstract fun dailyStatsDao(): DailyStatsDao

    abstract fun taskInstanceDao(): TaskInstanceDao

    abstract fun userCharacteristicDao(): UserCharacteristicDao

    companion object {
        private const val TAG = "AppDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_db.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.d(TAG, "Database onCreate called. Executing SQL from assets.")
            executeSqlFromAssets(db, context, "sql/create_tables.sql")
        }

        private fun executeSqlFromAssets(
            db: SupportSQLiteDatabase,
            context: Context,
            path: String,
        ) {
            try {
                context.assets.open(path).use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val sql = reader.readText()
                    val commands = sql.split(';')
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    commands.forEachIndexed { index, command ->
                        db.execSQL(command)
                        Log.d(
                            TAG,
                            "   [${index + 1}/${commands.size}] Выполнено: ${
                                command.substring(
                                    0,
                                    minOf(30, command.length)
                                )
                            }..."
                        )
                    }
                    Log.d(TAG, "✅ УСПЕХ: Все SQL команды из $path выполнены успешно.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ ОШИБКА: Не удалось выполнить SQL из $path. Проверьте синтаксис.", e)
            }
        }
    }
}
