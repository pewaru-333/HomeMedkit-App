package ru.application.homemedkit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.data.model.MedicineFull
import ru.application.homemedkit.data.model.MedicineMain
import ru.application.homemedkit.utils.enums.Sorting

@Dao
interface MedicineDAO : BaseDAO<Medicine> {
    // ============================== Queries ==============================
    @Query("SELECT * FROM medicines")
    fun getAll(): List<Medicine>

    @Transaction
    @Query(
        """
        SELECT id, productName, nameAlias, prodAmount, doseType, expDate, prodFormNormName, structure, phKinetics
        FROM medicines
        WHERE (:search = '' OR LOWER(productName) LIKE '%' || LOWER(:search) || '%'
               OR LOWER(nameAlias) LIKE '%' || LOWER(:search) || '%'
               OR LOWER(structure) LIKE '%' || LOWER(:search) || '%'
               OR LOWER(phKinetics) LIKE '%' || LOWER(:search) || '%')
        AND (:kitsEnabled = 0 OR EXISTS (
            SELECT 1
            FROM medicines_kits
            WHERE medicines_kits.medicineId = medicines.id
              AND medicines_kits.kitId IN (:kitIds)
        ))
        ORDER BY
            CASE WHEN :sorting = 'IN_NAME' THEN (CASE WHEN nameAlias = '' THEN productName ELSE nameAlias END) COLLATE NOCASE ELSE NULL END ASC,
            CASE WHEN :sorting = 'RE_NAME' THEN (CASE WHEN nameAlias = '' THEN productName ELSE nameAlias END) COLLATE NOCASE ELSE NULL END DESC,
            CASE WHEN :sorting = 'IN_DATE' THEN expDate ELSE NULL END ASC,
            CASE WHEN :sorting = 'RE_DATE' THEN expDate ELSE NULL END DESC,
            id ASC -- Дополнительная сортировка для разрешения конфликтов (id уникальный)
    """
    )
    fun getListFlow(search: String, sorting: Sorting, kitIds: List<Long>, kitsEnabled: Boolean): Flow<List<MedicineMain>>

    @Query("SELECT id FROM medicines WHERE :cis LIKE '%' || cis || '%' AND cis IS NOT NULL AND cis != '' LIMIT 1")
    fun getIdByCis(cis: String): Long?

    @Transaction
    @Query("SELECT * FROM medicines WHERE id = :id ")
    fun getById(id: Long): MedicineFull?

    @Query("SELECT image FROM images")
    fun getAllImages(): List<String>

    @Query("SELECT image FROM images WHERE medicineId = :medicineId")
    fun getMedicineImages(medicineId: Long): List<String>

    @Query("UPDATE medicines SET prodAmount = prodAmount - :amount WHERE id = :id")
    fun intakeMedicine(id: Long, amount: Double)

    @Query("UPDATE medicines SET prodAmount = prodAmount + :amount WHERE id = :id")
    fun untakeMedicine(id: Long, amount: Double)

    // ============================== Insert ==============================
    @Insert
    suspend fun addImage(image: Iterable<Image>)

    // ============================== Update ==============================
    @Transaction
    suspend fun updateImages(images: Iterable<Image>) {
        deleteImages(images.first().medicineId)
        addImage(images)
    }

    // ============================== Delete ==============================
    @Query("DELETE FROM images WHERE medicineId = :medicineId")
    suspend fun deleteImages(medicineId: Long)
}