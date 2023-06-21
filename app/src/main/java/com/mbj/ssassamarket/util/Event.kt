package com.mbj.marketapp.util

class Event<T>(private val content: T) {

    private var hasBennHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBennHandled) {
            null
        } else {
            hasBennHandled = true
            content
        }
    }
}
