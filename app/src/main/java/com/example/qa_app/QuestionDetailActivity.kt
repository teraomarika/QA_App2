package com.example.qa_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_question_detail.*

import java.util.HashMap

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    val user:FirebaseUser? = null
    private lateinit var toastButton: Button
    var mAuthListenr : FirebaseAuth.AuthStateListener? = null
    //private var toastButton: Button? = null
    var checkFlag: Boolean = false
    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            listView.adapter = mAdapter
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }


    override fun onResume() {
        super.onResume()
        setContentView(R.layout.activity_question_detail)
        var toastButton: Button = findViewById(R.id.show_toast_button)

        if (checkFlag == false) {
            checkFlag = true
            Toast.makeText(this, "テスメッセージです", Toast.LENGTH_SHORT).show()
            toastButton.setBackgroundColor(Color.rgb(0, 204, 255))
            toastButton.text = "お気に入り登録を外す"
            toastButton.shadowRadius
        } else {
            checkFlag = false
            Toast.makeText(this, "テトメッセージです2", Toast.LENGTH_SHORT).show()
            toastButton.setBackgroundColor(Color.rgb(192, 192, 192))
            toastButton.text = "お気に入り登録をする"
            toastButton.shadowRadius
        }
        var toastButton1:Button = findViewById(R.id.show_toast_button)
        // 渡ってきたQuestionのオブジェクトを保持する
        var extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()


        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する

            var user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                toastButton1.isEnabled = true
                toastButton1.text = "お気に入り登録をする"
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // ---  ここまで ---
            }
        }



        val dataBaseReference = FirebaseDatabase.getInstance().reference

        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var user = FirebaseAuth.getInstance().currentUser



        setContentView(R.layout.activity_question_detail)





    }
}
