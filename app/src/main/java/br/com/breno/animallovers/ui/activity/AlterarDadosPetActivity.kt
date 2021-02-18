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
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.service.PetService
import br.com.breno.animallovers.ui.activity.extensions.mostraToast
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_alterar_dados_pet.*
import kotlinx.android.synthetic.main.activity_pet_register.*
import java.io.ByteArrayOutputStream

private const val CAMERA_RQ = 102
private const val IMAGE_PICK_CODE = 1000
private const val STORAGE_RQ = 1001
private const val REQUEST_CODE = 42

class AlterarDadosPetActivity : AppCompatActivity() {
    private var petInfo = Pet()
    val petService = PetService()

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alterar_dados_pet)
        petInfo = (intent.getSerializableExtra("PET_INFO") as? Pet)!!

        requestingPermissionToUser()

        populatePetInfo(petInfo)
        updatePetInfo(petInfo)

        clickButtonCamera()
        clickButtonGallery()

    }

    private fun populatePetInfo(petInfo : Pet) {
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        nome_change_pet.editText!!.setText(petInfo.nome)
        idade_change_pet.editText!!.setText(petInfo.idade)
        peso_change_pet.editText!!.setText(petInfo.peso)
        tipo_change_pet.editText!!.setText(petInfo.tipo)
        raca_change_pet.editText!!.setText(petInfo.raca)
        resumo_change_pet.editText!!.setText(petInfo.resumo)

        if(petInfo.sexo == AnimalLoversConstants.FEMALE.nome) {
            radio_sexo_animal_change_pet.check(R.id.femea_change_pet)
        } else {
            radio_sexo_animal_change_pet.check(R.id.macho_change_pet)
        }

        if(petInfo.pathFotoPerfil != "") {
            val storageRef = storage.reference.child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                .child(petInfo.idOwner)
                .child(petInfo.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)
            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                iv_photo_to_profile_change_pet.setImageBitmap(bmp)

            }.addOnFailureListener {
                println(it.toString())
            }
        }

    }

    private fun updatePetInfo(pet: Pet) {
        btn_alterar_pet.setOnClickListener {

            if(nome_change_pet.editText!!.text.toString() != "") {
                pet.nome = nome_change_pet.editText!!.text.toString()
                pet.idade = idade_change_pet.editText!!.text.toString()
                pet.peso = peso_change_pet.editText!!.text.toString()
                pet.tipo = tipo_change_pet.editText!!.text.toString()
                pet.raca = raca_change_pet.editText!!.text.toString()
                pet.resumo = resumo_change_pet.editText!!.text.toString()

                val checkedRadio: Int = radio_sexo_animal_change_pet.checkedRadioButtonId
                val checkedRadioButton = findViewById<RadioButton>(checkedRadio)
                val checkedBox = checkedRadioButton.text.toString()

                pet.sexo = checkedBox
                petService.updatePetInfo(petInfo)
                Toasty.info(baseContext, "Alteração realizada").show()
                finish()
            }
            else {
                Toasty.error(baseContext, "Digite um nome para o pet\nO valor não pode ficar em branco").show()
            }

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
                        this@AlterarDadosPetActivity,
                        arrayOf(permission), requestCode
                    )
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun clickButtonCamera() {
        btn_camera_change_pet.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            } else {
                mostraToast("Impossivel abrir a camera")
            }
        }
    }

    private fun clickButtonGallery() {
        btn_galery_change_pet.setOnClickListener {
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
            iv_photo_to_profile_change_pet.setImageBitmap(takenImage)

            val bitmap = (iv_photo_to_profile_change_pet.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val dataPicture = baos.toByteArray()

            petService.uploadOrUpdateProfilePhotoPet(dataPicture, petInfo)
            Toasty.success(baseContext, "Foto de perfil alterada", Toasty.LENGTH_LONG).show()
        }
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            iv_photo_to_profile_change_pet.setImageURI(data?.data)

            val bitmap = (iv_photo_to_profile_change_pet.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val dataPicture = baos.toByteArray()

            petService.uploadOrUpdateProfilePhotoPet(dataPicture, petInfo)

            Toasty.success(baseContext, "Foto de perfil alterada", Toasty.LENGTH_LONG).show()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


}