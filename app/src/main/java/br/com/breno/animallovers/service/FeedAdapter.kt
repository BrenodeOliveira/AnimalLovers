package br.com.breno.animallovers.service

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.ui.activity.FeedActivity
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.feed_item.view.*

class FeedAdapter(private val posts: List<Post>, val petPost: Pet, private val context: Context) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.feed_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        val pet = petPost
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        if (post.pathPub != "") {
            var storageRef = storage.reference
                .child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                .child(auth.uid.toString())
                .child(pet.id)
                .child(post.dataHora)

            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                holder?.let {
                    it.name.text = pet.nome
                    it.dateTime.text = post.dataHora
                    it.description.text = post.legenda
                    it.photoPost.setImageBitmap(bmp)
                }
            }.addOnFailureListener {

            }
        }
        else {
            holder?.let {
                it.name.text = pet.nome
                it.dateTime.text = post.dataHora
                it.description.text = post.legenda
                it.photoPost.visibility = View.INVISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.tv_pet_name_post_feed
        val dateTime = itemView.tv_date_time_post_feed
        var description = itemView.tv_pet_description_post_feed
        var photoPost = itemView.iv_photo_post_feed

        fun bindView(post: Post, pet: Pet) {
            val name = itemView.tv_pet_name_post_feed
            val dateTime = itemView.tv_date_time_post_feed
            val description = itemView.tv_pet_description_post_feed
            val photoPost = itemView.iv_photo_post_feed

            name.text = pet.nome
            dateTime.text = post.dataHora
            description.text = post.legenda
        }
    }
}

