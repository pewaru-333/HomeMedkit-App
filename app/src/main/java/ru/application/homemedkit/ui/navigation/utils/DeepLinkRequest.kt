package ru.application.homemedkit.ui.navigation.utils

import android.net.Uri

internal class DeepLinkRequest(val uri: Uri) {
    val pathSegments: List<String> = uri.pathSegments
    val queries = buildMap {
        uri.queryParameterNames.forEach { argName ->
            put(argName, uri.getQueryParameter(argName)!!)
        }
    }
}