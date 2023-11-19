package ru.application.homemedkit.databaseController;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface IntakeDAO {
    @Query("SELECT * FROM intakes")
    List<Intake> getAll();

    @Query("SELECT * FROM intakes WHERE intakeId = :intakeId")
    Intake getByPK(long intakeId);

    @Insert
    long add(Intake intake);

    @Update
    void update(Intake intake);

    @Delete
    void delete(Intake intake);
}
