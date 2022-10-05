package com.livebarn.android.sreelibrary.model

enum class CelebrationType(val id: Int, val title: String) {
    NONE(0, "No choice"),
    NEW_PEOPLE(1, "New people!"),
    HAPPY_B_DAY(2, "Happy Birthday!"),
    ANNIVERSARY(3, "Anniversary");

    companion object {
        fun findById(id: Int): CelebrationType {
            return values().first { it.id == id }
        }
    }
}