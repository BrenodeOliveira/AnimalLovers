package br.com.breno.animallovers.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Comentario
import br.com.breno.animallovers.model.LikeComment
import br.com.breno.animallovers.model.Notification
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.model.ReportComment
import br.com.breno.animallovers.service.CommentsService
import br.com.breno.animallovers.service.LikeService
import br.com.breno.animallovers.service.NotificationService
import br.com.breno.animallovers.service.PetService
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

class CommentsPostAdapter(
    private val comments: List<Comentario>,
    private val context: Context,
    private val post: Post
) : RecyclerView.Adapter<CommentsPostAdapter.ViewHolder>() {
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var dt: DatabaseReference
    var listOfReasonsReportted = ArrayList<String>()
    private var petService = PetService(context)
    private var dateUtils = DateUtils()
    private var likeService = LikeService(context)
    private var commentService = CommentsService(context)
    private var notificationService = NotificationService(context)

    var hasPetLikedComment = false
    var numLikes = 0
    val myPreferences = ProjectPreferences(context)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comments_post_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comentario = comments[position]

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        dt = Firebase.database.reference
        var pet: Pet
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .addValueEventListener(
                object : ValueEventListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onDataChange(snapshot: DataSnapshot) {
                        pet = petService.retrievePetInfoFromUnknownOwner(comentario.idOwner, comentario.idPet, snapshot)

                        holder.let {
                            it.nome.text = pet.nome
                            it.textComment.text = comentario.textoComentario
                            it.dateComment.text = dateUtils.dateDiffInTextFormat(LocalDateTime.parse(comentario.dataHora, DateTimeFormatter.ofPattern(DateUtils.dateFrmt())))

                            it.nome.setOnClickListener {
                                val intent = Intent(context, ProfilePetActivity::class.java)
                                intent.putExtra("PET_INFO_PROFILE", pet)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                                ContextCompat.startActivity(context, intent, Bundle())
                            }
                            it.photo.setOnClickListener {
                                val intent = Intent(context, ProfilePetActivity::class.java)
                                intent.putExtra("PET_INFO_PROFILE", pet)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                                ContextCompat.startActivity(context, intent, Bundle())
                            }
                        }

                        numLikes = commentService.getNumOfLikesInComment(post, comentario, snapshot)
                        holder.numLikesComment.text = numLikes.toString()

                        hasPetLikedComment = commentService.checkIfUserLikedComment(post, comentario, snapshot)
                        var ownersPets = commentService.getOwnersPetsComments(post, comentario, snapshot)

                        if (hasPetLikedComment) {
                            holder.likeComment.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY)
                        }
                        else {
                            holder.likeComment.setColorFilter(ContextCompat.getColor(context, R.color.icon_tint), android.graphics.PorterDuff.Mode.MULTIPLY)
                        }

                        loadProfilePhoto(pet, holder)

                        likeCommentClick(holder, comentario, snapshot, post)

                        numLikesCommentClick(holder, comentario, snapshot, ownersPets)

                        openMenusComment(holder, comentario)

                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.toString())
                    }

                })
    }

    private fun openMenusComment(holder : ViewHolder, comentario : Comentario) {
        holder.optionsOpenIcon.setOnClickListener {
            if (comentario.idOwner == auth.uid && comentario.idPet == myPreferences.getPetLogged()) {
                val popupMenu = PopupMenu(context, holder.itemView, Gravity.LEFT)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {R.id.item_copy_menu_options_owner -> {
                        val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip: ClipData = ClipData.newPlainText("label", comentario.textoComentario)
                        clipboard.setPrimaryClip(clip)
                        Toasty.info(context, context.getString(R.string.text_copied_to_clipboard)).show()
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

                                    commentService.updateOrDeleteComment(post, comentario)
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

                                    commentService.updateOrDeleteComment(post, comentario)
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
                val popupMenu = PopupMenu(context, holder.itemView, Gravity.LEFT)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.item_copy_menu_options -> {
                            val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip: ClipData = ClipData.newPlainText("label", comentario.textoComentario)
                            clipboard.setPrimaryClip(clip)
                            Toasty.info(context, context.getString(R.string.text_copied_to_clipboard)).show()
                            return@setOnMenuItemClickListener true
                        }

                        R.id.item_report_menu_options -> {

                            val mDialogView = LayoutInflater.from(context).inflate(R.layout.report_comment, null)
                            val mBuilder = AlertDialog.Builder(context).setView(mDialogView)
                            val mAlertDialog = mBuilder.show()

                            mAlertDialog.tv_is_offensive_report_comment2.setOnClickListener {
                                chooseItemReport(mAlertDialog.tv_is_offensive_report_comment2)
                            }
                            mAlertDialog.tv_sexual_content_comment3.setOnClickListener {
                                chooseItemReport(mAlertDialog.tv_sexual_content_comment3)
                            }
                            mAlertDialog.tv_explicit_violence_comment.setOnClickListener {
                                chooseItemReport(mAlertDialog.tv_explicit_violence_comment)
                            }
                            mAlertDialog.tv_is_fake_news_report_comment.setOnClickListener {
                                chooseItemReport(mAlertDialog.tv_is_fake_news_report_comment)
                            }
                            mAlertDialog.tv_i_disliked_report_comment.setOnClickListener {
                                chooseItemReport(mAlertDialog.tv_i_disliked_report_comment)
                            }
                            mAlertDialog.tv_other_report_comment.setOnClickListener {
                                chooseItemReport(mAlertDialog.tv_other_report_comment)
                            }
                            mAlertDialog.tv_violate_rules_rgeport_comment2.setOnClickListener {
                                chooseItemReport(mAlertDialog.tv_violate_rules_rgeport_comment2)
                            }

                            mAlertDialog.bt_report_comment.setOnClickListener {
                                if(listOfReasonsReportted.size == 0 && mAlertDialog.et_input_description_report_comment.text.isNullOrEmpty()) {
                                    Toasty.error(context, context.getString(R.string.choose_a_reason_to_report_comment)).show()
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
                                            commentService.reportComment(snapshot, reportComment)
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

    private fun numLikesCommentClick(holder: ViewHolder, comentario: Comentario, snapshot: DataSnapshot, ownersPets : HashMap<String, HashMap<String, String>>) {
        holder.numLikesComment.setOnClickListener {
            val petsArray = petService.retrievePets(ownersPets, snapshot)

            if (holder.numLikesComment.text != "0") {

                val view = LayoutInflater.from(context).inflate(R.layout.modal_likes_pets_post, it.parent as ViewGroup)

                val recyclerView = view.findViewById(R.id.recycler_pets_likes_post) as? RecyclerView

                recyclerView!!.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = ProfilesLikesPostAdapter(petsArray, context)
                val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                recyclerView.layoutManager = layoutManager
            }
        }
    }

    private fun likeCommentClick(holder : ViewHolder, comentario : Comentario, snapshot: DataSnapshot, post : Post) {
        val likeComment = likeService.checkIfUserLikedComment(snapshot, post, comentario)
        val notificationModel = notificationService.getNotificationModelOfLikeInComment(snapshot, comentario, likeComment)

        holder.likeComment.setOnClickListener {
            hasPetLikedComment = if (hasPetLikedComment) {
                likeService.dislikeComment(post, comentario, likeComment, notificationModel)

                holder.likeComment.setColorFilter(ContextCompat.getColor(context, R.color.icon_tint), android.graphics.PorterDuff.Mode.MULTIPLY)
                numLikes--
                false
            } else {
                val likeInComment = likeService.likeComment(post, comentario, likeComment)
                holder.likeComment.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY)
                numLikes++

                if(comentario.idOwner != auth.uid) {
                    notificationService.sendNotificationOfLikedComment(post, comentario, snapshot, likeInComment, notificationModel)
                }

                true
            }
            holder.numLikesComment.text = numLikes.toString()
        }
    }

    private fun loadProfilePhoto(pet : Pet, holder : ViewHolder) {
        if (pet.pathFotoPerfil != "") {
            val storageRef = storage.reference
                .child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                .child(pet.idOwner)
                .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                holder.let {
                    it.photo.setImageBitmap(bmp)

                }
            }.addOnFailureListener {

            }
        }
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

    private fun chooseItemReport(tv : TextView) {
        if(tv.tag == R.drawable.selected_option_background) {
            tv.background = ContextCompat.getDrawable(context, R.drawable.likes_post_count)
            tv.tag = R.drawable.likes_post_count

            if(listOfReasonsReportted.contains(tv.text.toString())) {
                listOfReasonsReportted.remove(tv.text.toString())
            }
        }
        else {
            tv.background = ContextCompat.getDrawable(context, R.drawable.selected_option_background)
            tv.tag = R.drawable.selected_option_background
            listOfReasonsReportted.add(tv.text.toString())
        }
    }
}