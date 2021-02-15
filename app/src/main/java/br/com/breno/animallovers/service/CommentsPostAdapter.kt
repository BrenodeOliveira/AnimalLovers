package br.com.breno.animallovers.service

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
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
import kotlinx.android.synthetic.main.comments_post_item.view.*
import kotlinx.android.synthetic.main.fragment_publish.view.*
import kotlinx.android.synthetic.main.modal_likes_pets_post.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CommentsPostAdapter(
    private val comments: List<Comentario>,
    private val context: Context,
    private val post: Post
) : RecyclerView.Adapter<CommentsPostAdapter.ViewHolder>() {
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var db: DatabaseReference
    private lateinit var dBase: DatabaseReference
    private lateinit var dtBase: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comments_post_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comentario = comments[position]
        val myPreferences = ProjectPreferences(context)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        db = Firebase.database.reference
        dBase = Firebase.database.reference
        dtBase = Firebase.database.reference
        var pet: Pet
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(comentario.idOwner)
            .child(comentario.idPet)
            .child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onDataChange(snapshot: DataSnapshot) {
                        pet = snapshot.getValue<Pet>()!!

                        if (pet.pathFotoPerfil != "") {
                            var storageRef = storage.reference
                                .child(AnimalLoversConstants.STORAGE_ROOT.nome)
                                .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                                .child(pet.idOwner)
                                .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

                            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                                holder?.let {
                                    it.nome.text = pet.nome
                                    it.textComment.text = comentario.textoComentario
                                    it.photo.setImageBitmap(bmp)

                                    it.photo.setOnClickListener {

                                    }
                                    it.nome.setOnClickListener {

                                    }
                                }
                            }.addOnFailureListener {

                            }
                        } else {
                            holder?.let {
                                it.nome.text = pet.nome
                                it.textComment.text = comentario.textoComentario
                            }
                        }

                        holder?.let {
                            val formatter: DateTimeFormatter =
                                DateTimeFormatter.ofPattern(DateUtils.dateFrmt())
                            val start: LocalDateTime = LocalDateTime.parse(
                                comentario.dataHora,
                                formatter
                            )
                            val end: LocalDateTime = LocalDateTime.parse(
                                LocalDateTime.now().format(
                                    formatter
                                ), formatter
                            )
                            var dateDiffComment: String
                            if (ChronoUnit.DAYS.between(start, end) > 31) {
                                dateDiffComment = comentario.dataHora.substringBefore(" ")
                            } else if (ChronoUnit.DAYS.between(start, end) < 1) {
                                if (ChronoUnit.HOURS.between(start, end) > 1) {
                                    dateDiffComment =
                                        ChronoUnit.HOURS.between(start, end).toString() + " horas"
                                } else if (ChronoUnit.HOURS.between(start, end).toInt() == 1) {
                                    dateDiffComment =
                                        ChronoUnit.HOURS.between(start, end).toString() + " hora"
                                } else if (ChronoUnit.SECONDS.between(start, end) < 60) {
                                    dateDiffComment = ChronoUnit.SECONDS.between(start, end)
                                        .toString() + " segundos"
                                } else {
                                    dateDiffComment = ChronoUnit.MINUTES.between(start, end)
                                        .toString() + " minutos"
                                }
                            } else if ((ChronoUnit.DAYS.between(
                                    start,
                                    end
                                ) > 1) && (ChronoUnit.DAYS.between(
                                    start,
                                    end
                                ) < 4)
                            ) {
                                dateDiffComment =
                                    ChronoUnit.DAYS.between(start, end).toString() + " dias"
                            } else if ((ChronoUnit.DAYS.between(start, end).toInt() == 1)) {
                                dateDiffComment =
                                    ChronoUnit.DAYS.between(start, end).toString() + " dia"
                            } else {
                                dateDiffComment = comentario.dataHora.substringBefore(" ")
                            }
                            it.dateComment.text = dateDiffComment
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
            .child(comentario.idComentario)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var numLikes = 0

                    var hasPetLikedPost = false

                    if (snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)) {
                        val ownersPets: HashMap<String, HashMap<String, String>> =
                            snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome).value as HashMap<String, HashMap<String, String>>
                        for (i in 0 until ownersPets.size) {
                            val idOwnerPet = ownersPets.keys.toMutableList()[i]
                            for (j in 0 until (ownersPets.toMutableMap()[idOwnerPet]?.values?.size!!)) {
                                numLikes++
                                holder.numLikesComment.text = numLikes.toString()
                            }
                        }

                        if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)
                                .hasChild(auth.uid.toString())
                        ) {
                            println(
                                snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)
                                    .child(auth.uid.toString())
                            )

                            if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)
                                    .child(auth.uid.toString())
                                    .hasChild(myPreferences.getPetLogged().toString())
                            ) {
                                holder.likeComment.setColorFilter(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.colorAccent
                                    ), android.graphics.PorterDuff.Mode.MULTIPLY
                                )
                                hasPetLikedPost = true
                            }
                        }
                    }

                    holder.likeComment.setOnClickListener {
                        if (holder.numLikesComment.text != "") {

                            if (hasPetLikedPost) {
                                database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                                    .child(post.idOwner)//ID do dono do post
                                    .child(post.idPet)//ID do pet do post
                                    .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                                    .child(post.idPost)
                                    .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
                                    .child(comentario.idComentario)
                                    .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)
                                    .child(auth.uid.toString())
                                    .child(myPreferences.getPetLogged().toString())
                                    .setValue(null)

                                holder.likeComment.setColorFilter(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.icon_tint
                                    ), android.graphics.PorterDuff.Mode.MULTIPLY
                                )
                                numLikes--
                                hasPetLikedPost = false
                            } else {
                                database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                                    .child(post.idOwner)//ID do dono do post
                                    .child(post.idPet)//ID do pet do post
                                    .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                                    .child(post.idPost)
                                    .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
                                    .child(comentario.idComentario)
                                    .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)
                                    .child(auth.uid.toString())
                                    .child(myPreferences.getPetLogged().toString())
                                    .setValue(DateUtils.dataFormatWithMilliseconds())
                                holder.likeComment.setColorFilter(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.colorAccent
                                    ), android.graphics.PorterDuff.Mode.MULTIPLY
                                )
                                numLikes++
                                hasPetLikedPost = true
                            }
                            holder.numLikesComment.text = numLikes.toString()
                        } else {
                            holder.numLikesComment.text = "0"
                        }
                    }

                    holder.numLikesComment.setOnClickListener {
                        var petsArray = ArrayList<Pet>()

                        if (holder.numLikesComment.text != "0") {
                            dBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                                .child(post.idOwner)
                                .child(post.idPet)
                                .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                                .child(post.idPost)
                                .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
                                .child(comentario.idComentario)
                                .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)
                                .addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val ownersPets: HashMap<String, HashMap<String, String>> =
                                            snapshot.value as HashMap<String, HashMap<String, String>>

                                        dtBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {


                                                    for (i in 0 until ownersPets.size) {
                                                        val idOwnerPet =
                                                            ownersPets.keys.toMutableList()[i]
                                                        for (j in 0 until (ownersPets.toMutableMap()[idOwnerPet]?.values?.size!!)) {
                                                            pet = snapshot.child(idOwnerPet)
                                                                .child(ownersPets.toMutableMap()[idOwnerPet]?.keys?.toMutableList()!![j])
                                                                .child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome)
                                                                .getValue<Pet>()!!

                                                            petsArray.add(pet)
                                                        }
                                                    }

                                                    val view = LayoutInflater.from(context).inflate(R.layout.modal_likes_pets_post, it.parent as ViewGroup)

                                                    val recyclerView = view.findViewById(R.id.recycler_pets_likes_post) as? RecyclerView

                                                    recyclerView!!.layoutManager = LinearLayoutManager(context)
                                                    recyclerView.adapter = ProfilesLikesPostAdapter(petsArray, context)

                                                    val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                                                    recyclerView.layoutManager = layoutManager
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    println(error.toString())
                                                }

                                            })
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        println(error.toString())
                                    }

                                })
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.toString())
                }

            })

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nome = itemView.tv_pet_name_modal_comment
        val dateComment = itemView.tv_date_time_comment_modal_comment
        val textComment = itemView.tv_comment_text_modal_comment
        val numLikesComment = itemView.tv_num_likes_comment_modal_comment
        val likeComment = itemView.iv_action_fav_comment_modal_comment
        var photo = itemView.iv_icon_foto_perfil_pet_modal_comment
        val abc = itemView.recycler_pets_likes_post

        fun bindView(comment: Comentario) {
            val nome = itemView.tv_pet_name_modal_comment
            val dateComment = itemView.tv_date_time_comment_modal_comment
            val textComment = itemView.tv_comment_text_modal_comment
            val numLikesComment = itemView.tv_num_likes_comment_modal_comment
            val likeComment = itemView.iv_action_fav_comment_modal_comment
            var photo = itemView.iv_icon_foto_perfil_pet_modal_comment

            dateComment.text = comment.dataHora
            textComment.text = comment.textoComentario
        }
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}