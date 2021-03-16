package br.com.breno.animallovers.ui.activity

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.service.FeedAdapter

class SinglePostActivity : AppCompatActivity() {
    private var post = Post()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_post)
        post = (intent.getSerializableExtra("POST_INFO") as? Post)!!

        var posts = ArrayList<Post>()
        posts.add(post)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_single_post)
        recyclerView.layoutManager = LinearLayoutManager(this@SinglePostActivity)
        recyclerView.adapter = FeedAdapter(posts, this@SinglePostActivity)

        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
    }
}