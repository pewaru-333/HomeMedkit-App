package ru.application.homemedkit.utils.extensions

import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
fun String.toSHA256() = MessageDigest.getInstance("SHA-256").digest(toByteArray()).toHexString()