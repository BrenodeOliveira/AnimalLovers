package br.com.breno.animallovers.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.breno.animallovers.constants.StatusSolicitacaoAmizade
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.SolicitacaoAmizade
import br.com.breno.animallovers.notification.KindOfNotification
import br.com.breno.animallovers.service.FriendShipService
import br.com.breno.animallovers.service.NotificationService
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


class BroadcastReceiverNotificationFriendship : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val friendshipService = FriendShipService(context!!)
        var notificationService = NotificationService(context)
        var auth: FirebaseAuth = FirebaseAuth.getInstance()
        var database: DatabaseReference = Firebase.database.reference
        val myPreferences = ProjectPreferences(context)


        val kindOfNotificationExtra = intent?.getStringExtra("KIND_OF_FRIENDSHIP_REQUEST")
        val petInfo = intent?.getSerializableExtra("PET_INFO_PROFILE") as Pet
        val petLoggedInfo = intent?.getSerializableExtra("PET_LOGGED_INFO_PROFILE") as Pet

        when {
            kindOfNotificationExtra == KindOfNotification.FRIENDSHIP_REQUEST_ACCEPTED.nome -> {
            }
            kindOfNotificationExtra == KindOfNotification.FRIENDSHIP_REQUEST_RECEIVED.nome -> {
                //Aceitar a solicitação de amizade

                val solicitacao = SolicitacaoAmizade()

                solicitacao.statusSolicitacao = StatusSolicitacaoAmizade.ACCEPTED.status
                val dataInicioAmizade = DateUtils.dataFormatWithMilliseconds()
                solicitacao.statusSolicitacao = StatusSolicitacaoAmizade.ACCEPTED.status
                //Persiste a solicitação aceita no usuário
                friendshipService.persistFriendShipRequestInReceiver(solicitacao, petInfo)

                //Persiste na lista de amigos o novo amigo
                friendshipService.saveNewFriendShipReceiver(dataInicioAmizade, petInfo)

                //-----------------------------------------------------------------------//
                //Desfaz a solicitação no perfil do pet que enviou
                friendshipService.persistFriendShipRequestInSender(solicitacao, petInfo)

                //Persiste na lista de amigos o novo amigo
                friendshipService.saveNewFriendShipSender(dataInicioAmizade, petInfo)

                database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).child(petInfo.idOwner).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val petLogged = snapshot.child(myPreferences.getPetLogged().toString()).child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome).getValue<Pet>()!!
                        val owner = snapshot.getValue<Conta>()!!

                        notificationService.sendNotificationOfFriendshipAccepted(petInfo, petLogged, owner)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.toString())
                    }

                })

            }
        }
    }
}