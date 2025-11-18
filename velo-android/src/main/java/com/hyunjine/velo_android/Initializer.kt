package com.hyunjine.velo_android

import android.content.Context
import androidx.startup.Initializer

internal lateinit var applicationContext: Context
    private set

internal class Initializer: Initializer<Unit> {
    override fun create(context: Context) {
        applicationContext = context.applicationContext
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}