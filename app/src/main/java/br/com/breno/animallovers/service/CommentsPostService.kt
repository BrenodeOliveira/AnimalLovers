package br.com.breno.animallovers.service

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Comentario
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase


class CommentsPostService(private val post: Post) : BottomSheetDialogFragment() {

    private lateinit var database: DatabaseReference
    private lateinit var db: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var pet = Pet()
    private var comentario = Comentario()

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var v = inflater.inflate(R.layout.modal_comments_pets_post, container, false)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        db = Firebase.database.reference

        val myPreferences = ProjectPreferences(view.context)

        var arraylist = ArrayList<Comentario>()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    arraylist.clear()
                    for (i in 1 until dataSnapshot.childrenCount + 1) {
                        if(dataSnapshot.hasChild(i.toString())) {
                            var rootNodeComment = dataSnapshot.child(i.toString()).child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome).value as HashMap<String, HashMap<String, HashMap<String, String>>>
                            val idOnwer = rootNodeComment.keys.toMutableList()[0]
                            val idPet = rootNodeComment[idOnwer]?.keys?.toMutableList()?.get(0)

                            comentario = (dataSnapshot.child(i.toString())
                                .child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome)
                                .child(idOnwer)
                                .child(idPet.toString())
                                .getValue<Comentario>())!!
                            if(comentario.comentarioAtivo) {
                                arraylist.add(comentario)
                            }
                        }
                    }
                    if(arraylist.size != 0) {
                        val recyclerView = view.findViewById(R.id.recycler_comments_post) as? RecyclerView
                        recyclerView?.layoutParams!!.height = 700

                        recyclerView!!.layoutManager = LinearLayoutManager(activity)
                        recyclerView.adapter = CommentsPostAdapter(arraylist, view.context, post)

                        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                        recyclerView.layoutManager = layoutManager
                    }
                }

                override fun onCancelled(errorDb: DatabaseError) {
                    println(errorDb.toString())
                }

            })
        val ivIconPostComment : ImageButton = view.findViewById(R.id.bt_post_comment)
        val tvComment : EditText = view.findViewById(R.id.et_input_comment)
        ivIconPostComment.visibility = View.GONE
        tvComment.layoutParams.width =  ViewGroup.LayoutParams.MATCH_PARENT

        tvComment.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                if (s.length == 0) {
                    ivIconPostComment.visibility = View.GONE
                    tvComment.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                } else {
                    ivIconPostComment.visibility = View.VISIBLE
                    tvComment.layoutParams.width = (377 * context?.resources?.displayMetrics!!.density).toInt()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        ivIconPostComment.setOnClickListener {
            comentario.dataHora = DateUtils.dataFormatWithMilliseconds()
            comentario.idOwner = auth.uid.toString()
            comentario.idPet = myPreferences.getPetLogged().toString()
            comentario.textoComentario = tvComment.text.toString()
            comentario.comentarioAtivo = true

            db.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(post.idOwner)
                .child(post.idPet)
                .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                .child(post.idPost)
                .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        comentario.idComentario = (snapshot.childrenCount + 1).toString()
                        db.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                            .child(post.idOwner)
                            .child(post.idPet)
                            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                            .child(post.idPost)
                            .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
                            .child(comentario.idComentario)
                            .child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome)
                            .child(auth.uid.toString())
                            .child(myPreferences.getPetLogged().toString())
                            .setValue(comentario)

                        //Adicionar o comentário ao RecyclerView e atualizá-lo
                        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view!!.getWindowToken(), 0)

                        tvComment.text.clear()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.toString())
                    }

                })
        }
    }
}