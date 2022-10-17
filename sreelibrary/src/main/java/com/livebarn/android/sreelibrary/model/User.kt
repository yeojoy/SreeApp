package com.livebarn.android.sreelibrary.model

data class User(
    val uid: String,
    val createAt: Long?,
    val email: String?,
    val permission: String?,
    val username: String?
) {
    companion object {
        fun newUser(uid: String, map: HashMap<* ,*>?): User {
            return User(
                uid,
                map?.get("createdAt")?.toString()?.toLong(),
                map?.get("email")?.toString(),
                map?.get("permission")?.toString(),
                map?.get("username")?.toString()
            )
        }
    }
}