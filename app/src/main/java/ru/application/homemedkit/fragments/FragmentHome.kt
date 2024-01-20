package ru.application.homemedkit.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.application.homemedkit.R
import ru.application.homemedkit.activities.MedicineActivity
import ru.application.homemedkit.activities.ScannerActivity
import ru.application.homemedkit.helpers.ConstantsHelper.ADDING
import ru.application.homemedkit.ui.theme.AppTheme

class FragmentHome : Fragment() {

    private lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    Row(
                        Modifier.fillMaxSize(),
                        Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        Alignment.CenterVertically
                    ) {
                        CardPickAction(
                            resources.getString(R.string.text_data_matrix_scanner),
                            R.drawable.vector_scanner,
                            Intent(activity, ScannerActivity::class.java)
                        )
                        CardPickAction(
                            resources.getString(R.string.text_self_medicine_adding),
                            R.drawable.vector_edit,
                            Intent(activity, MedicineActivity::class.java).putExtra(ADDING, true)
                        )
                    }
                    BackHandler {
                        MaterialAlertDialogBuilder(activity)
                            .setTitle(R.string.text_confirm_exit)
                            .setMessage(R.string.text_sure_to_exit)
                            .setPositiveButton(R.string.text_yes) { _, _ -> activity.finishAndRemoveTask() }
                            .setNegativeButton(R.string.text_no) { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                }
            }
        }
    }


    @Composable
    fun CardPickAction(text: String, icon: Int, intent: Intent) {
        val bodyLarge = MaterialTheme.typography.bodyLarge
        var textStyle by remember { mutableStateOf(bodyLarge) }
        var drawReady by remember { mutableStateOf(false) }

        ElevatedCard(
            modifier = Modifier
                .width(160.dp)
                .height(200.dp)
                .clickable { startActivity(intent) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Image(
                painterResource(id = icon), null,
                Modifier
                    .fillMaxWidth()
                    .size(96.dp)
                    .padding(4.dp), Alignment.Center
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
}