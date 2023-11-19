package ru.application.homemedkit.databaseController;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MedicineDAO {
    @Query("SELECT * FROM medicines")
    List<Medicine> getAll();

    @Query("SELECT productName FROM medicines WHERE id=:medicineId")
    String getProductName(long medicineId);

    @Insert
    long add(Medicine medicine);

    @Update
    void update(Medicine medicine);

    @Delete
    void delete(Medicine medicine);

    @Query("SELECT * FROM medicines WHERE id = :id ")
    Medicine getByPK(long id);

    @Query("SELECT cis from medicines")
    List<String> getAllCIS();

    @Query("UPDATE medicines SET prodAmount = prodAmount-:amount WHERE id = :id")
    void intakeMedicine(long id, double amount);
}
