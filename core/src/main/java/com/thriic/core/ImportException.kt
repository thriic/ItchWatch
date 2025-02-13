package com.thriic.core

import okhttp3.Response

sealed class ImportException(message: String) : Exception(message) {
    class NetworkError(e: Exception) : ImportException("err: ${e.message}")
    class ExistError() : ImportException("Game already exists")
}