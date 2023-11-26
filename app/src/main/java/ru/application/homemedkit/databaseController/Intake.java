package ru.application.homemedkit.databaseController;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "intakes",
        foreignKeys = @ForeignKey(
                entity = Medicine.class,
                parentColumns = "id",
                childColumns = "medicineId",
                onUpdate = ForeignKey.CASCADE,
                onDelete = ForeignKey.CASCADE))

public class Intake {

    @PrimaryKey(autoGenerate = true)
    public long intakeId;
    public long medicineId;
    public double amount;
    public String interval;
    public String time;
    public String period;
    public String startDate;
    public String finalDate;

    public Intake() {
    }

    @Ignore
    public Intake(long intakeId) {
        this.intakeId = intakeId;
    }

    @Ignore
    public Intake(long medicineId,
                  double amount,
                  String interval,
                  String time,
                  String period,
                  String startDate,
                  String finalDate) {
        this.medicineId = medicineId;
        this.amount = amount;
        this.interval = interval;
        this.time = time;
        this.period = period;
        this.startDate = startDate;
        this.finalDate = finalDate;
    }
}
