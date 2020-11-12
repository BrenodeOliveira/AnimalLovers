package br.com.breno.animallovers.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.breno.animallovers.R
import br.com.breno.animallovers.ui.activity.extensions.mostraToast
import kotlinx.android.synthetic.main.activity_publish.*

private const val CAMERA_RQ = 102
private const val IMAGE_PICK_CODE = 1000
private const val STORAGE_RQ = 1001
private const val REQUEST_CODE = 42

class PublishActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)
        requestingPermissionToUser()

        clickButtonCamera()
        clickButtonGallery()

        setSupportActionBar(toolbar_publi)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun clickButtonGallery() {
        btn_galery.setOnClickListener {
            if (VERSION.SDK_INT >= VERSION_CODES.M) {
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

    private fun requestingPermissionToUser() {
        checkForPermission(Manifest.permission.CAMERA, "camera", CAMERA_RQ)
    }

    private fun clickButtonCamera() {
        btn_camera.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
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
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
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
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permissão para acessar sua câmera é requerida para usar esse app")
            setTitle("Requisição de permissão")
            setPositiveButton("Ok") { _, _ ->
                ActivityCompat
                    .requestPermissions(
                        this@PublishActivity,
                        arrayOf(permission), requestCode
                    )
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
}