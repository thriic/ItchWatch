package com.thriic.core.network

import okhttp3.Response

sealed class NetworkException(message: String) : Exception(message) {
    class NetworkError(response: Response) : NetworkException("Network request failed with code: ${response.code}")
    class ParsingError(extra: String) : NetworkException("Failed to parse $extra")
}