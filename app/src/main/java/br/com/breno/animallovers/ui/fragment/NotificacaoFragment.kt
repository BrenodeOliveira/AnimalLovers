package br.com.breno.animallovers.ui.fragment

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.adapters.FeedAdapter
import br.com.breno.animallovers.adapters.NotificationAdapter
import br.com.breno.animallovers.model.Notification
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.service.PetService
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile_pet.*
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_notificacao.*

class NotificacaoFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notificacao, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var myPreferences = ProjectPreferences(view.context)
        var database = Firebase.database.reference
        var db = Firebase.database.reference
        var auth = FirebaseAuth.getInstance()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    loadPetInfo(snapshot.child(auth.uid.toString()), view.context)
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.toString())
                }

            })

        db.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var listNotifications = ArrayList<Notification>()

                    var dataSnapshot = snapshot.child(auth.uid.toString())
                        .child(myPreferences.getPetLogged().toString())
                    if(dataSnapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)) {
                        for(i in 0 until dataSnapshot.child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome).childrenCount) {
                            var notification = dataSnapshot.child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome).child(i.toString()).getValue<Notification>()!!

                            if(notification.ativo) {
                                listNotifications.add(notification)
                            }
                        }
                    }
                    listNotifications.sortBy { DateUtils.convertStringToDate(it.dataHora) }
                    listNotifications.reverse()
                    recycler_notifications.layoutManager = LinearLayoutManager(requireContext())
                    recycler_notifications.adapter = NotificationAdapter(requireContext(), listNotifications.toMutableList())

                    val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                    recycler_notifications.layoutManager = layoutManager
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.toString())
                }

            })
    }

    private fun loadPetInfo(snapshot: DataSnapshot, context: Context) {
        val petService = PetService(context)
        var myPreferences = ProjectPreferences(context)
        var pet = petService.retrievePetInfo(myPreferences.getPetLogged().toString(), snapshot)

        tv_pet_name_notification_fragment.text = pet.nome

        if(pet.pathFotoPerfil != "") {
            retrieveOwnerProfilePhoto(pet)
        }
    }

    private fun retrieveOwnerProfilePhoto(pet: Pet) {
        var storage: FirebaseStorage = FirebaseStorage.getInstance()
        var storageRef = storage.reference
            .child(AnimalLoversConstants.STORAGE_ROOT.nome)
            .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
            .child(pet.idOwner)
            .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
            val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
            iv_icon_profile_photo_notification.setImageBitmap(bmp)
        }.addOnFailureListener {

        }
    }

}