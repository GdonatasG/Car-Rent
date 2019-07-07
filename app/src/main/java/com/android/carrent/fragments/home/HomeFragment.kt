package com.android.carrent.fragments.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.models.User
import com.android.carrent.utils.makeToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {
    private var TAG: String = "HomeActivity"
    private var typeOfSort: Int = 0

    // Firebase
    private var mAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Init firebase
        mAuth = FirebaseAuth.getInstance()

        if (mAuth?.currentUser == null) {
            Log.d(TAG, "User is not logged in, starting MainActivity")
            startMainActivity()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v: View = inflater.inflate(R.layout.fragment_home, container, false)

        // toolbar
        (activity as AppCompatActivity).setSupportActionBar(v.toolbar as Toolbar)
        setHasOptionsMenu(true)
        // Init user balance into toolbar
        initBalance()

        return v
    }

    private fun initBalance() {
        FirebaseDatabase.getInstance().getReference("/users/${mAuth?.currentUser?.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    makeToast(p0.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val user = p0.getValue(User::class.java)!!
                        (activity as AppCompatActivity).supportActionBar?.title = user.balance.toString() + " " +
                                resources.getText(R.string.nav_header_currency_euro)
                    }
                }

            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        var menuInflater: MenuInflater = activity!!.menuInflater
        menuInflater.inflate(R.menu.home_main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_search -> {
                println("clicked on btn search")
            }
            R.id.btn_sort -> {
                view?.let {
                    selectSortView(it)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        var menuInflater: MenuInflater = activity!!.menuInflater
        menuInflater.inflate(R.menu.home_sort_menu, menu)
        var action_all: MenuItem = menu.findItem(R.id.action_all)
        var action_nearest: MenuItem = menu.findItem(R.id.action_nearest)

        if (typeOfSort == 1) {
            action_all.setChecked(true)
        } else if (typeOfSort == 2) {
            action_nearest.setChecked(true)
        }
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_all -> {
                typeOfSort = 1
                println("clicked on action all")
            }
            R.id.action_nearest -> {
                typeOfSort = 2
                println("clicked on action nearest")
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun selectSortView(v: View) {
        registerForContextMenu(v)
        activity?.openContextMenu(v)
    }

    private fun startMainActivity() {
        activity?.finish()
        startActivity(Intent(activity, MainActivity::class.java))
    }
}
