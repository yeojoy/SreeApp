package com.livebarn.android.sreelibrary.model

import com.livebarn.android.sreelibrary.Constants

enum class Authority(val id: Int, val title: String) {
    USER(1, Constants.DB_AUTHORITY_USER),
    CONFIRMED_USER(2, Constants.DB_AUTHORITY_CONFIRMED_USER),
    ADMIN(3, Constants.DB_AUTHORITY_ADMIN),
    OWNER(99, Constants.DB_AUTHORITY_OWNER);

    companion object {
        fun findByTitle(title: String?): Authority? {
            if (title.isNullOrEmpty()) return null

            for (auth in values()) {
                if (title.lowercase() == auth.title.lowercase()) return auth
            }
            return null
        }
    }
}