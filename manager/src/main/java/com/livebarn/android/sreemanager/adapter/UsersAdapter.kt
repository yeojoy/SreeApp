package com.livebarn.android.sreemanager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.livebarn.android.sreemanager.R
import com.livebarn.android.sreemanager.contract.UsersContract

class UsersAdapter(
    private val presenter: UsersContract.Presenter
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_user, parent, false)
        return UserViewHolder(view, presenter::clickUser)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        presenter.userAt(position)?.let {
            holder.textViewUsername.text = it.username
            holder.textViewEmail.text = it.email
            holder.textViewUid.text = it.uid
            holder.textViewAuthority.text = it.authority
        }
    }

    override fun getItemCount() = presenter.numberOfUsers()

    class UserViewHolder(
        itemView: View,
        listener: ((position: Int) -> Unit)?
    ) : ViewHolder(itemView) {

        val textViewUsername: TextView = itemView.findViewById(R.id.text_view_username)
        val textViewEmail: TextView = itemView.findViewById(R.id.text_view_email)
        val textViewUid: TextView = itemView.findViewById(R.id.text_view_uid)
        val textViewAuthority: TextView = itemView.findViewById(R.id.text_view_authority)

        init {
            itemView.setOnClickListener { listener?.invoke(adapterPosition) }
        }
    }
}