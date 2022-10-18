package com.livebarn.android.sreemanager.contract

import com.livebarn.android.sreelibrary.BasePresenter
import com.livebarn.android.sreelibrary.BaseView
import com.livebarn.android.sreelibrary.model.Authority
import com.livebarn.android.sreelibrary.model.User

interface UsersContract {
    interface Presenter: BasePresenter {
        fun numberOfUsers(): Int
        fun userAt(position: Int): User?
        fun clickUser(position: Int)
        fun changeAuthority(authority: Authority?, position: Int)
    }

    interface View: BaseView<Presenter> {
        fun onUserClicked(authority: Authority?, position: Int)
        fun onPermissionChanged(position: Int)

        fun onUserChanged(position: Int)
        fun onUserRemoved(position: Int)
        fun onUserAdded(position: Int)
    }
}