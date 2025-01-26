package ru.application.homemedkit.data

import android.content.Context
import androidx.core.database.getIntOrNull
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
import ru.application.homemedkit.data.dto.IntakeTime
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.data.dto.MedicineKit
import ru.application.homemedkit.helpers.DATABASE_NAME

@Database(
    version = 18,
    entities = [
        Medicine::class,
        Intake::class,
        Alarm::class,
        Kit::class,
        IntakeTaken::class,
        MedicineKit::class,
        IntakeTime::class
    ]
)
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
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13,
                    MIGRATION_13_14,
                    MIGRATION_14_15,
                    MIGRATION_15_16,
                    MIGRATION_16_17,
                    MIGRATION_17_18
                )
                .allowMainThreadQueries()
                .build()
            INSTANCE = instance
            instance
        }

        private val MIGRATION_1_4 = object : Migration(1, 4) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }

        private val MIGRATION_4_6 = object : Migration(4, 6) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE medicines SET doseType = 'ed' WHERE doseType = 'ЕД'")
                db.execSQL("UPDATE medicines SET doseType = 'pcs' WHERE doseType = 'шт'")
                db.execSQL("UPDATE medicines SET doseType = 'g' WHERE doseType = 'г'")
                db.execSQL("UPDATE medicines SET doseType = 'mg' WHERE doseType = 'мг'")
                db.execSQL("UPDATE medicines SET doseType = 'l' WHERE doseType = 'л'")
                db.execSQL("UPDATE medicines SET doseType = 'ml' WHERE doseType = 'мл'")

                db.execSQL("UPDATE intakes_taken SET doseType = 'ed' WHERE doseType = 'ЕД'")
                db.execSQL("UPDATE intakes_taken SET doseType = 'pcs' WHERE doseType = 'шт'")
                db.execSQL("UPDATE intakes_taken SET doseType = 'g' WHERE doseType = 'г'")
                db.execSQL("UPDATE intakes_taken SET doseType = 'mg' WHERE doseType = 'мг'")
                db.execSQL("UPDATE intakes_taken SET doseType = 'l' WHERE doseType = 'л'")
                db.execSQL("UPDATE intakes_taken SET doseType = 'ml' WHERE doseType = 'мл'")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE intakes_taken ADD COLUMN inFact INTEGER NOT NULL DEFAULT 0")

                val cursor = db.query("SELECT takenId, trigger FROM intakes_taken")
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    val id = cursor.getInt(0)
                    val trigger = cursor.getLong(1)

                    db.execSQL("UPDATE intakes_taken SET inFact = $trigger WHERE takenId = $id")

                    cursor.moveToNext()
                }
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE intakes ADD COLUMN noSound INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE intakes ADD COLUMN preAlarm INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) =
                db.execSQL("ALTER TABLE alarms ADD COLUMN preAlarm INTEGER NOT NULL DEFAULT 0")
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) =
                db.execSQL("ALTER TABLE intakes ADD COLUMN fullScreen INTEGER NOT NULL DEFAULT 0")
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medicines ADD COLUMN structure TEXT NOT NULL DEFAULT '' ")
                db.execSQL("ALTER TABLE medicines ADD COLUMN recommendations TEXT NOT NULL DEFAULT '' ")
                db.execSQL("ALTER TABLE medicines ADD COLUMN storageConditions TEXT NOT NULL DEFAULT '' ")
            }
        }

        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS medicines_kits " +
                        "(`medicineId` INTEGER NOT NULL, `kitId` INTEGER NOT NULL, " +
                        "PRIMARY KEY (medicineId, kitId)" +
                        "FOREIGN KEY (medicineId) REFERENCES medicines (id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                        "FOREIGN KEY (kitId) REFERENCES kits (kitId) ON UPDATE CASCADE ON DELETE CASCADE)"
                )

                val cursor = db.query("SELECT id, kitId FROM MEDICINES")
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    val medicineId = cursor.getInt(0)
                    val kitId = cursor.getIntOrNull(1)

                    kitId?.let {
                        db.execSQL("INSERT INTO medicines_kits (medicineId, kitId) VALUES ($medicineId, $kitId)")
                    }

                    cursor.moveToNext()
                }

                db.execSQL(
                    "CREATE TABLE medicines_r (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`cis` TEXT NOT NULL, `productName` TEXT NOT NULL, " +
                            "`expDate` INTEGER NOT NULL, `prodFormNormName` TEXT NOT NULL, " +
                            "`structure` TEXT NOT NULL DEFAULT '', `recommendations` TEXT NOT NULL DEFAULT '', " +
                            "`storageConditions` TEXT NOT NULL DEFAULT '', " +
                            "`prodDNormName` TEXT NOT NULL, `prodAmount` REAL NOT NULL, " +
                            "`doseType` TEXT NOT NULL DEFAULT '', " +
                            "`phKinetics` TEXT NOT NULL, `comment` TEXT NOT NULL, " +
                            "`image` TEXT NOT NULL DEFAULT '', " +
                            "`scanned` INTEGER NOT NULL, `verified` INTEGER NOT NULL)"
                )
                db.execSQL("INSERT INTO medicines_r " +
                        "(id, cis, productName, expDate, prodFormNormName, structure, " +
                        "recommendations, storageConditions, prodDNormName, prodAmount, " +
                        "doseType, phKinetics, comment, image, scanned, verified) " +
                        "SELECT id, cis, productName, expDate, prodFormNormName, structure, " +
                        "recommendations, storageConditions, prodDNormName, prodAmount, doseType, " +
                        "phKinetics, comment, image, scanned, verified FROM medicines")
                db.execSQL("DROP TABLE medicines")
                db.execSQL("ALTER TABLE medicines_r RENAME TO medicines")

                db.execSQL("ALTER TABLE medicines ADD COLUMN nameAlias TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE intakes ADD COLUMN cancellable INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS intake_time " +
                            "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, " +
                            "`intakeId` INTEGER NOT NULL, `time` TEXT NOT NULL, `firstTrigger` INTEGER NOT NULL DEFAULT 0, " +
                            "FOREIGN KEY (intakeId) REFERENCES intakes (intakeId) ON UPDATE CASCADE ON DELETE CASCADE)"
                )
                db.execSQL("ALTER TABLE alarms ADD COLUMN amount REAL NOT NULL DEFAULT 0.0")

                var cursor = db.query("SELECT intakeId, amount, time FROM intakes")
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    val intakeId = cursor.getLong(0)
                    val amount = cursor.getDouble(1)
                    val timeString = cursor.getString(2)
                    val timeList = timeString.split(",")

                    timeList.forEach {
                        db.execSQL("INSERT INTO intake_time (intakeId, amount, time) VALUES ($intakeId, $amount, '$it')")
                    }

                    db.execSQL("UPDATE alarms SET amount = $amount WHERE intakeId = $intakeId")

                    cursor.moveToNext()
                }

                cursor = db.query("SELECT intakeId, trigger FROM alarms")
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    val intakeId = cursor.getLong(0)
                    val trigger = cursor.getLong(1)

                    db.execSQL("UPDATE intake_time SET firstTrigger = $trigger WHERE intakeId = $intakeId")

                    cursor.moveToNext()
                }

                db.execSQL(
                    "CREATE TABLE intakes_r (`intakeId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`medicineId` INTEGER NOT NULL, `schemaType` TEXT NOT NULL DEFAULT 'PERSONAL', " +
                            "`sameAmount` INTEGER NOT NULL DEFAULT 1, `interval` INTEGER NOT NULL, `foodType` INTEGER NOT NULL, " +
                            "`period` INTEGER NOT NULL, `startDate` TEXT NOT NULL, " +
                            "`finalDate` TEXT NOT NULL DEFAULT '', `fullScreen` INTEGER NOT NULL, " +
                            "`noSound` INTEGER NOT NULL, `preAlarm` INTEGER NOT NULL, `cancellable` INTEGER NOT NULL, " +
                            "FOREIGN KEY (medicineId) REFERENCES medicines (id) ON UPDATE CASCADE ON DELETE CASCADE)"
                )
                db.execSQL(
                    "INSERT INTO intakes_r " +
                            "(intakeId, medicineId, interval, foodType, period, " +
                            "startDate, finalDate, fullScreen, preAlarm, noSound, cancellable) " +
                            "SELECT intakeId, medicineId, interval, foodType, period, " +
                            "startDate, finalDate, fullScreen, preAlarm, noSound, cancellable FROM intakes"
                )
                db.execSQL("DROP TABLE intakes")
                db.execSQL("ALTER TABLE intakes_r RENAME TO intakes")
            }
        }
    }
}