package com.thriic.itchwatch.ui.nav.imports

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thriic.itchwatch.ui.nav.library.LibraryIntent
import com.thriic.itchwatch.ui.nav.library.LibraryItem
import com.thriic.itchwatch.ui.utils.WatchLayout
import com.thriic.itchwatch.ui.utils.cleanUrl
import com.thriic.itchwatch.ui.utils.getId
import com.thriic.itchwatch.ui.utils.isCollectionUrl
import com.thriic.itchwatch.ui.utils.isGamePageUrl
import com.thriic.itchwatch.ui.utils.readTextFile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Import(layout: WatchLayout, viewModel: ImportViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import") },
//                navigationIcon = {
//                    IconButton(onClick = {}) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
//                    }
//                }
            )
        },
        content = { innerPadding ->
            if (!state.loading) {
                SelectMethodPage(
                    innerPadding = innerPadding,
                    onTextFile = {
                        if (it == null) {
                            viewModel.sendMessage("find nothing")
                        } else {
                            //TODO("goto selectGamePage")
                            viewModel.send(ImportIntent.AddGames(it))
                        }
                    },
                    onSingleLink = {
                        if (it.isGamePageUrl())
                            viewModel.send(ImportIntent.AddGame(it))
                        else if (it.isCollectionUrl()) {
                            //TODO("goto selectGamePage")
                            viewModel.send(ImportIntent.FetchCollection(it))
                        }
                    }
                )
            }
            //TODO add a function
            if (state.loading)
                Column(
                    modifier = Modifier
                        .consumeWindowInsets(innerPadding)
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (state.progress != null) {
                        val animatedProgress by
                        animateFloatAsState(
                            targetValue = state.progress!!,
                            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                        )
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                        )
                        if (state.progressText.isNotBlank()) Text(state.progressText)
                    } else {
                        CircularProgressIndicator()
                        if (state.progressText.isNotBlank()) Text(state.progressText)
                    }

                }

        }
    )

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
}

@Composable
fun SelectGamePage(innerPadding: PaddingValues) {
    LazyColumn(
        contentPadding = innerPadding,
        //state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

    }
}

@Composable
fun SelectMethodPage(
    innerPadding: PaddingValues,
    onSingleLink: (String) -> Unit,
    onTextFile: (String?) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var text by rememberSaveable {
        if (clipboard.hasText()) {
            val clipboardText = clipboard.getText()!!.text.cleanUrl()
            if (clipboardText.isGamePageUrl() || clipboardText.isCollectionUrl())
                mutableStateOf(clipboardText)
            else
                mutableStateOf("")
        } else
            mutableStateOf("")
    }
    var isError by rememberSaveable { mutableStateOf(false) }

    val (selectedOption, onOptionSelected) = remember { mutableStateOf(0) }
    Column(
        modifier = Modifier
            .consumeWindowInsets(innerPadding)
            .padding(innerPadding)
            .selectableGroup(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .selectable(
                    selected = (selectedOption == 0),
                    onClick = { onOptionSelected(0) },
                    role = Role.RadioButton
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = (selectedOption == 0),
                onClick = null
            )
            Text(
                text = "From a single link",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                enabled = (selectedOption == 0),
                isError = isError,
                supportingText = { Text(if (isError) "not a link of game or collection" else "link of game or collection") },
                placeholder = { Text("https://") },
                singleLine = true,
                label = { Text(if (isError) "Link*" else "Link") },
            )
        }


        val intent = Intent(
            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .selectable(
                    selected = (selectedOption == 1),
                    onClick = { onOptionSelected(1) },
                    role = Role.RadioButton
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = (selectedOption == 1),
                onClick = null
            )
            Text(
                text = "From Text File",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        val contentResolver = LocalContext.current.contentResolver

        var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                Log.i("Import", "selected file URI ${it.data?.data}")
                pickedImageUri = it.data?.data
                if (pickedImageUri != null) {
                    val content =
                        contentResolver.openInputStream(pickedImageUri!!)
                            ?.readTextFile()
                    onTextFile(content)
                }

            }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "import all game links in the text file",
                style = MaterialTheme.typography.labelSmall
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = {
                if (selectedOption == 0) {
                    isError = !(text.isCollectionUrl() || text.isGamePageUrl())
                    if (!isError) {
                        onSingleLink(text)
                    }
                } else {
                    launcher.launch(intent)
                }
            }) {
                Text("Import")
            }
        }
    }
}

@Preview
@Composable
fun SelectPreview() {
    Surface {
    }
}

@Preview
@Composable
fun ImportPreview() {
    Surface {
//        Import(
//            layout = WatchLayout.Compact,
//            viewModel = ImportViewModel()
//        )
    }
}