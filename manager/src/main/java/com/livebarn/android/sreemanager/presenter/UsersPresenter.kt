package com.livebarn.android.sreemanager.presenter

import android.util.Log
import com.google.firebase.database.*
import com.livebarn.android.sreelibrary.Constants
import com.livebarn.android.sreelibrary.model.Authority
import com.livebarn.android.sreelibrary.model.User
import com.livebarn.android.sreemanager.contract.UsersContract

class UsersPresenter(
    private var view: UsersContract.View?,
    dbReference: DatabaseReference?
) : UsersContract.Presenter {

    companion object {
        private const val TAG = "UsersPresenter"
    }

    private var usersDBReference = dbReference?.child(Constants.DB_TABLE_USERS)

    private val users = mutableListOf<User>()

    override fun numberOfUsers() = users.size

    override fun userAt(position: Int) = try {
        users[position]
    } catch (e: Exception) {
        Log.e(TAG, "userAt($position): ${e.message}")
        null
    }

    override fun clickUser(position: Int) {
        userAt(position)?.let {
            view?.onUserClicked(Authority.findByTitle(it.authority), position)
        }
    }

    override fun changeAuthority(authority: Authority?, position: Int) {
        userAt(position)?.let {
            authority?.let { auth ->
                usersDBReference?.child(it.uid)?.child(Constants.DB_FIELD_AUTHORITY)
                    ?.setValue(auth.title)
            }
        }
    }

    override fun onViewCreated() {
        usersDBReference?.addChildEventListener(usersDBValueListener)
    }

    override fun onViewDestroyed() {
        view = null
        usersDBReference?.removeEventListener(usersDBValueListener)
        usersDBReference = null
    }

    private val usersDBValueListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            if (snapshot.value == null)
                return

            val user = User.newUser(snapshot.key.toString(), snapshot.value as? HashMap<*, *>)
            val index = users.indexOf(user)

            if (index < 0) {
                users.add(user)
                view?.onUserAdded(users.size - 1)
            } else {
                val existingUser = users[index]
                if (user.username != existingUser.username ||
                        user.email != existingUser.email ||
                        user.authority != existingUser.authority) {
                    users[index] = user
                    view?.onUserChanged(index)
                }
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            if (snapshot.value == null)
                return

            val user = User.newUser(snapshot.key.toString(), snapshot.value as? HashMap<*, *>)
            val index = users.indexOf(user)
            if (index >= 0) {
                users[index] = user
                view?.onUserChanged(index)
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            if (snapshot.value == null)
                return

            val user = User.newUser(snapshot.key.toString(), snapshot.value as? HashMap<*, *>)
            val index = users.indexOf(user)
            if (index >= 0) {
                users.removeAt(index)
                view?.onUserRemoved(index)
            }

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            // TODO("Not yet implemented")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() with usersDBValueListener: ${error.message}")
        }
    }
}