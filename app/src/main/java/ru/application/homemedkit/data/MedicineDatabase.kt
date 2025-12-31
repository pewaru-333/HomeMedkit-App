package ru.application.homemedkit.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import ru.application.homemedkit.data.dao.AlarmDAO
import ru.application.homemedkit.data.dao.IntakeDAO
import ru.application.homemedkit.data.dao.IntakeDayDAO
import ru.application.homemedkit.data.dao.KitDAO
import ru.application.homemedkit.data.dao.MedicineDAO
import ru.application.homemedkit.data.dao.TakenDAO
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.Intake
import ru.application.homemedkit.data.dto.IntakeDay
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.data.dto.IntakeTime
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.data.dto.MedicineFTS
import ru.application.homemedkit.data.dto.MedicineKit
import ru.application.homemedkit.utils.DATABASE_NAME

@Database(
    version = 35,
    entities = [
        Medicine::class,
        MedicineFTS::class,
        Intake::class,
        Alarm::class,
        Kit::class,
        IntakeDay::class,
        IntakeTaken::class,
        MedicineKit::class,
        IntakeTime::class,
        Image::class
    ],
    autoMigrations = [
        AutoMigration(
            from = 32,
            to = 33
        )
    ]
)
abstract class MedicineDatabase : RoomDatabase() {

    abstract fun medicineDAO(): MedicineDAO
    abstract fun intakeDAO(): IntakeDAO
    abstract fun alarmDAO(): AlarmDAO
    abstract fun intakeDayDAO(): IntakeDayDAO
    abstract fun kitDAO(): KitDAO
    abstract fun takenDAO(): TakenDAO

    companion object {
        @Volatile
        private var INSTANCE: MedicineDatabase? = null

        fun getInstance(context: Context) = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context = context.applicationContext,
                klass = MedicineDatabase::class.java,
                name = DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_30, MIGRATION_30_31, MIGRATION_31_32, MIGRATION_33_34, MIGRATION_34_35)
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
                .also { INSTANCE = it }
        }

        private val MIGRATION_1_30 = object : Migration(1, 30) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }

        private val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE kits ADD COLUMN position INTEGER NOT NULL DEFAULT 1")

                val cursor = db.query("SELECT kitId FROM kits")
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    val kitId = cursor.getLong(0)

                    db.execSQL("UPDATE kits SET position = $kitId WHERE kitId = $kitId")

                    cursor.moveToNext()
                }
            }
        }

        private val MIGRATION_31_32 = object : Migration(31, 32) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE images ADD COLUMN position INTEGER NOT NULL DEFAULT 1")

                val cursor = db.query("SELECT id FROM images")
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    val imageId = cursor.getLong(0)

                    db.execSQL("UPDATE images SET position = $imageId WHERE id = $imageId")

                    cursor.moveToNext()
                }
            }
        }

        private val MIGRATION_33_34 = object : Migration(33, 34) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS medicines_fts")
                db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `medicines_fts` USING FTS4(`productName` TEXT NOT NULL, `nameAlias` TEXT NOT NULL, `prodFormNormName` TEXT NOT NULL, `structure` TEXT NOT NULL, `phKinetics` TEXT NOT NULL, `comment` TEXT NOT NULL, content=`medicines`)")
                db.execSQL("INSERT INTO medicines_fts(medicines_fts) VALUES('rebuild')")
            }
        }

        private val MIGRATION_34_35 = object : Migration(34, 35) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS medicines_fts")
                db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `medicines_fts` USING FTS4(`productName` TEXT NOT NULL, `nameAlias` TEXT NOT NULL, `prodFormNormName` TEXT NOT NULL, `structure` TEXT NOT NULL, `phKinetics` TEXT NOT NULL, `comment` TEXT NOT NULL, content=`medicines`, prefix=`1,2,3`, tokenize=unicode61)")
                db.execSQL("INSERT INTO medicines_fts(medicines_fts) VALUES('rebuild')")
            }
        }
    }
}