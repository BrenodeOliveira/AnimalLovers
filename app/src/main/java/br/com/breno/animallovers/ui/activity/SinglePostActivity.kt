package br.com.breno.animallovers.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.adapters.FeedAdapter
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth

class SinglePostActivity : AppCompatActivity() {
    private var post = Post()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myPreferences = ProjectPreferences(baseContext)
        setContentView(R.layout.activity_single_post)
        post = (intent.getSerializableExtra("POST_INFO") as? Post)!!

        if((intent.getSerializableExtra("CAME_FROM_NOTIFICATION") as? Boolean)!!) {
            if(post.idOwner == auth.uid.toString()) {
                if(post.idPet != myPreferences.getPetLogged().toString()) {
                    myPreferences.setPetLogged(post.idPet)
                }
            }
        }

        val shouldInflateComments = intent.getSerializableExtra("SHOULD_INFLATE_COMMENT") as? Boolean

        var posts = ArrayList<Post>()
        posts.add(post)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_single_post)
        recyclerView.layoutManager = LinearLayoutManager(this@SinglePostActivity)
        recyclerView.adapter = FeedAdapter(posts, this@SinglePostActivity, shouldInflateComments!!)

        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if((intent.getSerializableExtra("CAME_FROM_NOTIFICATION") as? Boolean)!!) {
            startActivity(Intent(this@SinglePostActivity, MainActivity::class.java))
            finish()
        }
    }
}