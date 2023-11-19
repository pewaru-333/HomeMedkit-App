package ru.application.homemedkit.databaseController;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms",
        foreignKeys = @ForeignKey(
                entity = Intake.class,
                parentColumns = "intakeId",
                childColumns = "intakeId",
                onUpdate = ForeignKey.CASCADE,
                onDelete = ForeignKey.CASCADE))
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    public long alarmId;
    public long intakeId;
    public long trigger;

    public Alarm() {
    }

    @Ignore
    public Alarm(long intakeId, long trigger) {
        this.intakeId = intakeId;
        this.trigger = trigger;
    }
}
