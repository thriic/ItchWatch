package com.thriic.itchwatch

import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Navigator {

    private var navController: NavHostController? = null

    fun setController(controller: NavHostController) {
        navController = controller
    }

    fun clear() {
        navController = null
    }

    suspend fun navigate(route: String, builder: (NavOptionsBuilder.() -> Unit)? = null) {
        withContext(Dispatchers.Main) {
            if (builder == null) {
                navController?.navigate(route)
            } else {
                navController?.navigate(route, builder)
            }
        }
    }

    suspend fun popBackStack(){
        withContext(Dispatchers.Main) {
            navController?.popBackStack()
        }
    }
}