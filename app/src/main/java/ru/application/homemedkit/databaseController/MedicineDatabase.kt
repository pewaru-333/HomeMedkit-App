package ru.application.homemedkit.databaseController

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private const val DATABASE_NAME = "medicines"

@Database(entities = [Medicine::class, Intake::class, Alarm::class], version = 4)
@TypeConverters(Converters::class)
abstract class MedicineDatabase : RoomDatabase() {

    abstract fun medicineDAO(): MedicineDAO
    abstract fun intakeDAO(): IntakeDAO
    abstract fun alarmDAO(): AlarmDAO

    companion object {
        @Volatile
        private var INSTANCE: MedicineDatabase? = null

        fun getInstance(context: Context): MedicineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicineDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_4)
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_4 = object : Migration(1, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE medicines_r (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`cis` TEXT NOT NULL, `productName` TEXT NOT NULL, " +
                            "`expDate` INTEGER NOT NULL, `prodFormNormName` TEXT NOT NULL, " +
                            "`prodDNormName` TEXT NOT NULL, `prodAmount` REAL NOT NULL, " +
                            "`phKinetics` TEXT NOT NULL, `comment` TEXT NOT NULL, " +
                            "`scanned` INTEGER NOT NULL, `verified` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE intakes_r (`intakeId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`medicineId` INTEGER NOT NULL, `amount` REAL NOT NULL, " +
                            "`interval` INTEGER NOT NULL, `time` TEXT NOT NULL, `period` INTEGER NOT NULL, " +
                            "`startDate` TEXT NOT NULL, `finalDate` TEXT NOT NULL, " +
                            "FOREIGN KEY (medicineId) REFERENCES medicines (id) " +
                            "ON DELETE CASCADE ON UPDATE CASCADE)"
                )
                db.execSQL(
                    "CREATE TABLE alarms_r (`alarmId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`intakeId` INTEGER NOT NULL, `trigger` INTEGER NOT NULL, " +
                            "FOREIGN KEY (intakeId) REFERENCES intakes (intakeId) " +
                            "ON DELETE CASCADE ON UPDATE CASCADE)"
                )

                db.execSQL("UPDATE medicines SET cis = '' WHERE cis IS NULL")
                db.execSQL("UPDATE medicines SET productName = '' WHERE productName IS NULL")
                db.execSQL("UPDATE medicines SET expDate = -1 WHERE expDate IS NULL")
                db.execSQL("UPDATE medicines SET prodFormNormName = '' WHERE prodFormNormName IS NULL")
                db.execSQL("UPDATE medicines SET prodDNormName = '' WHERE prodDNormName IS NULL")
                db.execSQL("UPDATE medicines SET prodAmount = -1.0 WHERE prodAmount IS NULL")
                db.execSQL("UPDATE medicines SET phKinetics = '' WHERE phKinetics IS NULL")
                db.execSQL("UPDATE medicines SET comment = '' WHERE comment IS NULL")

                db.execSQL(
                    "INSERT INTO medicines_r (id, cis, productName, expDate, prodFormNormName, " +
                            "prodDNormName, prodAmount, phKinetics, comment, scanned, verified) " +
                            "SELECT id, cis, productName, expDate, prodFormNormName, prodDNormName, " +
                            "prodAmount, phKinetics, comment, scanned, verified FROM medicines"
                )

                val cursor = db.query("SELECT * FROM intakes")
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    val intakeId = cursor.getInt(0)
                    val medicineId = cursor.getInt(1)
                    val amount = cursor.getDouble(2)
                    val time = cursor.getString(4)
                    val startDate = cursor.getString(6)
                    val finalDate = cursor.getString(7)

                    val interval = when (val value = cursor.getString(3)) {
                        "daily" -> 1
                        "hourly" -> 1
                        "weekly" -> 7
                        else -> value.substringAfter("_")
                    }

                    val period = when (cursor.getString(5)) {
                        "week" -> 7
                        "month" -> 30
                        "other" -> 0
                        "indefinite" -> 38500
                        else -> -1
                    }

                    db.execSQL(
                        "INSERT INTO intakes_r (intakeId, medicineId, amount, interval, time, " +
                                "period, startDate, finalDate) VALUES ($intakeId, $medicineId, $amount, " +
                                "$interval, '$time', $period, '$startDate', '$finalDate')"
                    )

                    cursor.moveToNext()
                }

                db.execSQL(
                    "INSERT INTO alarms_r (alarmId, intakeId, trigger) " +
                            "SELECT alarmId, intakeId, trigger FROM alarms"
                )

                db.execSQL("DROP TABLE medicines")
                db.execSQL("DROP TABLE intakes")
                db.execSQL("DROP TABLE alarms")
                db.execSQL("ALTER TABLE medicines_r RENAME TO medicines")
                db.execSQL("ALTER TABLE intakes_r RENAME TO intakes")
                db.execSQL("ALTER TABLE alarms_r RENAME TO alarms")
                db.execSQL("ALTER TABLE medicines ADD COLUMN image TEXT NOT NULL DEFAULT '' ")
            }
        }
    }
}