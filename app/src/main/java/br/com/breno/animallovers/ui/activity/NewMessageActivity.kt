package br.com.breno.animallovers.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.Login
import br.com.breno.animallovers.model.User
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.item_latest_message.view.*
import kotlinx.android.synthetic.main.item_user_row.*
import kotlinx.android.synthetic.main.item_user_row.view.*

class NewMessageActivity : AppCompatActivity() {

    companion object {
        const val USER_KEY = "USER_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Selecione um contato"

        fetchUsers()
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/conta")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                snapshot.children.forEach {
                    Log.d("NewMessage", it.toString())

                    val user = it.child("/dono").getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }
                }

                adapter.setOnItemClickListener { item, _ ->

                    val userItem = item as UserItem

                    val intent = Intent(
                        this@NewMessageActivity, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                    finish()
                }

                recycler_new_message.adapter = adapter
            }


            override fun onCancelled(error: DatabaseError) {
                println(error.message)
            }
        })
    }
}

class UserItem(val user: User) : Item<ViewHolder>() {
    private var base: DatabaseReference = FirebaseDatabase.getInstance().reference


    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_name_user_row.text = user.usuario

        base.child(AnimalLoversConstants.DATABASE_ENTITY_CONTROL_LOGIN.nome).addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.hasChild(user!!.id)) {
                    val ownerLogin = snapshot.child(user.id).getValue<Login>()!!

                    if(ownerLogin.logged) {
                        viewHolder.itemView.tv_status_pet_chat_list_new_message.background = viewHolder.itemView.context.getDrawable(R.drawable.logged_owner_icon)
                    }
                    else {
                        viewHolder.itemView.tv_status_pet_chat_list_new_message.background = viewHolder.itemView.context.getDrawable(R.drawable.likes_post_count)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println(error.toString())
            }

        })

    }

    override fun getLayout(): Int {
        return R.layout.item_user_row
    }

}
