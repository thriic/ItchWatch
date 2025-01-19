package com.thriic.itchwatch

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.thriic.core.local.GameLocalDataSource
import com.thriic.itchwatch.utils.readFromShare
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ShareActivity : ComponentActivity() {
    @Inject
    lateinit var gameLocalDataSource: GameLocalDataSource
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            try {
                val data = intent.readFromShare(this@ShareActivity)?.toTypedArray()
                if (data != null) {
                    gameLocalDataSource.insertLocalInfo(*data)
                    Toast.makeText(this@ShareActivity, "import successfully", Toast.LENGTH_SHORT).show()
                }
                android.os.Process.killProcess(android.os.Process.myPid())
                this@ShareActivity.finish()
            } catch (e: Exception) {
                Toast.makeText(this@ShareActivity, "fail,${e.message}", Toast.LENGTH_SHORT).show()
                this@ShareActivity.finish()
            }
        }
    }
}