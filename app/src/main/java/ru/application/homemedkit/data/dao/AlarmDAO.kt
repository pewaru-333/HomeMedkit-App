package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.data.model.Schedule

@Dao
interface AlarmDAO : BaseDAO<Alarm> {
    @Transaction
    @Query(
        """
        SELECT alarms.alarmId, alarms.`trigger`, alarms.amount, images.image, 
        medicines.nameAlias, medicines.productName, medicines.prodFormNormName, medicines.doseType
        FROM alarms
        JOIN intakes ON intakes.intakeId = alarms.intakeId 
        JOIN medicines ON medicines.id = intakes.medicineId 
        JOIN images ON images.medicineId = medicines.id
        WHERE (:search = '' OR LOWER(medicines.productName) LIKE '%' || LOWER(:search) || '%')
        GROUP BY alarms.alarmId
        """
    )
    fun getFlow(search: String): Flow<List<Schedule>>

    @Query("SELECT * FROM alarms")
    fun getAll(): List<Alarm>

    @Query("SELECT * FROM alarms WHERE alarmId = :alarmId")
    fun getById(alarmId: Long): Alarm?

    @Query(
        """
        SELECT * FROM alarms 
        WHERE intakeId = :intakeId 
        ORDER BY `trigger` 
        LIMIT 1
        """
    )
    fun getNextByIntakeId(intakeId: Long): Alarm?

    @Query("DELETE FROM alarms WHERE intakeId = :intakeId")
    suspend fun deleteByIntakeId(intakeId: Long)
}