package com.sundbybergsit.cromfortune

import kotlin.reflect.KClass

val Any.TAG: String
    get() {
        val name = when (this) {
            is KClass<*> -> {
                this.java.simpleName
            }
            is Class<*> -> {
                this.simpleName
            }
            else -> {
                this::class.java.simpleName
            }
        }
        check(name != "Companion") { "This is probably not the value you want." }
        return if (name.length > 23) {
            val indexOfLast = name.indexOfLast { it.isUpperCase() }
            name.substring(0, 23 - (name.length - indexOfLast)) + name.substring(indexOfLast, name.length)
        } else {
            name
        }
    }
