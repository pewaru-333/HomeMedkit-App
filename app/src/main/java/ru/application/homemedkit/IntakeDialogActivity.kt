package ru.application.homemedkit

import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.ui.theme.AppTheme
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.Formatter
import ru.application.homemedkit.utils.ID
import ru.application.homemedkit.utils.TAKEN_ID


class IntakeDialogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(FLAG_KEEP_SCREEN_ON or FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)

        val database = MedicineDatabase.getInstance(this)
        val manager = NotificationManagerCompat.from(this)

        val medicineId = intent.getLongExtra(ID, 0L)
        val takenId = intent.getLongExtra(TAKEN_ID, 0L)
        val amount = intent.getDoubleExtra(BLANK, 0.0)

        val medicine = runBlocking { database.medicineDAO().getById(medicineId) } ?: return

        setContent {
            AppTheme {
                val scope = rememberCoroutineScope()

                fun onDismiss() {
                    scope.launch {
                        database.takenDAO().setNotified(takenId)
                        manager.cancel(takenId.toInt())
                        finishAndRemoveTask()
                    }
                }

                fun onConfirm() {
                    scope.launch {
                        database.takenDAO().setTaken(takenId, true, System.currentTimeMillis())
                        database.medicineDAO().intakeMedicine(medicineId, amount)
                        onDismiss()
                    }
                }

                AlertDialog(
                    onDismissRequest = ::onDismiss,
                    dismissButton = { Button(::onDismiss) { Text(stringResource(R.string.intake_text_not_taken)) } },
                    confirmButton = { Button(::onConfirm) { Text(stringResource(R.string.intake_text_taken)) } },
                    title = { Text(stringResource(R.string.text_do_intake)) },
                    text = {
                        Text(
                            style = MaterialTheme.typography.bodyLarge,
                            text = stringResource(
                                R.string.text_intake_time,
                                medicine.nameAlias.ifEmpty(medicine::productName),
                                Formatter.decimalFormat(amount),
                                stringResource(medicine.doseType.title),
                                Formatter.decimalFormat(medicine.prodAmount.minus(amount))
                            )
                        )
                    }
                )
            }
        }
    }
}