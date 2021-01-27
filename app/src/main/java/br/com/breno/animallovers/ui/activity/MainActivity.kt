package br.com.breno.animallovers.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.ui.activity.extensions.mostraToast
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.ProjectPreferences
import br.com.breno.animallovers.viewModel.EstadoAppViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.iv_photo_owner_contact
import kotlinx.android.synthetic.main.nav_header.*
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var storage: FirebaseStorage
    private lateinit var database: DatabaseReference
    private var accountInfo = Conta()

    private val controlador by lazy {
        findNavController(R.id.main_activity_nav_host)
    }
    private val viewModel: EstadoAppViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(feed_toolbar)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        checkForPermissions(
            android.Manifest.permission.ACCESS_FINE_LOCATION, "localização",
            LOCATION_PERMISSION_REQUEST_CODE
        )

        inicializaNavDrawer()
        clickUserPage()

//        controlador.addOnDestinationChangedListener { controller, destination, arguments ->
//
//            title = destination.label
//
//            viewModel.componentes.observe(this, Observer {
//                it?.let { temComponentes ->
//                    if (temComponentes.appBar) {
//                        supportActionBar?.show()
//                    } else {
//                        supportActionBar?.hide()
//                    }
//                    if (temComponentes.bottomNavigation) {
//                        bottomNavigationView.visibility = VISIBLE
//                    } else {
//                        bottomNavigationView.visibility = GONE
//                    }
//                }
//            })
//        }

        bottomNavigationView.setupWithNavController(controlador)

    }

    private fun clickUserPage() {
        iv_user.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun inicializaNavDrawer() {
        val toggle = ActionBarDrawerToggle(this, drawer_layout, feed_toolbar, 0, 0)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        retrieveUserInfo()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.alter_data -> {
                startActivity(Intent(this, UserDataActivity::class.java))
            }
            R.id.clinics_pets -> {
                startActivity(Intent(this, MapsActivity::class.java))
            }
            R.id.get_out -> {
                FirebaseAuth.getInstance().signOut()

                finish()
                val intent = Intent(this, SplashActivity::class.java)
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

// Começa a parte para permissão do mapa
    private fun checkForPermissions(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(applicationContext, permission) ==
                        PackageManager.PERMISSION_GRANTED -> {

                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(
                    permission,
                    name,
                    requestCode
                )

                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        fun innerCheck(name: String) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                mostraToast("Permissão negada para $name")
            } else {
                mostraToast("Permissão concedida para usar a $name")
            }
        }

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> innerCheck("localização")
        }

    }

    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permissão para acessar sua localização é requerida para usar esse app")
            setTitle("Requisição de permissão")
            setPositiveButton("OK") { _, _ ->
                ActivityCompat
                    .requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun retrieveUserInfo() {
        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).child(auth.uid.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                accountInfo = dataSnapshot.child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome).getValue<Conta>()!!

                if(accountInfo.pathFotoPerfil != "") {
                    retrieveOwnerProfilePhoto()
                }
                val paisEstado = accountInfo.cidade + " - " + accountInfo.pais
                txt_name_owner_account_main.text = accountInfo.usuario
                txt_local_account_main.text = paisEstado
            }

            override fun onCancelled(error: DatabaseError) {
                Toasty.error(baseContext, "Erro ao carregar informações do perfil", Toasty.LENGTH_LONG).show()
            }
        })

    }

    private fun retrieveOwnerProfilePhoto() {
        storage = FirebaseStorage.getInstance()
        var storageRef = storage.reference
            .child(AnimalLoversConstants.STORAGE_ROOT.nome)
            .child(AnimalLoversConstants.STORAGE_ROOT_OWNER_PHOTOS.nome)
            .child(auth.uid.toString())
            .child(auth.uid.toString() + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {bytesPrm ->
            val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
            img_profile_nav_main.setImageBitmap(bmp)
        }.addOnFailureListener {

        }
    }
}