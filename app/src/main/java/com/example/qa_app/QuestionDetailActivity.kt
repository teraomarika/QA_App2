package com.example.qa_app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
import android.provider.MediaStore
import java.net.URI


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
            Log.d("aqaqaqaqaqaqa","aqaqqqaqaqaqaqaqa")
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
            val map = dataSnapshot.value as Map<String, String>
            Log.d("っっっっっ","aqaqqqaqaqaqaqaqa")
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

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var user1 = FirebaseAuth.getInstance().currentUser
        setContentView(R.layout.activity_question_detail)
        var toastButton: Button = findViewById(R.id.show_toast_button)

        //val dataBaseReference = FirebaseDatabase.getInstance().reference

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        var extras = intent.extras

        mQuestion = extras.get("question") as Question
        val testRef = dataBaseReference.child("favorite").child(user1?.uid.toString()).child(mQuestion.questionUid)
        var bbb = Uri.parse(testRef.toString())
        Log.d("bbb",bbb.toString())
        Log.d("bbb",mQuestion.questionUid)
        if(bbb.path.contains(mQuestion.questionUid)) {
            Log.d("aaa","あり")

        } else {
            Log.d("aaa","なし")
        }



        toastButton.setBackgroundColor(Color.rgb(192, 192, 192))
        toastButton.text = "お気に入り登録をする"
        toastButton.setOnClickListener() {

            if (checkFlag == false) {
                checkFlag = true
                // お気に入りに登録
                var user1 = FirebaseAuth.getInstance().currentUser
                val dataBaseReference = FirebaseDatabase.getInstance().reference
                val testRef = dataBaseReference.child("favorite").child(user1!!.uid.toString()).child(mQuestion.questionUid)
                Log.d("xxx",user1!!.uid.toString())
                testRef.setValue(mQuestion.questionUid)
                Toast.makeText(this, "テスメッセージです", Toast.LENGTH_SHORT).show()
                toastButton.setBackgroundColor(Color.rgb(0, 204, 255))
                toastButton.text = "お気に入り登録を外す"
                toastButton.shadowRadius
            } else {
                checkFlag = false
                // お気に入りから外す
                Toast.makeText(this, "テトメッセージです2", Toast.LENGTH_SHORT).show()
                toastButton.setBackgroundColor(Color.rgb(192, 192, 192))
                toastButton.text = "お気に入り登録をする"
                toastButton.shadowRadius
            }
        }
        // 渡ってきたQuestionのオブジェクトを保持する

        println(mQuestion.questionUid)
        //val data = HashMap<String, String>()
        //data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid
        //data.setValue(mQuestion.questionUid)
        Log.d("ccccc", "cccc")
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
                //toastButton1.isEnabled = true
                //toastButton1.text = "お気に入り登録をする"
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // ---  ここまで ---
            }
        }





        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)


    }

    fun getRealPathFromURI(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(uri, projection, null, null, null)
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }
}