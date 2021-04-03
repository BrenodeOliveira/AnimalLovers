package br.com.breno.animallovers.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.breno.animallovers.R
import br.com.breno.animallovers.constants.KindOfPet
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.service.PetService
import br.com.breno.animallovers.service.PostService
import br.com.breno.animallovers.ui.fragment.extensions.mostraToast
import br.com.breno.animallovers.ui.fragment.extensions.mostraToastyError
import br.com.breno.animallovers.ui.fragment.extensions.mostraToastySuccess
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
import kotlinx.android.synthetic.main.fragment_publish.*
import java.io.ByteArrayOutputStream

private const val CAMERA_RQ = 102
private const val IMAGE_PICK_CODE = 1000
private const val STORAGE_RQ = 1001
private const val REQUEST_CODE = 42

private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth



private var accountInfo = Conta()
private var post = Post()
private var pet = Pet()

private var idPet: String = ""
private lateinit var storage: FirebaseStorage

class AdicionarFragment : Fragment() {



    private val controlador by lazy {
        findNavController()
    }
//    private val estadoAppViewModel: EstadoAppViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_publish, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestingPermissionToUser()

        getPetsName(view.context)
        clickButtonCamera()
        clickButtonGallery()
        clickPublishPost(view.context)

//        estadoAppViewModel.temComponentes = ComponentesVisuais(appBar = false)

//        setSupportActionBar(toolbar_publi)
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }

    private fun getPetsName(context: Context) {
        val petService = PetService(context)
        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).child(
            auth.uid.toString()
        ).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                accountInfo = dataSnapshot.getValue<Conta>()!!
                val myPreferences = ProjectPreferences(requireContext())

                idPet = myPreferences.getPetLogged().toString()
                if (idPet != "") {
                    pet = petService.retrievePetInfo(idPet, dataSnapshot)

                    if(pet.pathFotoPerfil != "") {
                        retrieveProfilePhoto(idPet)
                    }
                    else {
                        when (pet.tipo) {
                            KindOfPet.DOG.tipo -> {
                                iv_pet_photo_profile_publish.setImageResource(R.drawable.ic_dog_pet)
                            }
                            KindOfPet.CAT.tipo -> {
                                iv_pet_photo_profile_publish.setImageResource(R.drawable.ic_cat_pet)
                            }
                            KindOfPet.BIRD.tipo -> {
                                iv_pet_photo_profile_publish.setImageResource(R.drawable.ic_bird_pet)
                            }
                            else -> {
                                iv_pet_photo_profile_publish.setImageResource(R.drawable.ic_unkown_pet)
                            }
                        }
                    }
                    tv_name_user.text = pet.nome
                } else {
                    tv_name_user.text = ""
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun retrieveProfilePhoto(idPet: String) {
        storage = FirebaseStorage.getInstance()
        var storageRef = storage.reference
            .child(AnimalLoversConstants.STORAGE_ROOT.nome)
            .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
            .child(auth.uid.toString())
            .child(idPet + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {bytesPrm ->
            val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
            iv_pet_photo_profile_publish.setImageBitmap(bmp)
        }.addOnFailureListener {

        }
    }

    private fun clickPublishPost(context : Context) {
        val postService = PostService(context)

        btn_publish.setOnClickListener {
            database = Firebase.database.reference
            auth = FirebaseAuth.getInstance()
            val myPreferences = ProjectPreferences(requireContext())

            post.legenda = et_comment.text.toString()
            post.nomePet = tv_name_user.text as String
            post.idOwner = auth.uid.toString()
            post.idPet = myPreferences.getPetLogged().toString()

            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(
                   auth.uid.toString()
                ).addListenerForSingleValueEvent(object :
                ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    accountInfo =
                        dataSnapshot.getValue<Conta>()!!

                    val myPreferences = ProjectPreferences(requireContext())

                    idPet =
                        myPreferences.getPetLogged().toString()
                    if (idPet != "") {
                        iv_photo_to_publish.isDrawingCacheEnabled = true
                        iv_photo_to_publish.buildDrawingCache()

                        if (null == iv_photo_to_publish.drawable && post.legenda == "") {
                            mostraToastyError("Para realizar um post é necessário: \n- Digitar um texto;\nOu\n- Inserir uma foto.")
                        } else {
                            if (null != iv_photo_to_publish.drawable) {
                                val bitmap = (iv_photo_to_publish.drawable as BitmapDrawable).bitmap
                                val baos = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                val dataPicture = baos.toByteArray()

                                postService.persistNewPetPost(
                                    idPet, dataPicture, post )
                            } else {
                                //Como não há foto/vídeo, a data/hora da pub será definida aqui, se houvesse seria a data/hora de upload
                                post.dataHora =
                                    DateUtils.dataFormatWithMilliseconds()
                                post.pathPub = ""
                                postService.registerNewPost(
                                    idPet,
                                    post
                                )
                            }
                            mostraToastySuccess("Novo post realizado")
                            controlador.popBackStack()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    mostraToastyError("Erro ao carregar informações do perfil")
                }
            })
        }
    }

    private fun clickButtonGallery() {
        btn_galery.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    //denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(
                        permissions,
                        STORAGE_RQ
                    )
                } else {
                    //granted
                    pickImageFromGallery()
                }

            } else {
                //system OS is < M
                pickImageFromGallery()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun requestingPermissionToUser() {
        checkForPermission(
            Manifest.permission.CAMERA, "camera",
            CAMERA_RQ
        )
    }

    private fun clickButtonCamera() {
        btn_camera.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(
                    takePictureIntent,
                    REQUEST_CODE
                )
            } else {
                mostraToast("Impossivel abrir a camera")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val takenImage = data?.extras?.get("data") as Bitmap
            iv_photo_to_publish.setImageBitmap(takenImage)
        }
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            iv_photo_to_publish.setImageURI(data?.data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun checkForPermission(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                checkSelfPermission(requireContext(), permission) ==
                        PackageManager.PERMISSION_GRANTED -> {
                }

                shouldShowRequestPermissionRationale(permission) ->
                    showDialogPermissions(permission, name, requestCode)

                else -> ActivityCompat
                    .requestPermissions(requireActivity(), arrayOf(permission), requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        fun innerCheck(name: String) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                mostraToast("Permissão negada")
            } else {
                mostraToast("Permissão concedida")
            }
        }

        when (requestCode) {
            CAMERA_RQ -> innerCheck("camera")
            STORAGE_RQ -> innerCheck("gallery")
        }

    }

    private fun showDialogPermissions(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(requireContext())

        builder.apply {
            setMessage("Permissão para acessar sua câmera é requerida para usar esse app")
            setTitle("Requisição de permissão")
            setPositiveButton("Ok") { _, _ ->
                ActivityCompat
                    .requestPermissions(
                        requireActivity(),
                        arrayOf(permission), requestCode
                    )
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
}