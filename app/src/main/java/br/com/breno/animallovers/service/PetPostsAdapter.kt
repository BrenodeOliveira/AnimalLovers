package br.com.breno.animallovers.service

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Comentario
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.feed_item.view.*

class PetPostsAdapter (private val posts: List<Post>, val petPost: Pet, private val context: Context)  : RecyclerView.Adapter<PetPostsAdapter.ViewHolder>(){
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var db: DatabaseReference
    private var comentario = Comentario()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.feed_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[(posts.size - 1) - position]
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        var arraylist = ArrayList<Comentario>()

        val myPreferences = ProjectPreferences(context)

        if (post.pathPub != "") {
            val storageRef = storage.reference.child(AnimalLoversConstants.STORAGE_ROOT.nome).child(
                AnimalLoversConstants.CONST_ROOT_POSTS.nome
            ).child(post.idOwner).child(post.idPet).child(post.dataHora)
            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                holder.let {
                    it.name.text = post.nomePet
                    it.dateTime.text = post.dataHora.substringBefore(".")
                    it.description.text = post.legenda
                    it.photoPost.setImageBitmap(bmp)

                    if(it.photoPost.drawable == null) {
                        it.photoPost.visibility = View.INVISIBLE
                    }
                }
            }.addOnFailureListener {
                println(it.toString())
            }
        }
        else {
            holder.let {
                it.name.text = post.nomePet
                it.dateTime.text = post.dataHora
                it.description.text = post.legenda
                val layoutParams: ViewGroup.LayoutParams = it.photoPost.getLayoutParams()
                layoutParams.height = 50
                it.photoPost.setLayoutParams(layoutParams)
            }
        }
        database = Firebase.database.reference
        db = Firebase.database.reference
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var numLikes = 0

                    var hasPetLikedPost = false

                    if (snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)) {
                        val ownersPets: HashMap<String, HashMap<String, String>> = snapshot.child(
                            AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome).value as HashMap<String, HashMap<String, String>>
                        for (i in 0 until ownersPets.size) {
                            val idOwnerPet = ownersPets.keys.toMutableList()[i]
                            for (j in 0 until (ownersPets.toMutableMap()[idOwnerPet]?.values?.size!!)) {
                                numLikes ++
                                holder.numLikesPost.text = numLikes.toString()
                            }
                        }

                        if(snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome).hasChild(auth.uid.toString())) {
                            println(snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome).child(auth.uid.toString()))

                            if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome).child(auth.uid.toString()).hasChild(myPreferences.getPetLogged().toString())) {
                                holder.likePost.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY)
                                hasPetLikedPost = true
                            }
                        }
                    }


                    holder.likePost.setOnClickListener {
                        if (holder.numLikesPost.text != "") {

                            if (hasPetLikedPost) {
                                database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                                    .child(post.idOwner)
                                    .child(post.idPet)
                                    .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                                    .child(post.idPost)
                                    .child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)
                                    .child(auth.uid.toString())
                                    .child(myPreferences.getPetLogged().toString())
                                    .setValue(null)
                                holder.likePost.setColorFilter(ContextCompat.getColor(context, R.color.icon_tint), android.graphics.PorterDuff.Mode.MULTIPLY)
                                numLikes--
                                hasPetLikedPost = false
                            } else {
                                database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                                    .child(post.idOwner)
                                    .child(post.idPet)
                                    .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                                    .child(post.idPost)
                                    .child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)
                                    .child(auth.uid.toString())
                                    .child(myPreferences.getPetLogged().toString())
                                    .setValue(DateUtils.dataFormatWithMilliseconds())
                                holder.likePost.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY)
                                numLikes++
                                hasPetLikedPost = true
                            }
                            holder.numLikesPost.text = numLikes.toString()
                        } else {
                            holder.numLikesPost.text = "0"
                        }
                    }

                    holder.numLikesPost.setOnClickListener {
                        val profilesLikesPostAdapter = ProfilesLikesPostService(post)
                        val manager: FragmentManager = (context as AppCompatActivity).supportFragmentManager
                        profilesLikesPostAdapter.show(manager, "likesPost")
                    }

                    holder.commentPost.setOnClickListener {
                        val profilesLikesPostAdapter = CommentsPostService(post, arraylist)
                        val manager: FragmentManager = (context as AppCompatActivity).supportFragmentManager
                        profilesLikesPostAdapter.show(manager, "likesPost")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.toString())
                }

            })

        db.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dSnapshot: DataSnapshot) {

                    arraylist.clear()
                    for (i in 1 until dSnapshot.childrenCount + 1) {
                        if (dSnapshot.hasChild(i.toString())) {
                            var rootNodeComment = dSnapshot.child(i.toString()).child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome).value as HashMap<String, HashMap<String, HashMap<String, String>>>
                            val idOnwer = rootNodeComment.keys.toMutableList()[0]
                            val idPet = rootNodeComment[idOnwer]?.keys?.toMutableList()?.get(0)

                            comentario = (dSnapshot.child(i.toString())
                                .child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome)
                                .child(idOnwer)
                                .child(idPet.toString())
                                .getValue<Comentario>())!!
                            if (comentario.comentarioAtivo) {
                                arraylist.add(comentario)
                            }
                        }
                    }
                    holder.numCommentsPost.text = arraylist.size.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.toString())
                }

            })
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.tv_pet_name_post_feed
        val dateTime: TextView = itemView.tv_date_time_post_feed
        var description: TextView = itemView.tv_pet_description_post_feed
        var photoPost: ImageView = itemView.iv_photo_post_feed
        var likePost: ImageView = itemView.iv_action_fav
        var commentPost: ImageView = itemView.iv_action_comment
        var numLikesPost: TextView = itemView.tv_num_likes_post
        var numCommentsPost: TextView = itemView.tv_num_comments_post

        fun bindView(post: Post, pet: Pet) {
            val name = itemView.tv_pet_name_post_feed
            val dateTime = itemView.tv_date_time_post_feed
            val description = itemView.tv_pet_description_post_feed

            name.text = pet.nome
            dateTime.text = post.dataHora
            description.text = post.legenda
        }
    }
}