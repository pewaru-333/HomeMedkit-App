@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ru.application.homemedkit.ui.screens

import android.os.Build
import android.view.WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.application.homemedkit.R
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.ui.elements.MedicineImage
import ru.application.homemedkit.ui.elements.VectorIcon
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.Formatter
import ru.application.homemedkit.utils.ID
import ru.application.homemedkit.utils.IS_ENOUGH_IN_STOCK
import ru.application.homemedkit.utils.TAKEN_ID

@Composable
fun IntakeFullScreen(medicineId: Long, takenId: Long, amount: Double, onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val activity = LocalActivity.current as? ComponentActivity
    val scope = rememberCoroutineScope()

    val database = MedicineDatabase.getInstance(context)
    val manager = NotificationManagerCompat.from(context)

    val medicine = runBlocking { database.medicineDAO().getById(medicineId) } ?: return
    val intake = runBlocking { database.takenDAO().getById(takenId) } ?: return
    val image = runBlocking { database.medicineDAO().getMedicineImage(medicineId) }

    val flag = manager.activeNotifications.size > 1 && manager.activeNotifications
        .filter { it.packageName == context.packageName }
        .filter { it.notification.extras.containsKey(IS_ENOUGH_IN_STOCK) }
        .all { it.notification.extras.getBoolean(IS_ENOUGH_IN_STOCK) }

    fun onDismiss() {
        scope.launch {
            database.takenDAO().setNotified(takenId)
            manager.cancel(takenId.toInt())
            manager.cancel(Int.MAX_VALUE)

            if (onBack != null) {
                onBack()
            } else {
                activity?.finishAndRemoveTask()
            }
        }
    }

    fun onConfirm() {
        scope.launch {
            database.takenDAO().setTaken(takenId, true, System.currentTimeMillis())
            database.medicineDAO().intakeMedicine(medicineId, amount)
            onDismiss()
        }
    }

    fun onConfirmAll() {
        scope.launch {
            manager.cancel(Int.MAX_VALUE)
            manager.activeNotifications
                .filter { it.packageName == context.packageName }
                .filter { it.notification.extras.containsKey(IS_ENOUGH_IN_STOCK) }
                .forEach { item ->
                    val medicineId = item.notification.extras.getLong(ID)
                    val takenId = item.notification.extras.getLong(TAKEN_ID)
                    val amount = item.notification.extras.getDouble(BLANK)

                    manager.cancel(takenId.toInt())
                    database.takenDAO().setNotified(takenId)
                    database.takenDAO().setTaken(takenId, true, System.currentTimeMillis())
                    database.medicineDAO().intakeMedicine(medicineId, amount)
                }

            if (onBack != null) {
                onBack()
            } else {
                activity?.finishAndRemoveTask()
            }
        }
    }

    DisposableEffect(Unit) {
        activity?.window?.addFlags(FLAG_KEEP_SCREEN_ON or FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            activity?.setTurnScreenOn(true)
            activity?.setShowWhenLocked(true)
        }

        onDispose {
            activity?.window?.clearFlags(FLAG_KEEP_SCREEN_ON or FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                activity?.setTurnScreenOn(false)
                activity?.setShowWhenLocked(false)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = stringResource(R.string.text_taking_medicine),
                style = MaterialTheme.typography.headlineLargeEmphasized.copy(
                    fontWeight = FontWeight.W500
                )
            )

            AssistChip(
                onClick = {},
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                label = {
                    Text(
                        text = Formatter.timeFormat(intake.trigger),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                },
                leadingIcon = {
                    VectorIcon(
                        icon = R.drawable.vector_time,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                },
                colors = AssistChipDefaults.assistChipColors().copy(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                )
            )
        }

        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
                .align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(140.dp),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        MedicineImage(image, Modifier.size(96.dp))
                    }
                }

                Spacer(Modifier.height(32.dp))

                InfoRow(R.string.text_title, medicine.nameAlias.ifEmpty(medicine::productName))
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                InfoRow(R.string.text_intake_amount, "${Formatter.decimalFormat(amount)} ${stringResource(medicine.doseType.title)}")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                InfoRow(R.string.text_remainder_after, "${Formatter.decimalFormat(medicine.prodAmount.minus(amount))} ${stringResource(medicine.doseType.title)}")
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            TextButton(
                onClick = ::onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                content = { Text(stringResource(R.string.intake_text_not_taken)) }
            )

            Column(Modifier.animateContentSize(), Arrangement.spacedBy(8.dp), Alignment.End) {
                FilledTonalButton(
                    onClick = ::onConfirm,
                    content = { Text(stringResource(R.string.intake_text_taken)) }
                )

                AnimatedVisibility(flag) {
                    Button(
                        onClick = ::onConfirmAll,
                        content = { Text(stringResource(R.string.text_action_intake_all_accept)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(@StringRes label: Int, value: String) =
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        )
    }