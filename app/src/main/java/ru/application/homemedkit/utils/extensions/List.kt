package ru.application.homemedkit.utils.extensions

fun <E> List<E>.concat(element: E) = this + element

fun <E> List<E>.toggle(element: E) = if (element in this) this - element else this + element