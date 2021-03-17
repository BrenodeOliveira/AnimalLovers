package br.com.breno.animallovers.service

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.ui.activity.ProfilePetActivity
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.profiles_like_post_item.view.*

class ProfilesLikesPostAdapter(private val pets: List<Pet>, private val context: Context): RecyclerView.Adapter<ProfilesLikesPostAdapter.ViewHolder>() {

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.profiles_like_post_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pet = pets[position]

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        if (pet.pathFotoPerfil != "") {
            var storageRef = storage.reference
                .child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                .child(pet.idOwner)
                .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                holder?.let {
                    it.title.text = pet.nome
                    it.description.text = pet.resumo
                    it.photo.setImageBitmap(bmp)
                }
            }.addOnFailureListener {

            }
        }
        else {
            holder?.let {
                it.title.text = pet.nome
                it.description.text = pet.resumo
            }
        }

        holder.let {
            it.photo.setOnClickListener {
                val intent = Intent(context, ProfilePetActivity::class.java)
                intent.putExtra("PET_INFO_PROFILE", pet)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                ContextCompat.startActivity(context, intent, Bundle())
            }
            it.description.setOnClickListener {
                val intent = Intent(context, ProfilePetActivity::class.java)
                intent.putExtra("PET_INFO_PROFILE", pet)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                ContextCompat.startActivity(context, intent, Bundle())
            }
            it.title.setOnClickListener {
                val intent = Intent(context, ProfilePetActivity::class.java)
                intent.putExtra("PET_INFO_PROFILE", pet)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                ContextCompat.startActivity(context, intent, Bundle())
            }
        }
    }

    override fun getItemCount(): Int {
        return pets.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.tv_pet_name_likes_modal
        val description = itemView.tv_pet_description_likes_modal
        var photo = itemView.iv_icon_foto_perfil_pet_likes_modal

        fun bindView(pet: Pet) {
            val title = itemView.tv_pet_name_likes_modal
            val description = itemView.tv_pet_description_likes_modal

            title.text = pet.nome
            description.text = pet.resumo
        }
    }
}