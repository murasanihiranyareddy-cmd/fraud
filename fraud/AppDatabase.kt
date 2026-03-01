package com.fraud_detector.database

import androidx.room.*
import com.fraud_detector.models.ThreatEntity
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [ThreatEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun threatDao(): ThreatDao

    companion object {
        fun build(context: android.content.Context): AppDatabase {
            SQLiteDatabase.loadLibs(context)
            val passphrase = SQLiteDatabase.getBytes("fraud_secure_key".toCharArray())
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(context, AppDatabase::class.java, "fraud_db")
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

@Dao
interface ThreatDao {
    @Query("SELECT * FROM threats ORDER BY timestamp DESC LIMIT 100")
    fun getRecent(): kotlinx.coroutines.flow.Flow<List<ThreatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(threat: ThreatEntity)

    @Query("SELECT COUNT(*) FROM threats WHERE level = 'HIGH'")
    suspend fun countHigh(): Int

    @Query("SELECT COUNT(*) FROM threats WHERE level = 'MEDIUM'")
    suspend fun countMedium(): Int

    @Query("DELETE FROM threats WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}