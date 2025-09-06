package ru.application.homemedkit.data

import android.content.Context
import androidx.core.database.getIntOrNull
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
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
import ru.application.homemedkit.data.dto.MedicineKit
import ru.application.homemedkit.utils.DATABASE_NAME
import ru.application.homemedkit.utils.FORMAT_DD_MM_YYYY
import ru.application.homemedkit.utils.ZONE
import ru.application.homemedkit.utils.enums.SchemaType
import ru.application.homemedkit.utils.getDateTime
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZonedDateTime

@Database(
    version = 32,
    entities = [
        Medicine::class,
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
            from = 19,
            to = 20,
            spec = MedicineDatabase.Companion.AUTO_MIGRATION_19_20::class
        ),
        AutoMigration(
            from = 20,
            to = 21
        ),
        AutoMigration(
            from = 27,
            to = 28,
            spec = MedicineDatabase.Companion.AutoMigrationFrom27To28::class
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
                context.applicationContext,
                MedicineDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(
                    MIGRATION_1_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13,
                    MIGRATION_13_14,
                    MIGRATION_14_15,
                    MIGRATION_15_16,
                    MIGRATION_16_17,
                    MIGRATION_17_18,
                    MIGRATION_18_19,
                    MIGRATION_21_22,
                    MIGRATION_22_26,
                    MIGRATION_26_27,
                    MIGRATION_28_29,
                    MIGRATION_29_30,
                    MIGRATION_30_31,
                    MIGRATION_31_32
                )
                .setQueryExecutor(Dispatchers.IO.asExecutor())
                .setTransactionExecutor(Dispatchers.IO.asExecutor())
                .build()
                .also { INSTANCE = it }
        }

        private val MIGRATION_1_11 = object : Migration(1, 11) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
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
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS medicines_kits " +
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
                db.execSQL(
                    "INSERT INTO medicines_r " +
                            "(id, cis, productName, expDate, prodFormNormName, structure, " +
                            "recommendations, storageConditions, prodDNormName, prodAmount, " +
                            "doseType, phKinetics, comment, image, scanned, verified) " +
                            "SELECT id, cis, productName, expDate, prodFormNormName, structure, " +
                            "recommendations, storageConditions, prodDNormName, prodAmount, doseType, " +
                            "phKinetics, comment, image, scanned, verified FROM medicines"
                )
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

        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS images " +
                            "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`medicineId` INTEGER NOT NULL, " +
                            "`image` TEXT NOT NULL, " +
                            "FOREIGN KEY (medicineId) REFERENCES medicines (id) ON UPDATE CASCADE ON DELETE CASCADE)"
                )

                val cursor = db.query("SELECT id, image FROM medicines")
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    val medicineId = cursor.getLong(0)
                    val image = cursor.getString(1)

                    db.execSQL("INSERT INTO images (medicineId, image) VALUES ($medicineId, '$image')")

                    cursor.moveToNext()
                }
            }
        }

        private val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE medicines_r (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`cis` TEXT NOT NULL, `productName` TEXT NOT NULL, `nameAlias` TEXT NOT NULL, " +
                            "`expDate` INTEGER NOT NULL, `packageOpenedDate` INTEGER NOT NULL DEFAULT -1, `prodFormNormName` TEXT NOT NULL, " +
                            "`structure` TEXT NOT NULL DEFAULT '', `recommendations` TEXT NOT NULL DEFAULT '', " +
                            "`storageConditions` TEXT NOT NULL DEFAULT '', " +
                            "`prodDNormName` TEXT NOT NULL, `prodAmount` REAL NOT NULL, " +
                            "`doseType` TEXT DEFAULT NULL, " +
                            "`phKinetics` TEXT NOT NULL, `comment` TEXT NOT NULL, " +
                            "`scanned` INTEGER NOT NULL, `verified` INTEGER NOT NULL)"
                )

                db.execSQL(
                    "CREATE TABLE intakes_taken_r (`takenId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`medicineId` INTEGER NOT NULL, `intakeId` INTEGER NOT NULL, " +
                            "`alarmId` INTEGER NOT NULL, `productName` TEXT NOT NULL, " +
                            "`formName` TEXT NOT NULL, `amount` REAL NOT NULL, " +
                            "`doseType` TEXT DEFAULT NULL, `image` TEXT NOT NULL, " +
                            "`trigger` INTEGER NOT NULL, `inFact` INTEGER NOT NULL, " +
                            "`taken` INTEGER NOT NULL, `notified` INTEGER NOT NULL)"
                )

                db.execSQL(
                    "INSERT INTO medicines_r (`id`,`cis`,`productName`,`nameAlias`,`expDate`," +
                            "`packageOpenedDate`,`prodFormNormName`,`structure`,`recommendations`," +
                            "`storageConditions`, `prodDNormName`,`prodAmount`,`doseType`,`phKinetics`," +
                            "`comment`,`scanned`,`verified`) SELECT `id`,`cis`,`productName`,`nameAlias`,`expDate`," +
                            "`packageOpenedDate`,`prodFormNormName`,`structure`,`recommendations`," +
                            "`storageConditions`, `prodDNormName`,`prodAmount`,`doseType`,`phKinetics`," +
                            "`comment`,`scanned`,`verified` FROM medicines"
                )


                db.execSQL(
                    "INSERT INTO intakes_taken_r (`takenId`,`medicineId`,`intakeId`,`alarmId`," +
                            "`productName`,`formName`,`amount`,`doseType`,`image`,`trigger`,`inFact`," +
                            "`taken`,`notified`) SELECT `takenId`,`medicineId`,`intakeId`,`alarmId`," +
                            "`productName`,`formName`,`amount`,`doseType`,`image`,`trigger`,`inFact`," +
                            "`taken`,`notified` FROM intakes_taken"
                )

                var cursor = db.query("SELECT id, doseType FROM medicines_r")
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    val id = cursor.getLong(0)
                    val doseTypeOld = cursor.getString(1)

                    val doseType = when (doseTypeOld) {
                        "ed" -> "UNITS"
                        "pcs" -> "PIECES"
                        "sach" -> "SACHETS"
                        "g" -> "GRAMS"
                        "mg" -> "MILLIGRAMS"
                        "l" -> "LITERS"
                        "ml" -> "MILLILITERS"
                        "ratio" -> "RATIO"
                        else -> null
                    }

                    db.execSQL("UPDATE medicines_r SET doseType = '$doseType' WHERE id = $id")

                    cursor.moveToNext()
                }

                cursor = db.query("SELECT takenId, doseType FROM intakes_taken_r")
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    val id = cursor.getLong(0)
                    val doseTypeOld = cursor.getString(1)

                    val doseType = when (doseTypeOld) {
                        "ed" -> "UNITS"
                        "pcs" -> "PIECES"
                        "sach" -> "SACHETS"
                        "g" -> "GRAMS"
                        "mg" -> "MILLIGRAMS"
                        "l" -> "LITERS"
                        "ml" -> "MILLILITERS"
                        "ratio" -> "RATIO"
                        else -> null
                    }

                    db.execSQL("UPDATE intakes_taken_r SET doseType = '$doseType' WHERE takenId = $id")

                    cursor.moveToNext()
                }

                db.execSQL("DROP TABLE medicines")
                db.execSQL("ALTER TABLE medicines_r RENAME TO medicines")

                db.execSQL("DROP TABLE intakes_taken")
                db.execSQL("ALTER TABLE intakes_taken_r RENAME TO intakes_taken")
            }
        }

        private val MIGRATION_22_26 = object : Migration(22, 26) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }

        private val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE medicines_r (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`cis` TEXT NOT NULL, `productName` TEXT NOT NULL, `nameAlias` TEXT NOT NULL, " +
                            "`expDate` INTEGER NOT NULL, `packageOpenedDate` INTEGER NOT NULL DEFAULT -1, `prodFormNormName` TEXT NOT NULL, " +
                            "`structure` TEXT NOT NULL DEFAULT '', `recommendations` TEXT NOT NULL DEFAULT '', " +
                            "`storageConditions` TEXT NOT NULL DEFAULT '', " +
                            "`prodDNormName` TEXT NOT NULL, `prodAmount` REAL NOT NULL, " +
                            "`doseType` TEXT NOT NULL, " +
                            "`phKinetics` TEXT NOT NULL, `comment` TEXT NOT NULL, " +
                            "`scanned` INTEGER NOT NULL, `verified` INTEGER NOT NULL)"
                )

                db.execSQL(
                    "CREATE TABLE intakes_taken_r (`takenId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`medicineId` INTEGER NOT NULL, `intakeId` INTEGER NOT NULL, " +
                            "`alarmId` INTEGER NOT NULL, `productName` TEXT NOT NULL, " +
                            "`formName` TEXT NOT NULL, `amount` REAL NOT NULL, " +
                            "`doseType` TEXT NOT NULL, `image` TEXT NOT NULL, " +
                            "`trigger` INTEGER NOT NULL, `inFact` INTEGER NOT NULL, " +
                            "`taken` INTEGER NOT NULL, `notified` INTEGER NOT NULL)"
                )

                db.execSQL(
                    "INSERT INTO medicines_r (`id`,`cis`,`productName`,`nameAlias`,`expDate`," +
                            "`packageOpenedDate`,`prodFormNormName`,`structure`,`recommendations`," +
                            "`storageConditions`, `prodDNormName`,`prodAmount`,`doseType`,`phKinetics`," +
                            "`comment`,`scanned`,`verified`) SELECT `id`,`cis`,`productName`,`nameAlias`,`expDate`," +
                            "`packageOpenedDate`,`prodFormNormName`,`structure`,`recommendations`," +
                            "`storageConditions`, `prodDNormName`,`prodAmount`,`doseType`,`phKinetics`," +
                            "`comment`,`scanned`,`verified` FROM medicines"
                )

                db.execSQL(
                    "INSERT INTO intakes_taken_r (`takenId`,`medicineId`,`intakeId`,`alarmId`," +
                            "`productName`,`formName`,`amount`,`doseType`,`image`,`trigger`,`inFact`," +
                            "`taken`,`notified`) SELECT `takenId`,`medicineId`,`intakeId`,`alarmId`," +
                            "`productName`,`formName`,`amount`,`doseType`,`image`,`trigger`,`inFact`," +
                            "`taken`,`notified` FROM intakes_taken"
                )


                db.execSQL("UPDATE medicines_r SET doseType = 'UNKNOWN' WHERE doseType = 'null'")
                db.execSQL("UPDATE intakes_taken_r SET doseType = 'UNKNOWN' WHERE doseType = 'null'")

                db.execSQL("DROP TABLE medicines")
                db.execSQL("ALTER TABLE medicines_r RENAME TO medicines")

                db.execSQL("DROP TABLE intakes_taken")
                db.execSQL("ALTER TABLE intakes_taken_r RENAME TO intakes_taken")
            }
        }

        @DeleteColumn(
            tableName = "medicines",
            columnName = "image"
        )
        class AUTO_MIGRATION_19_20 : AutoMigrationSpec

        class AutoMigrationFrom27To28 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                val cursor =
                    db.query("SELECT intakeId, finalDate, schemaType, interval FROM intakes")
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    val intakeId = cursor.getLong(0)
                    val finalDate = cursor.getString(1)
                    val schemaType = cursor.getString(2)
                    val interval = cursor.getInt(3)

                    val alarmCursor =
                        db.query("SELECT alarmId, trigger FROM alarms WHERE intakeId = $intakeId")
                    alarmCursor.moveToFirst()

                    while (!alarmCursor.isAfterLast) {
                        val alarmId = alarmCursor.getLong(0)
                        val trigger = alarmCursor.getLong(1)

                        var first = getDateTime(trigger)

                        val last = ZonedDateTime.of(
                            LocalDate.parse(finalDate, FORMAT_DD_MM_YYYY),
                            getDateTime(trigger).toLocalTime(),
                            ZONE
                        )

                        while (!first.isAfter(last)) {
                            db.execSQL(
                                "INSERT INTO intake_schedule (`alarmId`, `trigger`) " +
                                        "VALUES ($alarmId, ${first.toInstant().toEpochMilli()})"
                            )

                            first = first.plusDays(
                                if (SchemaType.valueOf(schemaType) == SchemaType.BY_DAYS) SchemaType.valueOf(
                                    schemaType
                                ).interval.days.toLong()
                                else interval.toLong()
                            )
                        }


                        alarmCursor.moveToNext()
                    }

                    cursor.moveToNext()
                }
            }
        }

        private val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE intake_days (`intakeId` INTEGER NOT NULL, `day` TEXT NOT NULL, " +
                            "PRIMARY KEY (intakeId, day) " +
                            "FOREIGN KEY (intakeId) REFERENCES intakes (intakeId) ON UPDATE CASCADE ON DELETE CASCADE)"
                )

                var intakeCursor = db.query("SELECT intakeId FROM intakes")
                intakeCursor.moveToNext()

                while (!intakeCursor.isAfterLast) {
                    val intakeId = intakeCursor.getLong(0)

                    DayOfWeek.entries.forEach {
                        db.execSQL("INSERT INTO intake_days (`intakeId`, `day`) VALUES ($intakeId, '$it')")
                    }

                    intakeCursor.moveToNext()
                }

                db.execSQL("UPDATE intakes SET period = 1825 WHERE period = 38500")

                intakeCursor =
                    db.query("SELECT intakeId, finalDate, schemaType, interval FROM intakes")
                intakeCursor.moveToNext()

                while (!intakeCursor.isAfterLast) {
                    val intakeId = intakeCursor.getLong(0)
                    val finalDate = intakeCursor.getString(1)
                    val schemaType = intakeCursor.getString(2)
                    val interval = intakeCursor.getInt(3)

                    val alarmCursor = db.query(
                        "SELECT alarmId, intakeId, trigger, amount, preAlarm " +
                                "FROM alarms " +
                                "WHERE intakeId = $intakeId"
                    )
                    alarmCursor.moveToFirst()

                    while (!alarmCursor.isAfterLast) {
                        val alarmId = alarmCursor.getLong(0)
                        val intakeId = alarmCursor.getLong(1)
                        val trigger = alarmCursor.getLong(2)
                        val amount = alarmCursor.getDouble(3)
                        val preAlarm = alarmCursor.getInt(4)

                        var first = getDateTime(trigger)

                        first = first.let {
                            var unix = it

                            while (unix.toInstant()
                                    .toEpochMilli() < System.currentTimeMillis()
                            ) {
                                unix = unix.plusDays(1)
                            }

                            unix
                        }

                        val last = ZonedDateTime.of(
                            LocalDate.parse(finalDate, FORMAT_DD_MM_YYYY),
                            getDateTime(trigger).toLocalTime(),
                            ZONE
                        )

                        while (!first.isAfter(last) && first < ZonedDateTime.of(
                                2030,
                                1,
                                1,
                                0,
                                0,
                                0,
                                0,
                                ZONE
                            )
                        ) {
                            db.execSQL(
                                "INSERT INTO alarms (`intakeId`, `trigger`, `amount`, `preAlarm`) " +
                                        "VALUES ($intakeId, ${
                                            first.toInstant().toEpochMilli()
                                        }, $amount, $preAlarm)"
                            )


                            first = first.plusDays(
                                if (SchemaType.valueOf(schemaType) == SchemaType.BY_DAYS) SchemaType.valueOf(
                                    schemaType
                                ).interval.days.toLong()
                                else interval.toLong()
                            )
                        }

                        db.execSQL("DELETE FROM alarms WHERE alarmId = $alarmId")
                        alarmCursor.moveToNext()
                    }

                    intakeCursor.moveToNext()
                }

                db.execSQL("DROP TABLE IF EXISTS intake_schedule")

                db.execSQL(
                    "CREATE TABLE time_r (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`intakeId` INTEGER NOT NULL, `time` TEXT NOT NULL, `amount` REAL NOT NULL, " +
                            "FOREIGN KEY (intakeId) REFERENCES intakes (intakeId) ON UPDATE CASCADE ON DELETE CASCADE)"
                )

                val timeCursor = db.query("SELECT intakeId, time, amount FROM intake_time")
                timeCursor.moveToFirst()

                while (!timeCursor.isAfterLast) {
                    val intakeId = timeCursor.getLong(0)
                    val time = timeCursor.getString(1)
                    val amount = timeCursor.getDouble(2)

                    db.execSQL(
                        "INSERT INTO time_r (`intakeId`, `time`, `amount`) " +
                                "VALUES ($intakeId, '$time', $amount)"
                    )

                    timeCursor.moveToNext()
                }

                db.execSQL("DROP TABLE intake_time")
                db.execSQL("ALTER TABLE time_r RENAME TO intake_time")
            }
        }

        private val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val cursor = db.query("SELECT intakeId, finalDate FROM intakes")
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    val intakeId = cursor.getLong(0)
                    val finalDate = cursor.getString(1)
                    val parsed = LocalDate.parse(finalDate, FORMAT_DD_MM_YYYY)

                    if (parsed.year > 2030) {
                        db.execSQL("UPDATE intakes SET finalDate = '31.12.2029' WHERE intakeId = $intakeId")
                    }

                    cursor.moveToNext()
                }
            }
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
    }
}