package com.livebarn.android.sreemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.livebarn.android.sreelibrary.Constants
import com.livebarn.android.sreelibrary.model.Authority
import com.livebarn.android.sreemanager.adapter.UsersAdapter
import com.livebarn.android.sreemanager.app.ManagerApplication
import com.livebarn.android.sreemanager.contract.UsersContract
import com.livebarn.android.sreemanager.presenter.UsersPresenter

class UsersActivity : AppCompatActivity(), UsersContract.View {

    private var presenter: UsersContract.Presenter? = null

    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        setPresenter(
            UsersPresenter(
                this,
                (application as? ManagerApplication)?.dbReference
            )
        )

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            presenter?.let {
                adapter = UsersAdapter(it)
            }
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            )
        }

        presenter?.onViewCreated()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onViewDestroyed()
        presenter = null
        recyclerView = null
    }

    override fun onUserClicked(authority: Authority?, position: Int) {
        if (authority == Authority.OWNER) return

        val view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_layout_authority, null, false)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Change authority")
            .setView(view)
            .setNegativeButton("Close", null)
            .create()

        val buttonUser: Button = view.findViewById(R.id.button_user)
        val buttonConfirmedUser: Button = view.findViewById(R.id.button_confirmed_user)
        val buttonAdmin: Button = view.findViewById(R.id.button_admin)
        buttonUser.text = Constants.DB_AUTHORITY_USER
        buttonConfirmedUser.text = Constants.DB_AUTHORITY_CONFIRMED_USER
        buttonAdmin.text = Constants.DB_AUTHORITY_ADMIN

        buttonUser.setOnClickListener {
            presenter?.changeAuthority(Authority.USER, position)
            dialog.dismiss()
        }
        buttonConfirmedUser.setOnClickListener {
            presenter?.changeAuthority(Authority.CONFIRMED_USER, position)
            dialog.dismiss()
        }
        buttonAdmin.setOnClickListener {
            presenter?.changeAuthority(Authority.ADMIN, position)
            dialog.dismiss()
        }

        when (authority) {
            Authority.USER -> {
                buttonUser.isEnabled = false
                dialog.show()
            }
            Authority.CONFIRMED_USER -> {
                buttonConfirmedUser.isEnabled = false
                dialog.show()
            }
            Authority.ADMIN -> {
                buttonAdmin.isEnabled = false
                dialog.show()
            }
            else -> {

            }
        }
    }

    override fun onPermissionChanged(position: Int) {
        recyclerView?.adapter?.notifyItemChanged(position)
    }

    override fun onUserChanged(position: Int) {
        recyclerView?.adapter?.notifyItemChanged(position)
    }

    override fun onUserRemoved(position: Int) {
        recyclerView?.adapter?.notifyItemRemoved(position)
    }

    override fun onUserAdded(position: Int) {
        recyclerView?.adapter?.notifyItemInserted(position)
    }

    override fun setPresenter(presenter: UsersContract.Presenter) {
        this.presenter = presenter
    }
}