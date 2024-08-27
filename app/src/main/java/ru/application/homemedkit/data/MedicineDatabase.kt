package ru.application.homemedkit.data

import android.app.AlarmManager
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.application.homemedkit.data.dao.AlarmDAO
import ru.application.homemedkit.data.dao.IntakeDAO
import ru.application.homemedkit.data.dao.KitDAO
import ru.application.homemedkit.data.dao.MedicineDAO
import ru.application.homemedkit.data.dao.TakenDAO
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.data.dto.Intake
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.helpers.FORMAT_D_H
import ru.application.homemedkit.helpers.FORMAT_S
import ru.application.homemedkit.helpers.ZONE
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private const val DATABASE_NAME = "medicines"

@Database(
    entities = [Medicine::class, Intake::class, Alarm::class, Kit::class, IntakeTaken::class],
    version = 10
)
@TypeConverters(Converters::class)
abstract class MedicineDatabase : RoomDatabase() {

    abstract fun medicineDAO(): MedicineDAO
    abstract fun intakeDAO(): IntakeDAO
    abstract fun alarmDAO(): AlarmDAO
    abstract fun kitDAO(): KitDAO
    abstract fun takenDAO(): TakenDAO

    companion object {
        @Volatile
        private var INSTANCE: MedicineDatabase? = null

        fun getInstance(context: Context): MedicineDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                MedicineDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(
                    MIGRATION_1_4,
                    MIGRATION_4_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10
                )
                .allowMainThreadQueries()
                .build()
            INSTANCE = instance
            instance
        }

        fun setNull() {
            INSTANCE = null
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

        private val MIGRATION_4_6 = object : Migration(4, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE intakes ADD COLUMN foodType INTEGER NOT NULL DEFAULT -1")
                db.execSQL("ALTER TABLE medicines ADD COLUMN doseType TEXT NOT NULL DEFAULT '' ")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS kits (`kitId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`title` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE medicines_r (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`kitId` INTEGER NOT NULL DEFAULT 0, " +
                            "`cis` TEXT NOT NULL, `productName` TEXT NOT NULL, " +
                            "`expDate` INTEGER NOT NULL, `prodFormNormName` TEXT NOT NULL, " +
                            "`prodDNormName` TEXT NOT NULL, `prodAmount` REAL NOT NULL, " +
                            "`doseType` TEXT NOT NULL DEFAULT '', " +
                            "`phKinetics` TEXT NOT NULL, `comment` TEXT NOT NULL, " +
                            "`image` TEXT NOT NULL DEFAULT '', " +
                            "`scanned` INTEGER NOT NULL, `verified` INTEGER NOT NULL, " +
                            "FOREIGN KEY (kitId) REFERENCES kits (kitId) ON DELETE SET DEFAULT " +
                            "ON UPDATE CASCADE)"
                )
                db.execSQL(
                    "INSERT INTO medicines_r (id, cis, productName, expDate, prodFormNormName, " +
                            "prodDNormName, prodAmount, doseType, phKinetics, comment, image, scanned, verified) " +
                            "SELECT id, cis, productName, expDate, prodFormNormName, prodDNormName, " +
                            "prodAmount, doseType, phKinetics, comment, image, scanned, verified FROM medicines"
                )
                db.execSQL("DROP TABLE medicines")
                db.execSQL("ALTER TABLE medicines_r RENAME TO medicines")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE medicines_r (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`kitId` INTEGER DEFAULT NULL, " +
                            "`cis` TEXT NOT NULL, `productName` TEXT NOT NULL, " +
                            "`expDate` INTEGER NOT NULL, `prodFormNormName` TEXT NOT NULL, " +
                            "`prodDNormName` TEXT NOT NULL, `prodAmount` REAL NOT NULL, " +
                            "`doseType` TEXT NOT NULL DEFAULT '', " +
                            "`phKinetics` TEXT NOT NULL, `comment` TEXT NOT NULL, " +
                            "`image` TEXT NOT NULL DEFAULT '', " +
                            "`scanned` INTEGER NOT NULL, `verified` INTEGER NOT NULL, " +
                            "FOREIGN KEY (kitId) REFERENCES kits (kitId) ON DELETE SET NULL " +
                            "ON UPDATE CASCADE)"
                )
                db.execSQL(
                    "INSERT INTO medicines_r (id, cis, productName, expDate, prodFormNormName, " +
                            "prodDNormName, prodAmount, doseType, phKinetics, comment, image, scanned, verified) " +
                            "SELECT id, cis, productName, expDate, prodFormNormName, prodDNormName, " +
                            "prodAmount, doseType, phKinetics, comment, image, scanned, verified FROM medicines"
                )
                db.execSQL("DROP TABLE medicines")
                db.execSQL("ALTER TABLE medicines_r RENAME TO medicines")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS intakes_taken (`takenId` INTEGER PRIMARY KEY " +
                            "AUTOINCREMENT NOT NULL, `medicineId` INTEGER NOT NULL, " +
                            "`intakeId` INTEGER NOT NULL, `alarmId` INTEGER NOT NULL, " +
                            "`productName` TEXT NOT NULL, `formName` TEXT NOT NULL, " +
                            "`amount` REAL NOT NULL, `doseType` TEXT NOT NULL, " +
                            "`image` TEXT NOT NULL DEFAULT '', `trigger` INTEGER NOT NULL, " +
                            "`taken` INTEGER NOT NULL, `notified` INTEGER NOT NULL)"
                )

                val cursor = db.query("SELECT * FROM intakes")
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    val intakeId = cursor.getLong(0)
                    val medicineId = cursor.getLong(1)

                    val medicineC = db.query(
                        "SELECT productName, prodFormNormName, doseType, image FROM medicines " +
                                "WHERE id = $medicineId"
                    )
                    medicineC.moveToFirst()
                    val productName = medicineC.getString(0)
                    val formName = medicineC.getString(1)
                    val doseType = medicineC.getString(2)
                    val image = medicineC.getString(3)

                    val intakeC = db.query(
                        "SELECT amount, interval, time, startDate, finalDate " +
                                "FROM intakes WHERE intakeId = $intakeId"
                    )
                    intakeC.moveToFirst()
                    val amount = intakeC.getDouble(0)
                    val interval = intakeC.getInt(1)
                    val time = Converters().toTimeList(intakeC.getString(2))
                    val startDate = intakeC.getString(3)

                    if (time.size == 1) {
                        var milliS = LocalDateTime.parse("$startDate ${time[0]}", FORMAT_D_H)
                            .toInstant(ZONE).toEpochMilli()
                        val milliF = LocalDateTime.now().toInstant(ZONE).toEpochMilli()

                        while (milliS <= milliF) {
                            db.execSQL(
                                "INSERT INTO intakes_taken (medicineId, intakeId, alarmId, productName, " +
                                        "formName, amount, doseType, image, trigger, taken, notified) " +
                                        "VALUES ($medicineId, $intakeId, 0, '$productName', '$formName', " +
                                        "$amount, '$doseType', '$image', $milliS, true, true)"
                            )
                            milliS += interval * AlarmManager.INTERVAL_DAY
                        }
                    } else {
                        var localS = LocalDate.parse(startDate, FORMAT_S)

                        var milliS = if (time.isNotEmpty()) LocalDateTime.of(localS, time.first())
                        else LocalDateTime.of(LocalDate.of(3000, 1, 1), LocalTime.of(12, 0))
                        val milliF = if (time.isNotEmpty()) LocalDateTime.of(LocalDate.now(), time.last { it <= LocalTime.now() })
                        else LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.of(12, 0))

                        while (milliS <= milliF) {
                            time.forEach {
                                val millis = LocalDateTime.of(localS, it).toInstant(ZONE).toEpochMilli()
                                if (millis <= LocalDateTime.now().toInstant(ZONE).toEpochMilli())
                                    db.execSQL(
                                        "INSERT INTO intakes_taken (medicineId, intakeId, alarmId, productName, " +
                                                "formName, amount, doseType, image, trigger, taken, notified) " +
                                                "VALUES ($medicineId, $intakeId, 0, '$productName', '$formName', " +
                                                "$amount, '$doseType', '$image', $millis, true, true)"
                                    )
                            }
                            localS = localS.plusDays(interval.toLong())
                            milliS = milliS.plusDays(interval.toLong())
                        }
                    }

                    cursor.moveToNext()
                }
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }
    }
}