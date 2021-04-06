package br.com.breno.animallovers.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isEmpty
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.service.DonoService
import br.com.breno.animallovers.ui.activity.extensions.mostraToast
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_user_data.*
import java.io.ByteArrayOutputStream

private const val CAMERA_RQ = 102
private const val IMAGE_PICK_CODE = 1000
private const val STORAGE_RQ = 1001
private const val REQUEST_CODE = 42

private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private lateinit var storage: FirebaseStorage

private val donoService = DonoService()
private var accountInfo = Conta()

class UserDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_data)

        requestingPermissionToUser()
        clickButtonCamera()
        clickButtonGallery()

        retriveOwnerInfo()
        persistOwner()

        /**
         * Fazer com que o usuário se cadastre para que tenha nome, foto e local de onde
         * está usando o app
         */

    }

    private fun retriveOwnerInfo() {
        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).child(auth.uid.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_OWNER.nome)) {
                    accountInfo = dataSnapshot.child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome).getValue<Conta>()!!
                    tv_nome_dono.setText(accountInfo.usuario)
                    tv_cidade_dono.setText(accountInfo.cidade)
                    tv_pais_dono.setText(accountInfo.pais)

                    if(accountInfo.pathFotoPerfil != "") {
                        retrieveProfilePhoto()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println(error.message)
            }
        })
    }

    private fun persistOwner() {
        btn_cadastrar_user.setOnClickListener{
            database = Firebase.database.reference
            auth = FirebaseAuth.getInstance()

            if (tv_nome_dono.text.toString().isNotEmpty() and tv_pais_dono.text.toString().isNotEmpty() and tv_cidade_dono.text.toString().isNotEmpty()) {
                database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).child(auth.uid.toString()).addListenerForSingleValueEvent(
                    object :
                        ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            accountInfo.usuario = tv_nome_dono.text.toString()
                            accountInfo.cidade = tv_cidade_dono.text.toString()
                            accountInfo.pais = tv_pais_dono.text.toString()

                            if (iv_photo_to_profile_owner.drawable == null) {
                                donoService.persistOwner(accountInfo)
                            } else {
                                val bitmap = (iv_photo_to_profile_owner.drawable as BitmapDrawable).bitmap
                                val baos = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                val dataPicture = baos.toByteArray()

                                donoService.uploadProfilePhotoOwner(dataPicture, accountInfo)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                finish()
            } else {
                mostraToast("Preencha os campos requeridos")
            }
        }
    }

    private fun retrieveProfilePhoto() {
        storage = FirebaseStorage.getInstance()
        var storageRef = storage.reference
            .child(AnimalLoversConstants.STORAGE_ROOT.nome)
            .child(AnimalLoversConstants.STORAGE_ROOT_OWNER_PHOTOS.nome)
            .child(auth.uid.toString())
            .child(auth.uid.toString() + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {bytesPrm ->
            val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
            iv_photo_to_profile_owner.setImageBitmap(bmp)
        }.addOnFailureListener {

        }
    }

    private fun requestingPermissionToUser() {
        checkForPermission(Manifest.permission.CAMERA, "camera", CAMERA_RQ)
    }

    private fun checkForPermission(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(applicationContext, permission) ==
                        PackageManager.PERMISSION_GRANTED -> {
                }

                shouldShowRequestPermissionRationale(permission) ->
                    showDialogPermissions(permission, name, requestCode)

                else -> ActivityCompat
                    .requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    private fun showDialogPermissions(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permissão para acessar sua câmera é requerida para usar esse app")
            setTitle("Requisição de permissão")
            setPositiveButton("Ok") { _, _ ->
                ActivityCompat
                    .requestPermissions(
                        this@UserDataActivity,
                        arrayOf(permission), requestCode
                    )
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun clickButtonCamera() {
        btn_camera_cadastro_user.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            } else {
                mostraToast("Impossivel abrir a camera")
            }
        }
    }

    private fun clickButtonGallery() {
        btn_galery_cadastro_user.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    //denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, STORAGE_RQ)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val takenImage = data?.extras?.get("data") as Bitmap
            iv_photo_to_profile_owner.setImageBitmap(takenImage)
        }
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            iv_photo_to_profile_owner.setImageURI(data?.data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}