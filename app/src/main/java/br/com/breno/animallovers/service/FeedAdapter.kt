package br.com.breno.animallovers.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.*
import br.com.breno.animallovers.ui.activity.ProfilePetActivity
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
import kotlinx.android.synthetic.main.edit_post.*
import kotlinx.android.synthetic.main.feed_item.view.*
import kotlinx.android.synthetic.main.report_comment.et_input_description_report_comment
import kotlinx.android.synthetic.main.report_comment.tv_explicit_violence_comment
import kotlinx.android.synthetic.main.report_comment.tv_i_disliked_report_comment
import kotlinx.android.synthetic.main.report_comment.tv_is_fake_news_report_comment
import kotlinx.android.synthetic.main.report_comment.tv_is_offensive_report_comment2
import kotlinx.android.synthetic.main.report_comment.tv_other_report_comment
import kotlinx.android.synthetic.main.report_comment.tv_sexual_content_comment3
import kotlinx.android.synthetic.main.report_comment.tv_violate_rules_rgeport_comment2
import kotlinx.android.synthetic.main.report_post.*


class FeedAdapter(
    private val posts: List<Post>,
    private val context: Context
) : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var db: DatabaseReference
    private lateinit var dt: DatabaseReference
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
                val layoutParams: ViewGroup.LayoutParams = it.photoPost.layoutParams
                layoutParams.height = 50
                it.photoPost.layoutParams = layoutParams
            }
        }
        database = Firebase.database.reference
        db = Firebase.database.reference
        dt = Firebase.database.reference

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
                            AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome
                        ).value as HashMap<String, HashMap<String, String>>
                        for (i in 0 until ownersPets.size) {
                            val idOwnerPet = ownersPets.keys.toMutableList()[i]
                            for (j in 0 until (ownersPets.toMutableMap()[idOwnerPet]?.values?.size!!)) {
                                numLikes++
                                holder.numLikesPost.text = numLikes.toString()
                            }
                        }

                        if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)
                                .hasChild(
                                    auth.uid.toString()
                                )
                        ) {
                            println(
                                snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)
                                    .child(
                                        auth.uid.toString()
                                    )
                            )

                            if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)
                                    .child(
                                        auth.uid.toString()
                                    ).hasChild(myPreferences.getPetLogged().toString())
                            ) {
                                holder.likePost.setColorFilter(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.colorAccent
                                    ), android.graphics.PorterDuff.Mode.MULTIPLY
                                )
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
                                holder.likePost.setColorFilter(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.icon_tint
                                    ), android.graphics.PorterDuff.Mode.MULTIPLY
                                )
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
                                holder.likePost.setColorFilter(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.colorAccent
                                    ), android.graphics.PorterDuff.Mode.MULTIPLY
                                )
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
                        var manager: FragmentManager = if (context is ProfilePetActivity) {
                            context.supportFragmentManager
                        } else {
                            (context as AppCompatActivity).supportFragmentManager
                        }
                        profilesLikesPostAdapter.show(manager, "likesPost")
                    }

                    holder.commentPost.setOnClickListener {
                        val profilesLikesPostAdapter = CommentsPostService(post, arraylist)
                        val manager: FragmentManager =
                            (context as AppCompatActivity).supportFragmentManager
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
                            var rootNodeComment = dSnapshot.child(i.toString()).child(
                                AnimalLoversConstants.DATABASE_NODE_COMMENT.nome
                            ).value as HashMap<String, HashMap<String, HashMap<String, String>>>
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

        holder.optionsOpenIcon.setOnClickListener {

            if (post.idOwner == auth.uid && post.idPet == myPreferences.getPetLogged()) {
                val popupMenu = PopupMenu(context, holder.itemView)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.item_copy_menu_options_owner -> {
                            val clipboard: ClipboardManager = context.getSystemService(
                                Context.CLIPBOARD_SERVICE
                            ) as ClipboardManager
                            val clip: ClipData = ClipData.newPlainText(
                                "label",
                                post.legenda
                            )
                            clipboard.setPrimaryClip(clip)
                            Toasty.info(
                                context,
                                "Legenda copiada para a área de transferência"
                            ).show()
                            return@setOnMenuItemClickListener true
                        }

                        R.id.item_edit_menu_options -> {
                            val mDialogView = LayoutInflater.from(context).inflate(R.layout.edit_post, null)
                            val mBuilder = AlertDialog.Builder(context).setView(mDialogView)
                            val mAlertDialog = mBuilder.show()

                            mAlertDialog.tv_post_text_edit_post.text = post.legenda
                            mAlertDialog.et_input_comment_edit_post.setText(post.legenda)

                            mAlertDialog.bt_cancel_edit_post.setOnClickListener {
                                mAlertDialog.dismiss()
                            }

                            mAlertDialog.bt_update_edit_post.setOnClickListener {
                                if (mAlertDialog.tv_post_text_edit_post.text != mAlertDialog.et_input_comment_edit_post.text) {
                                    post.legenda = mAlertDialog.et_input_comment_edit_post.text.toString()

                                    database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                                        .child(auth.uid.toString())
                                        .child(myPreferences.getPetLogged().toString())
                                        .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                                        .child(post.idPost)
                                        .setValue(post)
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

                                    post.postAtivo = false

                                    database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                                        .child(auth.uid.toString())
                                        .child(myPreferences.getPetLogged().toString())
                                        .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                                        .child(post.idPost)
                                        .setValue(post)
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
                                post.legenda
                            )
                            clipboard.setPrimaryClip(clip)
                            Toasty.info(
                                context,
                                "Legenda copiada para a área de transferência"
                            ).show()
                            return@setOnMenuItemClickListener true

                        }

                        R.id.item_report_menu_options -> {
                            var listOfReasonsReportted = ArrayList<String>()

                            val mDialogView = LayoutInflater.from(context).inflate(R.layout.report_post, null)
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




                            mAlertDialog.tv_spam_report_post.setOnClickListener {
                                if(mAlertDialog.tv_spam_report_post.tag == R.drawable.selected_option_background) {
                                    mAlertDialog.tv_spam_report_post.background = ContextCompat.getDrawable(context, R.drawable.likes_post_count)
                                    mAlertDialog.tv_spam_report_post.tag = R.drawable.likes_post_count

                                    if(listOfReasonsReportted.contains(mAlertDialog.tv_spam_report_post.text.toString())) {
                                        listOfReasonsReportted.remove(mAlertDialog.tv_spam_report_post.text.toString())
                                    }
                                }
                                else {
                                    mAlertDialog.tv_spam_report_post.background = ContextCompat.getDrawable(context, R.drawable.selected_option_background)
                                    mAlertDialog.tv_spam_report_post.tag = R.drawable.selected_option_background
                                    listOfReasonsReportted.add(mAlertDialog.tv_spam_report_post.text.toString())

                                }
                            }
                            mAlertDialog.tv_wrong_photo_report_post.setOnClickListener {
                                if(mAlertDialog.tv_wrong_photo_report_post.tag == R.drawable.selected_option_background) {
                                    mAlertDialog.tv_wrong_photo_report_post.background = ContextCompat.getDrawable(context, R.drawable.likes_post_count)
                                    mAlertDialog.tv_wrong_photo_report_post.tag = R.drawable.likes_post_count

                                    if(listOfReasonsReportted.contains(mAlertDialog.tv_wrong_photo_report_post.text.toString())) {
                                        listOfReasonsReportted.remove(mAlertDialog.tv_wrong_photo_report_post.text.toString())
                                    }
                                }
                                else {
                                    mAlertDialog.tv_wrong_photo_report_post.background = ContextCompat.getDrawable(context, R.drawable.selected_option_background)
                                    mAlertDialog.tv_wrong_photo_report_post.tag = R.drawable.selected_option_background
                                    listOfReasonsReportted.add(mAlertDialog.tv_wrong_photo_report_post.text.toString())

                                }
                            }
                            mAlertDialog.tv_criminal_content_report_post.setOnClickListener {
                                if(mAlertDialog.tv_criminal_content_report_post.tag == R.drawable.selected_option_background) {
                                    mAlertDialog.tv_criminal_content_report_post.background = ContextCompat.getDrawable(context, R.drawable.likes_post_count)
                                    mAlertDialog.tv_criminal_content_report_post.tag = R.drawable.likes_post_count

                                    if(listOfReasonsReportted.contains(mAlertDialog.tv_criminal_content_report_post.text.toString())) {
                                        listOfReasonsReportted.remove(mAlertDialog.tv_criminal_content_report_post.text.toString())
                                    }
                                }
                                else {
                                    mAlertDialog.tv_criminal_content_report_post.background = ContextCompat.getDrawable(context, R.drawable.selected_option_background)
                                    mAlertDialog.tv_criminal_content_report_post.tag = R.drawable.selected_option_background
                                    listOfReasonsReportted.add(mAlertDialog.tv_criminal_content_report_post.text.toString())

                                }
                            }

                            mAlertDialog.bt_report_post.setOnClickListener {
                                if(listOfReasonsReportted.size == 0 && mAlertDialog.et_input_description_report_comment.text.isNullOrEmpty()) {
                                    Toasty.error(context, "Escolha um dos motivos para realizar a denuncia da publicação, ou descreva a sua denuncia").show()
                                }
                                else {
                                    var reportPost = ReportPost()

                                    reportPost.idComentario = comentario.idComentario
                                    reportPost.idOwnerPetPost = comentario.idOwner
                                    reportPost.idPetPost = comentario.idPet
                                    reportPost.idPost = post.idPost

                                    reportPost.idOwnerReportter = auth.uid.toString()
                                    reportPost.idPetReportter = myPreferences.getPetLogged().toString()
                                    reportPost.dateTimeReport = DateUtils.dataFormatWithMilliseconds()
                                    reportPost.listOfReasonsReportted = listOfReasonsReportted
                                    reportPost.descriptionOfReport = mAlertDialog.et_input_description_report_comment.text.toString()

                                    dt.child(AnimalLoversConstants.DATABASE_ENTITY_ADMIN.nome).addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if(snapshot.hasChildren() && snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_REPORT_POST.nome)) {
                                                val numReporttedPosts = snapshot.child(AnimalLoversConstants.DATABASE_NODE_REPORT_POST.nome).childrenCount + 1

                                                database.child(AnimalLoversConstants.DATABASE_ENTITY_ADMIN.nome)
                                                    .child(AnimalLoversConstants.DATABASE_NODE_REPORT_POST.nome)
                                                    .child(numReporttedPosts.toString())
                                                    .setValue(reportPost)
                                            }
                                            else {
                                                database.child(AnimalLoversConstants.DATABASE_ENTITY_ADMIN.nome)
                                                    .child(AnimalLoversConstants.DATABASE_NODE_REPORT_POST.nome)
                                                    .child("1")
                                                    .setValue(reportPost)
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
        var optionsOpenIcon = itemView.iv_icon_options_post_feed

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

