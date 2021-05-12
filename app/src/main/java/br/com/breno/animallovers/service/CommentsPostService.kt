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
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.adapters.CommentsPostAdapter
import br.com.breno.animallovers.model.Comentario
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class CommentsPostService(private val post: Post, private val listComentario: ArrayList<Comentario>, context : Context) : BottomSheetDialogFragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var pet = Pet()
    private var comentario = Comentario()
    private var commentService = CommentsService(context)
    private var notificacaoService = NotificationService(context)

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.modal_comments_pets_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        val myPreferences = ProjectPreferences(view.context)

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(listComentario.isNullOrEmpty()) {
                        var arraylist = commentService.getAllComments(post, dataSnapshot)

                        if (arraylist.size != 0) {
                            val recyclerView = view.findViewById(R.id.recycler_comments_post) as? RecyclerView
                            recyclerView?.layoutParams!!.height = 700

                            recyclerView!!.layoutManager = LinearLayoutManager(activity)
                            recyclerView.adapter = CommentsPostAdapter(arraylist, view.context, post)

                            val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                            recyclerView.layoutManager = layoutManager
                        }
                    }
                    else {
                        val recyclerView = view.findViewById(R.id.recycler_comments_post) as? RecyclerView
                        recyclerView?.layoutParams!!.height = 700

                        recyclerView!!.layoutManager = LinearLayoutManager(activity)
                        recyclerView.adapter = CommentsPostAdapter(listComentario, view.context, post)

                        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                        recyclerView.layoutManager = layoutManager
                    }

                    val ivIconPostComment : ImageButton = view.findViewById(R.id.bt_post_comment)
                    val tvComment : EditText = view.findViewById(R.id.et_input_comment)
                    ivIconPostComment.visibility = View.GONE
                    tvComment.layoutParams.width =  ViewGroup.LayoutParams.MATCH_PARENT

                    tvComment.addTextChangedListener(object : TextWatcher {
                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                            if (s.isEmpty()) {
                                ivIconPostComment.visibility = View.GONE
                                tvComment.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            } else {
                                ivIconPostComment.visibility = View.VISIBLE
                                tvComment.layoutParams.width = (330 * context?.resources?.displayMetrics!!.density).toInt()
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

                        comentario = commentService.saveNewComment(post, comentario, dataSnapshot)

                        //Adicionar o comentário ao RecyclerView e atualizá-lo
                        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)

                        tvComment.text.clear()

                        if(comentario.idOwner != post.idOwner) {
                            notificacaoService.sendNotificationOfCommentInPost(post, comentario, dataSnapshot)
                        }
                    }
                }

                override fun onCancelled(errorDb: DatabaseError) {
                    println(errorDb.toString())
                }

            })
    }
}