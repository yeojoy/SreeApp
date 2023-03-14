package com.livebarn.android.sreelibrary

object Constants {

    /* DB */
    const val DB_URL = "https://sree-project-1f7a5-default-rtdb.firebaseio.com"
    const val DB_NAME = "SreeApp"
    const val DB_TABLE_USERS = "users"
    const val DB_PATH_MESSAGE = "message"
    const val DB_PATH_TARGET_GIF = "targetGif"
    const val DB_PATH_LOCATION = "location"
    const val DB_PATH_CATEGORIES = "categories"
    const val DB_PATH_CATEGORY_GIFS = "category-gifs"
    const val DB_PATH_GIFS = "gifs"

    const val DB_FIELD_CREATED_AT = "createdAt"
    const val DB_FIELD_EMAIL = "email"
    const val DB_FIELD_AUTHORITY = "authority"
    const val DB_FIELD_USERNAME = "username"
    const val DB_FIELD_ARRIVAL_RANK = "arrivalRank"

    const val DB_AUTHORITY_USER = "User"
    const val DB_AUTHORITY_CONFIRMED_USER = "Confirmed User"
    const val DB_AUTHORITY_ADMIN = "Admin"
    const val DB_AUTHORITY_OWNER = "Owner"
    /* End of DB */

    /* value */
    const val DB_VALUE_TOP = "top"
    const val DB_VALUE_CENTER = "center"
    const val DB_VALUE_BOTTOM = "bottom"
    /* End of value */

    val claps = hashMapOf(
        "Standing Ovation" to "https://media4.giphy.com/media/qIXVd1RoKGqlO/giphy.gif?cid=790b761168f0f0aebcb2525b012b1240e887d85d8b4b790d",
        "Morgan Freeman" to "https://media2.giphy.com/media/l3q2XhfQ8oCkm1Ts4/giphy.gif?cid=790b76115057d2195edf89df83fc0677296f65f3b547a8b8",
        "Simon Cowell" to "https://media0.giphy.com/media/nbvFVPiEiJH6JOGIok/giphy.gif?cid=790b7611aa1748755059406e74d539ee0eabc7182a7bb432",
        "People" to "https://media1.giphy.com/media/KJCsv8KcpwHjG/giphy.gif?cid=790b761117c1e8b4aeb3b37fb99e1f00dfc66c40883bd577",
        "Joker" to "https://media0.giphy.com/media/11OOAQSnUaZT2M/giphy.gif?cid=790b7611cf9f4ecfc8926260c9113d8467048c7254d0f5d8"
    )

    val happyBDay = hashMapOf(
        "Boy" to "https://media2.giphy.com/media/gw3JY2uqiaXKaQXS/giphy.gif?cid=790b76119f16adeefb4f1f3e100f01885a60a5c721c16b46",
        "Seth Meyers" to "https://media4.giphy.com/media/xUOrw4tlQfCTGmD5Kw/giphy.gif?cid=790b76119630821c55be4b7de9ca5fbaf7acae4439f7e320",
        "The Office" to "https://media2.giphy.com/media/g5R9dok94mrIvplmZd/giphy.gif?cid=790b76111853fb2e66dbc070272813fc26e8369dc5783647",
        "Dancing" to "https://media2.giphy.com/media/QXhOMKpaLQvZnjftXF/giphy.gif?cid=790b761185cc439dfe3eef77dafd17f47a619232ce3f464fh"
    )

    val graduation = hashMapOf(
        "default" to "https://media3.giphy.com/media/AjIW8jJaL8xIoXV6wd/giphy.gif?cid=ecf05e47imfabp6qox2vl5s357tp645jetdd6x65kig60pcb"
    )
}