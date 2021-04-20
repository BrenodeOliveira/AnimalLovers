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
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import br.com.breno.animallovers.R
import br.com.breno.animallovers.constants.KindOfPet
import br.com.breno.animallovers.model.*
import br.com.breno.animallovers.service.*
import br.com.breno.animallovers.ui.activity.ProfilePetActivity
import br.com.breno.animallovers.ui.activity.SinglePostActivity
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
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
import kotlinx.android.synthetic.main.ad_feed_item.view.*
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
import java.lang.IllegalStateException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class FeedAdapter(private val posts: List<Post>, private val context: Context, private val shouldInflateComments : Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private lateinit var database: DatabaseReference
    private lateinit var dt: DatabaseReference

    private var comentario = Comentario()
    private var postService = PostService(context)
    private var likeService = LikeService(context)
    private var notificationService = NotificationService(context)

    private var dateUtils = DateUtils()

    private val myPreferences = ProjectPreferences(context)
    private var listOfReasonsReportted = ArrayList<String>()

    companion object {
        const val VIEW_TYPE_ZERO_NORMAL_POST = 0
        const val VIEW_TYPE_ONE_AD_POST = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if(viewType == VIEW_TYPE_ZERO_NORMAL_POST) {
            ViewPostViewHolder(LayoutInflater.from(context).inflate(R.layout.feed_item, parent, false))
        } else {
            ViewAdPostViewHolder(LayoutInflater.from(context).inflate(R.layout.ad_feed_item, parent, false))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val post = posts[(posts.size - 1) - position]

        if(post.postType === VIEW_TYPE_ZERO_NORMAL_POST) {
            holder.setIsRecyclable(false)
            (holder as ViewPostViewHolder).bind(position)
        } else {
            (holder as ViewAdPostViewHolder).bind()
        }
    }

    private inner class ViewPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.tv_pet_name_post_feed
        val dateTime: TextView = itemView.tv_date_time_post_feed
        var description: TextView = itemView.tv_pet_description_post_feed
        var photoPost: ImageView = itemView.iv_photo_post_feed
        var photoPostCard: CardView = itemView.card_iv_photo_post_feed
        var likePost: ImageView = itemView.iv_action_fav
        var commentPost: ImageView = itemView.iv_action_comment
        var numLikesPost: TextView = itemView.tv_num_likes_post
        var numCommentsPost: TextView = itemView.tv_num_comments_post
        var optionsOpenIcon = itemView.iv_icon_options_post_feed
        var photoProfile : ImageView = itemView.iv_icon_profile_photo_post_feed

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(position: Int) {
            var post = posts[(posts.size - 1) - position]

            photoProfile.setImageResource(R.drawable.ic_unkown_pet)

            storage = FirebaseStorage.getInstance()
            auth = FirebaseAuth.getInstance()
            var pet = Pet()

            var arraylist = ArrayList<Comentario>()

            if (post.pathPub != "") {
                loadPostPic(post)
            }
            else {
                    name.text = post.nomePet
                    dateTime.text = dateUtils.dateDiffInTextFormat(LocalDateTime.parse(post.dataHora, DateTimeFormatter.ofPattern(DateUtils.dateFrmt())))
                    description.text = post.legenda
                    val layoutParams: ViewGroup.LayoutParams = photoPost.layoutParams
                    layoutParams.height = 15
                    photoPost.layoutParams = layoutParams

                    val layoutParamsCard: ViewGroup.LayoutParams = photoPostCard.layoutParams
                    layoutParamsCard.height = 15
                    photoPostCard.layoutParams = layoutParamsCard
            }

            loadProfilePic(post)

            database = Firebase.database.reference
            dt = Firebase.database.reference

            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)

                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dSnap: DataSnapshot) {

                        if(FirebaseAuth.getInstance().currentUser == null || myPreferences.getPetLogged().equals("")) {
                            database.removeEventListener(this)
                            return
                        }

                        var numLikes = 0

                        var hasPetLikedPost = false

                        val owner = dSnap.child(post.idOwner).child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome).getValue<Conta>()!!

                        pet = (dSnap.child(post.idOwner).child(post.idPet).child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome).getValue<Pet>())!!

                        if(pet.pathFotoPerfil == "") {
                            when (pet.tipo) {
                                KindOfPet.DOG.tipo -> {
                                    photoProfile.setImageResource(R.drawable.ic_dog_pet)
                                }
                                KindOfPet.CAT.tipo -> {
                                    photoProfile.setImageResource(R.drawable.ic_cat_pet)
                                }
                                KindOfPet.BIRD.tipo -> {
                                    photoProfile.setImageResource(R.drawable.ic_bird_pet)
                                }
                                else -> {
                                    photoProfile.setImageResource(R.drawable.ic_unkown_pet)
                                }
                            }
                        }

                        val petLoggedInfo = (dSnap.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome).getValue<Pet>())!!

                        val snapshot = dSnap.child(post.idOwner).child(post.idPet).child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                            .child(post.idPost)
                        if (snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)) {
                            val ownersPets: HashMap<String, HashMap<String, String>> = snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome).value as HashMap<String, HashMap<String, String>>
                            for (i in 0 until ownersPets.size) {
                                val idOwnerPet = ownersPets.keys.toMutableList()[i]
                                for (j in 0 until (ownersPets.toMutableMap()[idOwnerPet]?.values?.size!!)) {
                                    numLikes++
                                    numLikesPost.text = numLikes.toString()
                                }
                            }

                            if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome).hasChild(auth.uid.toString())) {
                                if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome).child(auth.uid.toString()).hasChild(myPreferences.getPetLogged().toString())) {
                                    likePost.setColorFilter(ContextCompat.getColor(context, R.color.colorLiked), android.graphics.PorterDuff.Mode.MULTIPLY)
                                    hasPetLikedPost = true
                                }
                            }
                        }
                        arraylist.clear()
                        arraylist = postService.getCommentsOfPost(snapshot, post)
                        numCommentsPost.text = arraylist.size.toString()

                        likePost.setOnClickListener {
                            val likePostModel = likeService.checkIfUserLikedPost(snapshot)
                            val notificationModel = notificationService.getNotificationModelOfLikeInPost(dSnap, post, likePostModel)

                            if (numLikesPost.text != "") {

                                hasPetLikedPost = if (hasPetLikedPost) {
                                    likeService.dislikePost(post, likePostModel, notificationModel)

                                    likePost.setColorFilter(ContextCompat.getColor(context, R.color.icon_tint), android.graphics.PorterDuff.Mode.MULTIPLY)
                                    numLikes--
                                    false
                                } else {
                                    val likeInPost = likeService.likePost(post, likePostModel)
                                    likePost.setColorFilter(ContextCompat.getColor(context, R.color.colorLiked), android.graphics.PorterDuff.Mode.MULTIPLY)
                                    numLikes++
                                    if(post.idOwner != auth.uid) {
                                        notificationService.sendNotificationOfLikedPost(pet, petLoggedInfo, post, owner, notificationModel, likeInPost, dSnap)
                                    }
                                    true
                                }
                                numLikesPost.text = numLikes.toString()
                            } else {
                                numLikesPost.text = "0"
                            }
                        }

                        numLikesPost.setOnClickListener {
                            val profilesLikesPostAdapter = ProfilesLikesPostService(post)
                            var manager: FragmentManager = when (context) {
                                is ProfilePetActivity -> {
                                    context.supportFragmentManager
                                }
                                is SinglePostActivity -> {
                                    context.supportFragmentManager
                                }
                                else -> {
                                    (context as AppCompatActivity).supportFragmentManager
                                }
                            }

                            if(numLikes != 0) {
                                profilesLikesPostAdapter.show(manager, "likesPost")
                            }
                        }

                        val profilesLikesPostAdapter = CommentsPostService(post, arraylist, context)
                        var manager: FragmentManager = when (context) {
                            is ProfilePetActivity -> {
                                context.supportFragmentManager
                            }
                            is SinglePostActivity -> {
                                context.supportFragmentManager
                            }
                            else -> {
                                (context as AppCompatActivity).supportFragmentManager
                            }
                        }

                        try {
                            if(shouldInflateComments) {
                                profilesLikesPostAdapter.show(manager, "likesPost")
                            }

                            commentPost.setOnClickListener {
                                profilesLikesPostAdapter.show(manager, "likesPost")
                            }
                        }
                        catch (ilEx : IllegalStateException) {
                            println(ilEx.toString())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.toString())
                    }

                })

            photoProfile.setOnClickListener {
                val intent = Intent(context, ProfilePetActivity::class.java)
                intent.putExtra("PET_INFO_PROFILE", pet)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                ContextCompat.startActivity(context, intent, Bundle())
            }

            name.setOnClickListener {
                val intent = Intent(context, ProfilePetActivity::class.java)
                intent.putExtra("PET_INFO_PROFILE", pet)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                ContextCompat.startActivity(context, intent, Bundle())
            }

            optionsOpenIcon.setOnClickListener {

                if (post.idOwner == auth.uid && post.idPet == myPreferences.getPetLogged()) {
                    showPopUpMenuForOwnerPost(itemView, post)
                } else {
                    showPopUpMenuForOtherPosts(post)
                }

            }
        }

        private fun loadProfilePic(post: Post) {
            val ref = storage.reference
                .child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                .child(post.idOwner)
                .child(post.idPet + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)
            try {
                ref.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                    val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                        photoProfile.setImageBitmap(bmp)

                        if(photoProfile.drawable == null) {
                            photoProfile.visibility = View.INVISIBLE
                        }

                }.addOnFailureListener { itException ->
                    println(itException.toString())
                }
            } catch (ex: Exception) {
                println("Erro ao buscar foto de perfil: $ex")
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun loadPostPic(post: Post) {
            val storageRef = storage.reference.child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                .child(post.idOwner)
                .child(post.idPet)
                .child(post.dataHora)
            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                    name.text = post.nomePet
                    dateTime.text = dateUtils.dateDiffInTextFormat(LocalDateTime.parse(post.dataHora, DateTimeFormatter.ofPattern(DateUtils.dateFrmt())))
                    description.text = post.legenda
                    photoPost.setImageBitmap(bmp)

                    if(photoPost.drawable == null) {
                        photoPost.visibility = View.INVISIBLE
                        photoPostCard.visibility = View.INVISIBLE
                    }

            }.addOnFailureListener {
                println(it.toString())
            }
        }

        private fun showPopUpMenuForOtherPosts(post: Post) {
            val popupMenu = PopupMenu(context, itemView, Gravity.RIGHT)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.item_copy_menu_options -> {
                        val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip: ClipData = ClipData.newPlainText("label", post.legenda)
                        clipboard.setPrimaryClip(clip)
                        Toasty.info(context, "Legenda copiada para a área de transferência").show()
                        return@setOnMenuItemClickListener true

                    }

                    R.id.item_report_menu_options -> {
                        val mDialogView = LayoutInflater.from(context).inflate(R.layout.report_post, null)
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
                            chooseItemReport(mAlertDialog.tv_i_disliked_report_comment)                                   }
                        mAlertDialog.tv_other_report_comment.setOnClickListener {
                            chooseItemReport(mAlertDialog.tv_other_report_comment)
                        }
                        mAlertDialog.tv_violate_rules_rgeport_comment2.setOnClickListener {
                            chooseItemReport(mAlertDialog.tv_violate_rules_rgeport_comment2)
                        }
                        mAlertDialog.tv_spam_report_post.setOnClickListener {
                            chooseItemReport(mAlertDialog.tv_spam_report_post)
                        }
                        mAlertDialog.tv_wrong_photo_report_post.setOnClickListener {
                            chooseItemReport(mAlertDialog.tv_wrong_photo_report_post)
                        }
                        mAlertDialog.tv_criminal_content_report_post.setOnClickListener {
                            chooseItemReport(mAlertDialog.tv_criminal_content_report_post)
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
                                        postService.reportPost(snapshot, reportPost)
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

    private inner class ViewAdPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            MobileAds.initialize(context) {}

            val mAdview : AdView = itemView.adView

            val adRequest = AdRequest.Builder().build()
            mAdview.loadAd(adRequest)
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun getItemViewType(position: Int): Int {
        return posts[(posts.size - 1) - position].postType
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun showPopUpMenuForOwnerPost(itemView: View, post : Post) {
        val popupMenu = PopupMenu(context, itemView, Gravity.RIGHT)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.item_copy_menu_options_owner -> {
                    val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData = ClipData.newPlainText("label", post.legenda)
                    clipboard.setPrimaryClip(clip)
                    Toasty.info(context, "Legenda copiada para a área de transferência").show()
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

                            postService.updatePost(post)

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

                            postService.updatePost(post)

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

