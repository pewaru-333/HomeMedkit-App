package ru.application.homemedkit.fragments

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.MedicineScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ScannerScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import ru.application.homemedkit.R

@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen(navController: DestinationsNavigator, context: Context = LocalContext.current) {
    var show by rememberSaveable { mutableStateOf(false) }

    Row(
        Modifier.fillMaxSize(),
        Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        Alignment.CenterVertically
    ) {
        Card(
            context.resources.getString(R.string.text_data_matrix_scanner),
            ImageVector.vectorResource(R.drawable.vector_scanner),
            navController,
            ScannerScreenDestination
        )
        Card(
            context.resources.getString(R.string.text_manual_add),
            Icons.Outlined.Create,
            navController,
            MedicineScreenDestination()
        )
    }

    BackHandler { show = true }
    if (show) ExitDialog({ show = false }, { (context as Activity).finishAndRemoveTask() })
}

@Composable
private fun Card(text: String, icon: ImageVector, navigator: DestinationsNavigator, route: Direction) {
    val bodyLarge = MaterialTheme.typography.bodyLarge
    var textStyle by remember { mutableStateOf(bodyLarge) }
    var drawReady by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .clickable { navigator.navigate(route) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Image(
            painter = rememberVectorPainter(icon),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .size(96.dp)
                .padding(4.dp),
            alignment = Alignment.Center,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
        )
        Text(
            text = text,
            modifier = Modifier
                .wrapContentWidth()
                .height(104.dp)
                .padding(4.dp)
                .drawWithContent { if (drawReady) drawContent() },
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Clip,
            maxLines = 3,
            onTextLayout = {
                if (it.didOverflowHeight) {
                    textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.9)
                } else drawReady = true
            },
            style = textStyle
        )
    }
}

@Composable
private fun ExitDialog(onDismiss: () -> Unit, onConfirm: () -> Unit, context: Context = LocalContext.current) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onConfirm) { Text(context.getString(R.string.text_yes)) } },
        dismissButton = { TextButton(onDismiss) { Text(context.getString(R.string.text_no)) } },
        title = { Text(context.getString(R.string.text_confirm_exit)) },
        text = { Text(context.getString(R.string.text_sure_to_exit)) }
    )
}