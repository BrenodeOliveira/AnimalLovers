package br.com.breno.animallovers.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Comentario
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.model.ReportComment
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
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.comments_post_item.view.*
import kotlinx.android.synthetic.main.edit_comment.*
import kotlinx.android.synthetic.main.edit_comment.tv_comment_text_edit_comment
import kotlinx.android.synthetic.main.modal_likes_pets_post.view.*
import kotlinx.android.synthetic.main.report_comment.*
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
    private lateinit var dt: DatabaseReference

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
        dt = Firebase.database.reference
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

                                                    val view = LayoutInflater.from(context).inflate(
                                                        R.layout.modal_likes_pets_post,
                                                        it.parent as ViewGroup
                                                    )

                                                    val recyclerView =
                                                        view.findViewById(R.id.recycler_pets_likes_post) as? RecyclerView

                                                    recyclerView!!.layoutManager =
                                                        LinearLayoutManager(
                                                            context
                                                        )
                                                    recyclerView.adapter = ProfilesLikesPostAdapter(
                                                        petsArray,
                                                        context
                                                    )

                                                    val layoutManager = StaggeredGridLayoutManager(
                                                        1,
                                                        StaggeredGridLayoutManager.VERTICAL
                                                    )
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

                    holder.optionsOpenIcon.setOnClickListener {

                        if (comentario.idOwner == auth.uid && comentario.idPet == myPreferences.getPetLogged()) {
                            val popupMenu = PopupMenu(context, holder.itemView)
                            popupMenu.setOnMenuItemClickListener {
                                when (it.itemId) {
                                    R.id.item_copy_menu_options_owner -> {
                                        val clipboard: ClipboardManager = context.getSystemService(
                                            Context.CLIPBOARD_SERVICE
                                        ) as ClipboardManager
                                        val clip: ClipData = ClipData.newPlainText(
                                            "label",
                                            comentario.textoComentario
                                        )
                                        clipboard.setPrimaryClip(clip)
                                        Toasty.info(
                                            context,
                                            "Comentário copiado para a área de transferência"
                                        ).show()
                                        return@setOnMenuItemClickListener true
                                    }

                                    R.id.item_edit_menu_options -> {
                                        val mDialogView = LayoutInflater.from(context).inflate(R.layout.edit_comment, null)
                                        val mBuilder = AlertDialog.Builder(context).setView(mDialogView)
                                        val mAlertDialog = mBuilder.show()

                                        mAlertDialog.tv_comment_text_edit_comment.text = comentario.textoComentario
                                        mAlertDialog.et_input_comment_edit_comment.setText(comentario.textoComentario)

                                        mAlertDialog.bt_cancel_edit_comment.setOnClickListener {
                                            mAlertDialog.dismiss()
                                        }

                                        mAlertDialog.bt_update_edit_comment.setOnClickListener {
                                            if (mAlertDialog.tv_comment_text_edit_comment.text != mAlertDialog.et_input_comment_edit_comment.text) {
                                                comentario.textoComentario = mAlertDialog.et_input_comment_edit_comment.text.toString()

                                                database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                                                    .child(post.idOwner)
                                                    .child(post.idPet)
                                                    .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                                                    .child(post.idPost)
                                                    .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
                                                    .child(comentario.idComentario)
                                                    .child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome)
                                                    .child(comentario.idOwner)
                                                    .child(comentario.idPet)
                                                    .setValue(comentario)
                                                mAlertDialog.dismiss()
                                            }
                                        }
                                        return@setOnMenuItemClickListener true
                                    }

                                    R.id.item_delete_menu_options -> {

                                        AlertDialog.Builder(context)
                                            .setTitle(R.string.delete_comment_title)
                                            .setMessage(R.string.delete_comment_message)
                                            .setPositiveButton(R.string.yes) { dialog, which ->

                                                comentario.comentarioAtivo = false

                                                database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                                                    .child(post.idOwner)
                                                    .child(post.idPet)
                                                    .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                                                    .child(post.idPost)
                                                    .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
                                                    .child(comentario.idComentario)
                                                    .child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome)
                                                    .child(comentario.idOwner)
                                                    .child(comentario.idPet)
                                                    .setValue(comentario)
                                            }
                                            .setNegativeButton(R.string.no, null)
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show()

                                        return@setOnMenuItemClickListener true
                                    }

                                }
                                return@setOnMenuItemClickListener false
                            }

                            popupMenu.inflate(R.menu.menu_options_owner)
                            popupMenu.show()
                        } else {
                            val popupMenu = PopupMenu(context, holder.itemView)
                            popupMenu.setOnMenuItemClickListener {
                                when (it.itemId) {
                                    R.id.item_copy_menu_options -> {
                                        val clipboard: ClipboardManager = context.getSystemService(
                                            Context.CLIPBOARD_SERVICE
                                        ) as ClipboardManager
                                        val clip: ClipData = ClipData.newPlainText(
                                            "label",
                                            comentario.textoComentario
                                        )
                                        clipboard.setPrimaryClip(clip)
                                        Toasty.info(
                                            context,
                                            "Comentário copiado para a área de transferência"
                                        ).show()
                                        return@setOnMenuItemClickListener true

                                    }

                                    R.id.item_report_menu_options -> {
                                        var listOfReasonsReportted = ArrayList<String>()

                                        val mDialogView = LayoutInflater.from(context).inflate(R.layout.report_comment, null)
                                        val mBuilder = AlertDialog.Builder(context).setView(mDialogView)
                                        val mAlertDialog = mBuilder.show()

                                        mAlertDialog.tv_is_offensive_report_comment2.setOnClickListener {
                                            if(mAlertDialog.tv_is_offensive_report_comment2.tag == R.drawable.selected_option_background) {
                                                mAlertDialog.tv_is_offensive_report_comment2.background = ContextCompat.getDrawable(context, R.drawable.likes_post_count)
                                                mAlertDialog.tv_is_offensive_report_comment2.tag = R.drawable.likes_post_count

                                                if(listOfReasonsReportted.contains(mAlertDialog.tv_is_offensive_report_comment2.text.toString())) {
                                                    listOfReasonsReportted.remove(mAlertDialog.tv_is_offensive_report_comment2.text.toString())
                                                }
                                            }
                                            else {
                                                mAlertDialog.tv_is_offensive_report_comment2.background = ContextCompat.getDrawable(context, R.drawable.selected_option_background)
                                                mAlertDialog.tv_is_offensive_report_comment2.tag = R.drawable.selected_option_background
                                                listOfReasonsReportted.add(mAlertDialog.tv_is_offensive_report_comment2.text.toString())
                                            }
                                        }
                                        mAlertDialog.tv_sexual_content_comment3.setOnClickListener {
                                            if(mAlertDialog.tv_sexual_content_comment3.tag == R.drawable.selected_option_background) {
                                                mAlertDialog.tv_sexual_content_comment3.background = ContextCompat.getDrawable(context, R.drawable.likes_post_count)
                                                mAlertDialog.tv_sexual_content_comment3.tag = R.drawable.likes_post_count

                                                if(listOfReasonsReportted.contains(mAlertDialog.tv_sexual_content_comment3.text.toString())) {
                                                    listOfReasonsReportted.remove(mAlertDialog.tv_sexual_content_comment3.text.toString())
                                                }
                                            }
                                            else {
                                                mAlertDialog.tv_sexual_content_comment3.background = ContextCompat.getDrawable(context, R.drawable.selected_option_background)
                                                mAlertDialog.tv_sexual_content_comment3.tag = R.drawable.selected_option_background
                                                listOfReasonsReportted.add(mAlertDialog.tv_sexual_content_comment3.text.toString())
                                            }
                                        }
                                        mAlertDialog.tv_explicit_violence_comment.setOnClickListener {
                                            if(mAlertDialog.tv_explicit_violence_comment.tag == R.drawable.selected_option_background) {
                                                mAlertDialog.tv_explicit_violence_comment.background = ContextCompat.getDrawable(context, R.drawable.likes_post_count)
                                                mAlertDialog.tv_explicit_violence_comment.tag = R.drawable.likes_post_count

                                                if(listOfReasonsReportted.contains(mAlertDialog.tv_explicit_violence_comment.text.toString())) {
                                                    listOfReasonsReportted.remove(mAlertDialog.tv_explicit_violence_comment.text.toString())
                                                }
                                            }
                                            else {
                                                mAlertDialog.tv_explicit_violence_comment.background = ContextCompat.getDrawable(context, R.drawable.selected_option_background)
                                                mAlertDialog.tv_explicit_violence_comment.tag = R.drawable.selected_option_background
                                                listOfReasonsReportted.add(mAlertDialog.tv_explicit_violence_comment.text.toString())
                                            }
                                        }
                                        mAlertDialog.tv_is_fake_news_report_comment.setOnClickListener {
                                            if(mAlertDialog.tv_is_fake_news_report_comment.tag == R.drawable.selected_option_background) {
                                                mAlertDialog.tv_is_fake_news_report_comment.background = ContextCompat.getDrawable(context, R.drawable.likes_post_count)
                                                mAlertDialog.tv_is_fake_news_report_comment.tag = R.drawable.likes_post_count

                                                if(listOfReasonsReportted.contains(mAlertDialog.tv_is_fake_news_report_comment.text.toString())) {
                                                    listOfReasonsReportted.remove(mAlertDialog.tv_is_fake_news_report_comment.text.toString())
                                                }
                                            }
                                            else {
                                                mAlertDialog.tv_is_fake_news_report_comment.background = ContextCompat.getDrawable(context, R.drawable.selected_option_background)
                                                mAlertDialog.tv_is_fake_news_report_comment.tag = R.drawable.selected_option_background
                                                listOfReasonsReportted.add(mAlertDialog.tv_is_fake_news_report_comment.text.toString())
                                            }
                                        }
                                        mAlertDialog.tv_i_disliked_report_comment.setOnClickListener {
                                            if(mAlertDialog.tv_i_disliked_report_comment.tag == R.drawable.selected_option_background) {
                                                mAlertDialog.tv_i_disliked_report_comment.background = ContextCompat.getDrawable(context, R.drawable.likes_post_count)
                                                mAlertDialog.tv_i_disliked_report_comment.tag = R.drawable.likes_post_count

                                                if(listOfReasonsReportted.contains(mAlertDialog.tv_i_disliked_report_comment.text.toString())) {
                                                    listOfReasonsReportted.remove(mAlertDialog.tv_i_disliked_report_comment.text.toString())
                                                }
                                            }
                                            else {
                                                mAlertDialog.tv_i_disliked_report_comment.background = ContextCompat.getDrawable(context, R.drawable.selected_option_background)
                                                mAlertDialog.tv_i_disliked_report_comment.tag = R.drawable.selected_option_background
                                                listOfReasonsReportted.add(mAlertDialog.tv_i_disliked_report_comment.text.toString())

                                            }                                     }
                                        mAlertDialog.tv_other_report_comment.setOnClickListener {
                                            if(mAlertDialog.tv_other_report_comment.tag == R.drawable.selected_option_background) {
                                                mAlertDialog.tv_other_report_comment.background = ContextCompat.getDrawable(context, R.drawable.likes_post_count)
                                                mAlertDialog.tv_other_report_comment.tag = R.drawable.likes_post_count

                                                if(listOfReasonsReportted.contains(mAlertDialog.tv_other_report_comment.text.toString())) {
                                                    listOfReasonsReportted.remove(mAlertDialog.tv_other_report_comment.text.toString())
                                                }
                                            }
                                            else {
                                                mAlertDialog.tv_other_report_comment.background = ContextCompat.getDrawable(context, R.drawable.selected_option_background)
                                                mAlertDialog.tv_other_report_comment.tag = R.drawable.selected_option_background
                                                listOfReasonsReportted.add(mAlertDialog.tv_other_report_comment.text.toString())

                                            }
                                        }
                                        mAlertDialog.tv_violate_rules_rgeport_comment2.setOnClickListener {
                                            if(mAlertDialog.tv_violate_rules_rgeport_comment2.tag == R.drawable.selected_option_background) {
                                                mAlertDialog.tv_violate_rules_rgeport_comment2.background = ContextCompat.getDrawable(context, R.drawable.likes_post_count)
                                                mAlertDialog.tv_violate_rules_rgeport_comment2.tag = R.drawable.likes_post_count

                                                if(listOfReasonsReportted.contains(mAlertDialog.tv_violate_rules_rgeport_comment2.text.toString())) {
                                                    listOfReasonsReportted.remove(mAlertDialog.tv_violate_rules_rgeport_comment2.text.toString())
                                                }
                                            }
                                            else {
                                                mAlertDialog.tv_violate_rules_rgeport_comment2.background = ContextCompat.getDrawable(context, R.drawable.selected_option_background)
                                                mAlertDialog.tv_violate_rules_rgeport_comment2.tag = R.drawable.selected_option_background
                                                listOfReasonsReportted.add(mAlertDialog.tv_violate_rules_rgeport_comment2.text.toString())

                                            }
                                        }

                                        mAlertDialog.bt_report_comment.setOnClickListener {
                                            if(listOfReasonsReportted.size == 0 && mAlertDialog.et_input_description_report_comment.text.isNullOrEmpty()) {
                                                Toasty.error(context, "Escolha um dos motivos para realizar a denuncia do comentário, ou descreva a sua denuncia").show()
                                            }
                                            else {
                                                var reportComment = ReportComment()

                                                reportComment.idComentario = comentario.idComentario
                                                reportComment.idOwnerComment = comentario.idOwner
                                                reportComment.idPetComment = comentario.idPet
                                                reportComment.idPost = post.idPost

                                                reportComment.idOwnerReportter = auth.uid.toString()
                                                reportComment.idPetReportter = myPreferences.getPetLogged().toString()
                                                reportComment.dateTimeReport = DateUtils.dataFormatWithMilliseconds()
                                                reportComment.listOfReasonsReportted = listOfReasonsReportted
                                                reportComment.descriptionOfReport = mAlertDialog.et_input_description_report_comment.text.toString()

                                                dt.child(AnimalLoversConstants.DATABASE_ENTITY_ADMIN.nome).addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        if(snapshot.hasChildren() && snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_REPORT_COMMENT.nome)) {
                                                            val numReporttedComments = snapshot.child(AnimalLoversConstants.DATABASE_NODE_REPORT_COMMENT.nome).childrenCount + 1

                                                            database.child(AnimalLoversConstants.DATABASE_ENTITY_ADMIN.nome)
                                                                .child(AnimalLoversConstants.DATABASE_NODE_REPORT_COMMENT.nome)
                                                                .child(numReporttedComments.toString())
                                                                .setValue(reportComment)
                                                        }
                                                        else {
                                                            database.child(AnimalLoversConstants.DATABASE_ENTITY_ADMIN.nome)
                                                                .child(AnimalLoversConstants.DATABASE_NODE_REPORT_COMMENT.nome)
                                                                .child("1")
                                                                .setValue(reportComment)
                                                        }
                                                        mAlertDialog.hide()
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {
                                                        println(error.toString())
                                                    }

                                                })


                                            }
                                        }

                                        return@setOnMenuItemClickListener true
                                    }

                                }
                                return@setOnMenuItemClickListener false
                            }


                            popupMenu.inflate(R.menu.menu_options)
                            popupMenu.show()
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
        var optionsOpenIcon = itemView.iv_icon_options_modal_comment
        var layoutItem = itemView.lin_layout_comment_item
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