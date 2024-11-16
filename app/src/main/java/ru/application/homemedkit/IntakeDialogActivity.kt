package ru.application.homemedkit

import android.os.Bundle
import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.R.string.intake_text_not_taken
import ru.application.homemedkit.R.string.intake_text_taken
import ru.application.homemedkit.R.string.text_do_intake
import ru.application.homemedkit.R.string.text_intake_time
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.DoseTypes
import ru.application.homemedkit.helpers.ID
import ru.application.homemedkit.helpers.TAKEN_ID
import ru.application.homemedkit.helpers.shortName
import ru.application.homemedkit.ui.theme.AppTheme


class IntakeDialogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        theme.applyStyle(android.R.style.Theme_Wallpaper, true)
        super.onCreate(savedInstanceState)

        window.addFlags(FLAG_KEEP_SCREEN_ON or FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)

        val database = MedicineDatabase.getInstance(this)
        val takenDAO = database.takenDAO()

        val medicineId = intent.getLongExtra(ID, 0L)
        val takenId = intent.getLongExtra(TAKEN_ID, 0L)
        val amount = intent.getDoubleExtra(BLANK, 0.0)

        val medicine = database.medicineDAO().getById(medicineId)

        setContent {
            AppTheme {
                AlertDialog(
                    title = { Text(stringResource(text_do_intake)) },
                    onDismissRequest = {
                        takenDAO.setNotified(takenId)
                        NotificationManagerCompat.from(this).cancel(takenId.toInt())
                        finishAndRemoveTask()
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                takenDAO.setNotified(takenId)
                                NotificationManagerCompat.from(this).cancel(takenId.toInt())
                                finishAndRemoveTask()
                            }
                        ) { Text(stringResource(intake_text_not_taken)) }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                takenDAO.setNotified(takenId)
                                takenDAO.setTaken(takenId, true, System.currentTimeMillis())
                                database.medicineDAO().intakeMedicine(medicineId, amount)
                                NotificationManagerCompat.from(this).cancel(takenId.toInt())
                                finishAndRemoveTask()
                            }
                        ) { Text(stringResource(intake_text_taken)) }
                    },
                    text = {
                        Text(
                            style = MaterialTheme.typography.bodyLarge,
                            text = stringResource(
                                text_intake_time,
                                shortName(medicine?.productName),
                                amount,
                                stringResource(DoseTypes.getTitle(medicine?.doseType))
                            )
                        )
                    }
                )
            }
        }
    }
}