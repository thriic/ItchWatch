package com.thriic.itchwatch.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thriic.core.TimeFormat
import com.thriic.itchwatch.ui.common.IconSwitch
import com.thriic.itchwatch.utils.readTextFile
import com.thriic.itchwatch.utils.share

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val toastMsg by viewModel.toastMessage.collectAsState()
    LaunchedEffect(toastMsg) {
        if (toastMsg != null) {
            Toast.makeText(
                context,
                toastMsg,
                Toast.LENGTH_SHORT,
            ).show()
            viewModel.sendMessage(null)
        }
    }
    var checked by remember { mutableStateOf(true) }
    var openDialog by remember { mutableStateOf(false) }
    var threadDialog by remember { mutableStateOf(false) }
    var importDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {},
            )
        },
        content = { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
            ) {
                item {
                    SettingSubTitle("basic")
                    SettingItem(
                        modifier = Modifier.clickable { threadDialog = true },
                        title = "Fetch Concurrency",
                        description = "The maximum number of threads"
                    ) {
                        Text(
                            modifier = it.padding(end = 8.dp),
                            text = state.threadCount.toString()
                        )
                    }
                    HorizontalDivider(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    )

                }

                item {
                    SettingSubTitle("time format")
                    SettingItem(
                        title = "Absolute",
                        description = "2024/5/29"
                    ) {
                        IconSwitch(
                            checked = state.timeFormat == TimeFormat.AbsoluteDate,
                            onCheckedChange = {
                                viewModel.send(SettingsIntent.ChangeTimeFormat(TimeFormat.AbsoluteDate))
                            },
                            modifier = it
                        )
                    }
                    SettingItem(
                        title = "Relative",
                        description = "x days ago"
                    ) {
                        IconSwitch(
                            checked = state.timeFormat == TimeFormat.SimpleRelative,
                            onCheckedChange = {
                                viewModel.send(SettingsIntent.ChangeTimeFormat(TimeFormat.SimpleRelative))
                            },
                            modifier = it
                        )
                    }
                    SettingItem(
                        title = "Detailed Relative",
                        description = "x years/months/days/minutes ago"
                    ) {
                        IconSwitch(
                            checked = state.timeFormat == TimeFormat.DetailedRelative,
                            onCheckedChange = {
                                viewModel.send(SettingsIntent.ChangeTimeFormat(TimeFormat.DetailedRelative))
                            },
                            modifier = it
                        )
                    }
                    HorizontalDivider(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    )
                }

                item {

                    SettingSubTitle("im/export")
                    SettingItem(
                        modifier = Modifier.clickable {
                            viewModel.send(SettingsIntent.Export {
                                context.share(it)
                            })
                        },
                        title = "Export Local Data"
                    ) {

                    }
                    val intent = Intent(
                        Intent.ACTION_OPEN_DOCUMENT,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    ).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    val contentResolver = LocalContext.current.contentResolver

                    var pickedFileUri by remember { mutableStateOf<Uri?>(null) }
                    val launcher =
                        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                            Log.i("Import", "selected file URI ${it.data?.data}")
                            pickedFileUri = it.data?.data
                            if (pickedFileUri != null) {
                                val content =
                                    contentResolver.openInputStream(pickedFileUri!!)
                                        ?.readTextFile()
                                if (content != null)
                                    viewModel.send(SettingsIntent.Import(content))
                            }

                        }
                    SettingItem(
                        modifier = Modifier.clickable { launcher.launch(intent) },
                        title = "Import Local Data",
                        description = "open .iw or .json file"
                    ) {

                    }
                }
            }
        }
    )
    if (threadDialog) {
        ThreadDialog(
            onConfirm = { viewModel.send(SettingsIntent.ChangeThreadCount(it)) },
            onDismiss = { threadDialog = false }
        )
    }

//    if (openDialog) {
//        var length by rememberSaveable { mutableStateOf(uiState.aspect.first.toString()) }
//        var width by rememberSaveable { mutableStateOf(uiState.aspect.second.toString()) }
//        Dialog(
//            title = "规格",
//            onConfirm = {
//                if (length.isNotEmpty() && width.isNotEmpty())
//                    viewModel.send(SettingIntent.UpdateAspect(Pair(length.toInt(), width.toInt())))
//                openDialog = false
//            },
//            onCancel = { openDialog = false }
//        ) {
//            Column(
//                verticalArrangement = Arrangement.spacedBy(16.dp),
//                modifier = Modifier.height(150.dp)
//            ) {
//                val keyboardController = LocalSoftwareKeyboardController.current
//                OutlinedTextField(
//                    value = length,
//                    singleLine = true,
//                    onValueChange = { if (it.isNotEmpty() && it.isDigitsOnly()) length = it },
//                    label = { Text("行数") },
//                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//                    keyboardActions = KeyboardActions(
//                        onDone = {
//                            keyboardController?.hide()
//                        }
//                    )
//                )
//                OutlinedTextField(
//                    value = width,
//                    singleLine = true,
//                    onValueChange = { if (it.isDigitsOnly()) width = it },
//                    label = { Text("列数") },
//                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//                    keyboardActions = KeyboardActions(
//                        onDone = {
//                            keyboardController?.hide()
//                        }
//                    )
//                )
//            }
//        }
//    }

}

@Composable
fun ThreadDialog(
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Fetch Concurrency") },
        text = {
            Column {
                TextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it.filter { char -> char.isDigit() }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    placeholder = { Text("input a number") }
                )
                Text(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    text = "The maximum number of threads used for simultaneous network requests\n" +
                            "Excessive threads could trigger server-side request rejections (5-20 recommended)",
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val number = inputText.toIntOrNull() ?: 0
                    onConfirm(number)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String = "",
    content: @Composable (Modifier) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold)
            if (description.isNotEmpty()) Text(
                text = description,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp
            )
        }
        content(Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
fun SettingSubTitle(title: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = title, color = MaterialTheme.colorScheme.surfaceTint, fontSize = 14.sp)
    }
}

@Composable
@Preview
fun DialogPreview() {
    Surface {
        ThreadDialog({}, {})
    }
}