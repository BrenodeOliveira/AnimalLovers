package br.com.breno.animallovers.adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.breno.animallovers.R
import br.com.breno.animallovers.constants.KindOfPet
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.service.ModalBottomSheet
import br.com.breno.animallovers.ui.activity.MainActivity
import br.com.breno.animallovers.ui.activity.ProfileActivity
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.pet_item.view.*


class PetsProfileAdapter(
    private val pet: List<Pet>,
    private val context: Context,
    private val mbs: ModalBottomSheet
) : RecyclerView.Adapter<PetsProfileAdapter.ViewHolder>() {
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pets = pet[position]

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        if(pet.isEmpty()) {
            println("Não há pets")
        }
        if (pets.pathFotoPerfil != "") {
            var storageRef = storage.reference
                .child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                .child(auth.uid.toString())
                .child(pets.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                holder.let {
                    it.title.text = pets.nome
                    it.description.text = pets.resumo
                    it.photo.setImageBitmap(bmp)
                }
            }.addOnFailureListener {

            }
        }
        else {
            holder.let {
                it.title.text = pets.nome
                it.description.text = pets.resumo

                when (pets.tipo) {
                    KindOfPet.DOG.tipo -> {
                        it.photo.setImageResource(R.drawable.ic_dog_pet)
                    }
                    KindOfPet.CAT.tipo -> {
                        it.photo.setImageResource(R.drawable.ic_cat_pet)
                    }
                    KindOfPet.BIRD.tipo -> {
                        it.photo.setImageResource(R.drawable.ic_bird_pet)
                    }
                    else -> {
                        it.photo.setImageResource(R.drawable.ic_unkown_pet)
                    }
                }
            }
        }
        holder.let {
            it.photo.setOnClickListener {
                val myPreferences = ProjectPreferences(context)
                myPreferences.setPetLogged(pets.id)

                val i = Intent(context, MainActivity::class.java)
                context.startActivity(i)

                mbs.dismiss()
            }
            it.description.setOnClickListener {
                val myPreferences = ProjectPreferences(context)
                myPreferences.setPetLogged(pets.id)
                context.startActivity(Intent(context, MainActivity::class.java))
                mbs.dismiss()
            }
            it.title.setOnClickListener {
                val myPreferences = ProjectPreferences(context)
                myPreferences.setPetLogged(pets.id)
                context.startActivity(Intent(context, MainActivity::class.java))
                mbs.dismiss()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.pet_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pet.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.note_item_title
        val description = itemView.note_item_description
        var photo = itemView.iv_icon_foto_perfil

        fun bindView(pet: Pet) {
            val title = itemView.note_item_title
            val description = itemView.note_item_description

            title.text = pet.nome
            description.text = pet.resumo
        }
    }


}
