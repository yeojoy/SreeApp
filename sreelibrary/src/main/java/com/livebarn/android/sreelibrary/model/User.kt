package com.livebarn.android.sreelibrary.model

import com.livebarn.android.sreelibrary.Constants

data class User(
    val uid: String,
    val createAt: Long?,
    val email: String?,
    val authority: String?,
    val username: String?
) {
    companion object {
        fun newUser(uid: String, map: HashMap<* ,*>?): User {
            return User(
                uid,
                map?.get(Constants.DB_FIELD_CREATED_AT)?.toString()?.toLong(),
                map?.get(Constants.DB_FIELD_EMAIL)?.toString(),
                map?.get(Constants.DB_FIELD_AUTHORITY)?.toString(),
                map?.get(Constants.DB_FIELD_USERNAME)?.toString()
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        return this.uid == (other as User).uid
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + (createAt?.hashCode() ?: 0)
        return result
    }
}