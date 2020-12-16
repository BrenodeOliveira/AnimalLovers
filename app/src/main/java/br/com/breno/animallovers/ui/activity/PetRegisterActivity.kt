package br.com.breno.animallovers.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import br.com.breno.animallovers.ui.activity.extensions.mostraToastySuccess
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_pet_register.*
import java.io.ByteArrayOutputStream


private const val CAMERA_RQ = 102
private const val IMAGE_PICK_CODE = 1000
private const val STORAGE_RQ = 1001
private const val REQUEST_CODE = 42

private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth

private val petService = PetService()
private var pet = Pet()

private var idPet: Int = 0

class PetRegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_register)
        requestingPermissionToUser()

        clickButtonCamera()
        clickButtonGallery()
        clickButtonRegisterPet()
    }

    private fun clickButtonRegisterPet() {
        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        btn_cadastrar_pet.setOnClickListener {
            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(auth.uid.toString()).addListenerForSingleValueEvent(
                object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        idPet = petService.idFirstPet(dataSnapshot) + 1 //Incrementa os ids dos pets
                        val checkedRadio: Int = radio_sexo_animal.checkedRadioButtonId
                        val checkedRadioButton = findViewById<RadioButton>(checkedRadio)
                        val checkedBox = checkedRadioButton.text.toString()

                        pet.resumo = resumo_pet_register.editText?.text.toString()
                        pet.idade = idade_pet_register.editText?.text.toString()
                        pet.nome = nome_pet_register.editText?.text.toString()
                        pet.peso = peso_pet_register.editText?.text.toString()
                        pet.raca = raca_pet_register.editText?.text.toString()
                        pet.tipo = tipo_pet_register.editText?.text.toString()
                        pet.sexo = checkedBox

                        if (null != iv_photo_to_profile.drawable) {
                            val bitmap = (iv_photo_to_profile.drawable as BitmapDrawable).bitmap
                            val baos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val dataPicture = baos.toByteArray()


                            petService.uploadProfilePhotoPet(idPet, dataPicture, pet)
                        } else {
                            petService.registerNewPet(idPet, pet)
                        }
                        mostraToastySuccess("Novo pet registrado com sucesso")
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

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
                        this@PetRegisterActivity,
                        arrayOf(permission), requestCode
                    )
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun clickButtonCamera() {
        btn_camera_cadastro_pet.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            } else {
                mostraToast("Impossivel abrir a camera")
            }
        }
    }

    private fun clickButtonGallery() {
        btn_galery_cadastro_pet.setOnClickListener {
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
            iv_photo_to_profile.setImageBitmap(takenImage)
        }
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            iv_photo_to_profile.setImageURI(data?.data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@PetRegisterActivity, ProfileActivity::class.java))
        finish()
    }
}