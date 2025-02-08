package com.thriic.itchwatch.ui.settings

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thriic.itchwatch.utils.readTextFile
import com.thriic.itchwatch.utils.share

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

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
    var shareDialog by remember { mutableStateOf(false) }
    var importDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "设置",
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
                        modifier = Modifier.clickable { openDialog = true },
                        title = "hi",
                        description = "prefer"
                    ) {
                        Text(
                            modifier = it,
                            text = "des"
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
                                if(content!=null)
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
fun SettingItem(
    modifier: Modifier,
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