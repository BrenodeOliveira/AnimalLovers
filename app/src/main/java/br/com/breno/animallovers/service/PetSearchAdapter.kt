package br.com.breno.animallovers.service

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import br.com.breno.animallovers.R
import br.com.breno.animallovers.constants.StatusSolicitacaoAmizade
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.model.SolicitacaoAmizade
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
import kotlinx.android.synthetic.main.analize_friendship_request.*
import kotlinx.android.synthetic.main.analize_friendship_request.view.*
import kotlinx.android.synthetic.main.cancel_friendship_request.*
import kotlinx.android.synthetic.main.cancel_friendship_request.view.*
import kotlinx.android.synthetic.main.pet_search_item.view.*
import kotlinx.android.synthetic.main.undo_friendship.*
import kotlinx.android.synthetic.main.undo_friendship.view.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*


class PetSearchAdapter(private val pets: List<Pet>, private val context: Context) : RecyclerView.Adapter<PetSearchAdapter.ViewHolder>() {
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var dBase: DatabaseReference
    private var solicitacao = SolicitacaoAmizade()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetSearchAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.pet_search_item, parent, false)
        return PetSearchAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetSearchAdapter.ViewHolder, position: Int) {
        val pet = pets[(pets.size - 1) - position]

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        val myPreferences = ProjectPreferences(context)

        if (pet.pathFotoPerfil != "") {
            var storageRef = storage.reference.child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                .child(pet.idOwner)
                .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)
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

        holder.let {
            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).addListenerForSingleValueEvent(
                object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)) {
                            if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).hasChild(pet.idOwner)) {
                                if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).child(pet.idOwner).hasChild(pet.id)) {
                                    var solicitacaoAmizade = snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                                        .child(pet.idOwner)
                                        .child(pet.id)
                                        .getValue<SolicitacaoAmizade>()!!

                                    when (solicitacaoAmizade.statusSolicitacao) {
                                        StatusSolicitacaoAmizade.ACCEPTED.status -> it.icSendFriendShipRequest.setImageResource(R.drawable.ic_coracao_check)
                                        StatusSolicitacaoAmizade.WAITING.status -> it.icSendFriendShipRequest.setImageResource(R.drawable.ic_relogio_waiting_friend_request)
                                        StatusSolicitacaoAmizade.SENT.status -> it.icSendFriendShipRequest.setImageResource(R.drawable.ic_coracao_espera)
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.toString())
                    }

                })

            it.icSendFriendShipRequest.setOnClickListener {

                database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).addListenerForSingleValueEvent(
                    object :
                        ValueEventListener {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)) {
                                if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).hasChild(pet.idOwner)) {
                                    if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).child(pet.idOwner).hasChild(pet.id)) {
                                        var solicitacaoAmizade = snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                                            .child(pet.idOwner)
                                            .child(pet.id)
                                            .getValue<SolicitacaoAmizade>()!!

                                        when (solicitacaoAmizade.statusSolicitacao) {
                                            StatusSolicitacaoAmizade.SENT.status -> chamarMyDialogCancelFriendshipRequest(pet, solicitacaoAmizade)
                                            StatusSolicitacaoAmizade.WAITING.status -> chamarMyDialogAnalizeFriendshipRequest(pet, solicitacaoAmizade)
                                            StatusSolicitacaoAmizade.ACCEPTED.status -> chamarMyDialogUndoFriendship(pet, solicitacaoAmizade)
                                            StatusSolicitacaoAmizade.CANCELLED.status -> {
                                                sendFriendShipRequest(pet, solicitacao)
                                                it.iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_espera)
                                            }
                                        }
                                    } else {
                                        sendFriendShipRequest(pet, solicitacao)
                                        it.iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_espera)
                                    }
                                } else {
                                    sendFriendShipRequest(pet, solicitacao)
                                    it.iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_espera)
                                }
                            } else {
                                sendFriendShipRequest(pet, solicitacao)
                                it.iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_espera)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            println(error.toString())
                        }
                    })
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.tv_pet_name_search_pets
        var photoProfile: ImageView = itemView.iv_icon_foto_perfil_search_pets
        var icSendFriendShipRequest : ImageView = itemView.iv_add_friend_search_pets

        fun bindView(post: Post, pet: Pet) {
            val name = itemView.tv_pet_name_search_pets
            val photoProfile = itemView.iv_icon_foto_perfil_search_pets
            var icSendFriendShipRequest = itemView.iv_add_friend_search_pets

            name.text = pet.nome
        }
    }

    fun sendFriendShipRequest(pet: Pet, solicitacao: SolicitacaoAmizade) {
        val myPreferences = ProjectPreferences(context)

        //Adiciona no perfil desse usuário
        solicitacao.dataEnvioSolicitacao = DateUtils.dataFormatWithMilliseconds()
        solicitacao.statusSolicitacao = StatusSolicitacaoAmizade.SENT.status

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
            .child(pet.idOwner)
            .child(pet.id)
            .setValue(solicitacao)

        //Adiciona no perfil da solicitação enviada
        solicitacao.statusSolicitacao = StatusSolicitacaoAmizade.WAITING.status
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(pet.idOwner)
            .child(pet.id)
            .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .setValue(solicitacao)
    }

    fun chamarMyDialogCancelFriendshipRequest(pet: Pet, solicitacao: SolicitacaoAmizade) {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.cancel_friendship_request, null)
        val mBuilder = AlertDialog.Builder(context).setView(mDialogView).setTitle("Solicitação de amizade")
        val mAlertDialog = mBuilder.show()
        mAlertDialog.tv_pet_name_cancel_friendship_request.text = pet.nome

        if(pet.pathFotoPerfil != "") {
            var storageRef = storage.reference.child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                .child(pet.idOwner)
                .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                mAlertDialog.iv_icon_foto_perfil_cancel_friendship_request.setImageBitmap(bmp)

                if(mAlertDialog.iv_icon_foto_perfil_cancel_friendship_request.drawable == null) {
                    mAlertDialog.iv_icon_foto_perfil_cancel_friendship_request.visibility = View.INVISIBLE
                }
            }
        }
        mDialogView.btn_cancel_friendship_request.setOnClickListener {
            val myPreferences = ProjectPreferences(context)
            dBase = Firebase.database.reference

            //Desfaz a solicitação no usuário
            dBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(auth.uid.toString())
                .child(myPreferences.getPetLogged().toString())
                .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                .child(pet.idOwner)
                .child(pet.id)
                .setValue(null)

            //Desfaz a solicitação no perfil do pet que enviou
            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(pet.idOwner)
                .child(pet.id)
                .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                .child(auth.uid.toString())
                .child(myPreferences.getPetLogged().toString())
                .setValue(null)

            mAlertDialog.dismiss()
        }
        mDialogView.btn_donot_cancel_friendship_request.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    fun chamarMyDialogAnalizeFriendshipRequest(pet: Pet, solicitacao: SolicitacaoAmizade) {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.analize_friendship_request, null)
        val mBuilder = AlertDialog.Builder(context).setView(mDialogView).setTitle("Solicitação de amizade")
        val mAlertDialog = mBuilder.show()
        mAlertDialog.tv_pet_name_analize_friendship_request.text = pet.nome

        dBase = Firebase.database.reference
        val myPreferences = ProjectPreferences(context)

        if(pet.pathFotoPerfil != "") {
            var storageRef = storage.reference.child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                .child(pet.idOwner)
                .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                mAlertDialog.iv_icon_foto_perfil_analize_friendship_request.setImageBitmap(bmp)

                if(mAlertDialog.iv_icon_foto_perfil_analize_friendship_request.drawable == null) {
                    mAlertDialog.iv_icon_foto_perfil_analize_friendship_request.visibility = View.INVISIBLE
                }
            }
        }
        mDialogView.btn_confirm_friendship_request.setOnClickListener {

            val dataInicioAmizade = DateUtils.dataFormatWithMilliseconds()
            solicitacao.statusSolicitacao = StatusSolicitacaoAmizade.ACCEPTED.status
            //Persiste a solicitação aceita no usuário
            dBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(auth.uid.toString())
                .child(myPreferences.getPetLogged().toString())
                .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                .child(pet.idOwner)
                .child(pet.id)
                .setValue(solicitacao)

            //Persiste na lista de amigos o novo amigo
            dBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(auth.uid.toString())
                .child(myPreferences.getPetLogged().toString())
                .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome)
                .child(pet.idOwner)
                .child(pet.id)
                .setValue(dataInicioAmizade)

            //-----------------------------------------------------------------------//
            //Desfaz a solicitação no perfil do pet que enviou
            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(pet.idOwner)
                .child(pet.id)
                .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                .child(auth.uid.toString())
                .child(myPreferences.getPetLogged().toString())
                .setValue(solicitacao)

            //Persiste na lista de amigos o novo amigo
            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(pet.idOwner)
                .child(pet.id)
                .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome)
                .child(auth.uid.toString())
                .child(myPreferences.getPetLogged().toString())
                .setValue(dataInicioAmizade)

            mAlertDialog.dismiss()
        }
        mDialogView.btn_decline_friendship_request.setOnClickListener {

            //Desfaz a solicitação no usuário
            dBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(auth.uid.toString())
                .child(myPreferences.getPetLogged().toString())
                .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                .child(pet.idOwner)
                .child(pet.id)
                .setValue(null)

            //Desfaz a solicitação no perfil do pet que enviou
            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(pet.idOwner)
                .child(pet.id)
                .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                .child(auth.uid.toString())
                .child(myPreferences.getPetLogged().toString())
                .setValue(null)
            mAlertDialog.dismiss()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun dateToDateTime(dateToConvert: Date): LocalDateTime? {
        return dateToConvert.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun chamarMyDialogUndoFriendship(pet: Pet, solicitacao: SolicitacaoAmizade) {
        val myPreferences = ProjectPreferences(context)
        dBase = Firebase.database.reference
        var numMutualFriends = 0

        val mDialogView = LayoutInflater.from(context).inflate(R.layout.undo_friendship, null)
        val mBuilder = AlertDialog.Builder(context).setView(mDialogView).setTitle("Desfazer amizade")
        val mAlertDialog = mBuilder.show()
        mAlertDialog.tv_pet_name_undo_friendship.text = pet.nome

//        val diff: Duration = Duration.between(DateUtils.convertStringToDate(solicitacao.dataEnvioSolicitacao)?.let { dateToDateTime(it) }, LocalDateTime.now())
//        System.out.println(diff)
//        val formattedDiff: String = java.lang.String.format(Locale.ENGLISH, "%d days", diff.toDays())
//        println(formattedDiff)


        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DateUtils.dateFrmt())
        val start: LocalDateTime = LocalDateTime.parse(solicitacao.dataEnvioSolicitacao, formatter)
        val end: LocalDateTime = LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter)


        if(ChronoUnit.DAYS.between(start, end) > 31) {
            if (ChronoUnit.MONTHS.between(start, end) > 12) {
                if(ChronoUnit.YEARS.between(start, end) > 1) {
                    mAlertDialog.tv_friends_since_undo_friendship.text = "Amigos há " + ChronoUnit.YEARS.between(start, end) + " anos"
                } else {
                    mAlertDialog.tv_friends_since_undo_friendship.text = "Amigos há " + ChronoUnit.YEARS.between(start, end) + " ano"
                }
            } else if(ChronoUnit.MONTHS.between(start, end) <= 1){
                mAlertDialog.tv_friends_since_undo_friendship.text = "Amigos há " + ChronoUnit.MONTHS.between(start, end) + " mês"
            } else {
                mAlertDialog.tv_friends_since_undo_friendship.text = "Amigos há " + ChronoUnit.MONTHS.between(start, end) + " meses"
            }
        } else if (ChronoUnit.DAYS.between(start, end) < 1) {
            if(ChronoUnit.HOURS.between(start, end) > 1) {
                mAlertDialog.tv_friends_since_undo_friendship.text = "Amigos há " + ChronoUnit.HOURS.between(start, end) + " horas"
            } else if (ChronoUnit.HOURS.between(start, end).toInt() == 1){
                mAlertDialog.tv_friends_since_undo_friendship.text = "Amigos há " + ChronoUnit.HOURS.between(start, end) + " hora"
            } else {
                mAlertDialog.tv_friends_since_undo_friendship.text = "Amigos há " + ChronoUnit.MINUTES.between(start, end) + " minutos"
            }
        } else {
            mAlertDialog.tv_friends_since_undo_friendship.text = "Amigos há " + ChronoUnit.DAYS.between(start, end) + " dias"
        }
        System.out.println(ChronoUnit.DAYS.between(start, end))



        dBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.child(pet.idOwner).child(pet.id).child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome).hasChildren() && snapshot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome).hasChildren()) {

                    val amgsSearchPet: HashMap<String, HashMap<String, HashMap<String, String>>> = snapshot.child(pet.idOwner)
                        .child(pet.id)
                        .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome).value as HashMap<String, HashMap<String, HashMap<String, String>>>

                    val amgsPet: HashMap<String, HashMap<String, HashMap<String, String>>> = snapshot.child(auth.uid.toString())
                        .child(myPreferences.getPetLogged().toString())
                        .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome).value as HashMap<String, HashMap<String, HashMap<String, String>>>

                    for (i in 0 until amgsPet.size) {
                        for (j in 0 until amgsSearchPet.size) {
                            val idOwnerAmgSearchPet = amgsSearchPet.keys.toList()[j]
                            val idOwnerAmgPet = amgsPet.keys.toList()[i]

                            if (idOwnerAmgSearchPet.equals(idOwnerAmgPet)) {

                                for (k in 0 until (amgsPet.get(idOwnerAmgPet)?.size ?: 0)) {
                                    for (l in 0 until (amgsSearchPet.get(idOwnerAmgSearchPet)?.size ?: 0)) {
                                        if (amgsPet.get(idOwnerAmgPet)?.keys?.toList()?.get(k)?.equals(amgsSearchPet.get(idOwnerAmgSearchPet)?.keys?.toList()?.get(l)!!)!!) {
                                            numMutualFriends++
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (numMutualFriends == 0) {
                        mAlertDialog.tv_mutual_friends_undo_friendship.setText("Nenhum amigo em comum")
                    } else if (numMutualFriends == 1) {
                        mAlertDialog.tv_mutual_friends_undo_friendship.setText(numMutualFriends.toString() + " amigo em comum")
                    } else {
                        mAlertDialog.tv_mutual_friends_undo_friendship.setText(numMutualFriends.toString() + " amigos em comum")
                    }
                } else {
                    mAlertDialog.tv_mutual_friends_undo_friendship.setText("Nenhum amigo em comum11")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println(error.toString())
            }

        })
        if(pet.pathFotoPerfil != "") {
            var storageRef = storage.reference.child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                .child(pet.idOwner)
                .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                mAlertDialog.iv_icon_foto_perfil_undo_friendship.setImageBitmap(bmp)

                if(mAlertDialog.iv_icon_foto_perfil_undo_friendship.drawable == null) {
                    mAlertDialog.iv_icon_foto_perfil_undo_friendship.visibility = View.INVISIBLE
                }
            }
        }
        mDialogView.btn_undo_friendship.setOnClickListener {
            //Desfaz a solicitação no usuário
            dBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(auth.uid.toString())
                .child(myPreferences.getPetLogged().toString())
                .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome)
                .child(pet.idOwner)
                .child(pet.id)
                .setValue(null)

            solicitacao.statusSolicitacao = StatusSolicitacaoAmizade.CANCELLED.status
            dBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(auth.uid.toString())
                .child(myPreferences.getPetLogged().toString())
                .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                .child(pet.idOwner)
                .child(pet.id)
                .setValue(solicitacao)

            //Desfaz a solicitação no perfil do pet que enviou
            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(pet.idOwner)
                .child(pet.id)
                .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome)
                .child(auth.uid.toString())
                .child(myPreferences.getPetLogged().toString())
                .setValue(null)

            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(pet.idOwner)
                .child(pet.id)
                .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                .child(auth.uid.toString())
                .child(myPreferences.getPetLogged().toString())
                .setValue(solicitacao)

            mAlertDialog.dismiss()
        }
        mDialogView.btn_donot_undo_friendship.setOnClickListener {
            mAlertDialog.dismiss()
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

}