package br.com.breno.animallovers.service

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.pet_search_item.view.*

class PetSearchAdapter(
    private val pets : List<Pet>,
    private val context: Context
) : RecyclerView.Adapter<PetSearchAdapter.ViewHolder>() {
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetSearchAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.pet_search_item, parent, false)
        return PetSearchAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetSearchAdapter.ViewHolder, position: Int) {
        val pet = pets[(pets.size -1) - position]
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        if (pet.pathFotoPerfil != "") {
            var storageRef = storage.reference.child(AnimalLoversConstants.STORAGE_ROOT.nome).child(
                AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome).child(pet.idOwner).child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)
            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                holder.let {
                    it.name.text = pet.nome
                    it.photoProfile.setImageBitmap(bmp)

                    if(it.photoProfile.drawable == null) {
                        it.photoProfile.visibility = View.INVISIBLE
                    }
                }
            }.addOnFailureListener {

            }
        }
        else {
            holder.let {
                it.name.text = pet.nome
                val layoutParams: ViewGroup.LayoutParams = it.photoProfile.getLayoutParams()
                layoutParams.height = 50
                it.photoProfile.setLayoutParams(layoutParams)
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
        val name: TextView = itemView.tv_pet_name_search_pets
        var photoProfile: ImageView = itemView.iv_icon_foto_perfil_search_pets

        fun bindView(post: Post, pet: Pet) {
            val name = itemView.tv_pet_name_search_pets
            val photoProfile = itemView.iv_icon_foto_perfil_search_pets

            name.text = pet.nome
        }
    }
}