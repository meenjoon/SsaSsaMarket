package com.mbj.marketapp.util

import androidx.lifecycle.Observer

class EventObserver<T>(private val onEventUnHandleContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>) {
        event?.getContentIfNotHandled()?.let {
            onEventUnHandleContent(it)
        }
    }
}
