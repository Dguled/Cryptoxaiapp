@Database(
    entities = [CoinEntity::class, AnalysisResultEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coinDao(): CoinDao
    
    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
        
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "CryptoXAi.db"
            )
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}

class Converters {
    @TypeConverter
    fun fromAnalysisResult(result: AnalysisResult): String {
        return Gson().toJson(result)
    }
    
    @TypeConverter
    fun toAnalysisResult(json: String): AnalysisResult {
        return Gson().fromJson(json, AnalysisResult::class.java)
    }
}