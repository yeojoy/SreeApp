package com.livebarn.android.sreemanager.contract

import com.livebarn.android.sreelibrary.BasePresenter
import com.livebarn.android.sreelibrary.BaseView
import com.livebarn.android.sreelibrary.model.CelebrationType
import com.livebarn.android.sreelibrary.model.User

interface ManagerContract {
    interface Presenter: BasePresenter {
        fun clickAlignButton(alignment: String)
        fun clickSaveMessageButton(message: String)

        fun bindDatabase()
        fun unbindDatabase()

        fun numberOfGifs(): Int
        fun gifAt(position: Int): String?
        fun clickGif(position: Int)

        fun numberOfTypes(): Int
        fun typeAt(position: Int): CelebrationType?
        fun clickType(position: Int)

        fun clickSignOut()
        fun clickUsers()

        fun isAdminUser(): Boolean
    }

    interface View: BaseView<Presenter> {
        fun onAlignButtonClicked(alignment: String?)
        fun onSaveMessageButtonClicked(message: String?)
        fun warnNoAuthority()

        fun onTypeClicked()
        fun onGifClicked(url: String?)

        fun onSignOutActionClicked()
        fun onUsersActionClicked()
        fun onUserFetched(user: User?)
    }
}