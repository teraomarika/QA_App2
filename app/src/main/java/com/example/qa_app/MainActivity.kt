package com.example.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView

import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.support.design.widget.Snackbar
import android.util.Base64  //追加する
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.FILL_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.database.*
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mToolbar: Toolbar
    private var mGenre = 0

    // --- ここから ---
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mListView2: ListView
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mFavoriteArrayList: ArrayList<Favorite>
    private lateinit var mAdapter: QuestionsListAdapter
    private lateinit var mAdapter2: FavoriteListAdapter
    private lateinit var mNavigationView: NavigationView
    private lateinit var fab: FloatingActionButton
    var bbb:List<String> = mutableListOf()
    var ccc:Map<String,String> = mutableMapOf()
    private lateinit var params: FloatArray
    private var mGenreRef: DatabaseReference? = null

    var user1 = FirebaseAuth.getInstance().currentUser

    //private lateinit var modoruButton: Button
    //val overlayView: ViewGroup by lazy { LayoutInflater.from(this).inflate(R.layout.activity_main, null) as ViewGroup }

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            //if (mGenre != 99) {
            if(mGenre == 99) {
                Log.d("ccc", ccc.keys.toString())
                for (favorite_item in ccc.keys) {
                    if(favorite_item.toString() == dataSnapshot.key.toString()) {
                        val map = dataSnapshot.value as Map<String, String>
                        val title = map["title"] ?: ""
                        val body = map["body"] ?: ""
                        val name = map["name"] ?: ""
                        val uid = map["uid"] ?: ""
                        val imageString = map["image"] ?: ""
                        val bytes =
                            if (imageString.isNotEmpty()) {
                                Base64.decode(imageString, Base64.DEFAULT)
                            } else {
                                byteArrayOf()
                            }

                        val answerArrayList = ArrayList<Answer>()
                        val answerMap = map["answers"] as Map<String, String>?
                        if (answerMap != null) {
                            for (key in answerMap.keys) {
                                val temp = answerMap[key] as Map<String, String>
                                val answerBody = temp["body"] ?: ""
                                val answerName = temp["name"] ?: ""
                                val answerUid = temp["uid"] ?: ""
                                val answer = Answer(answerBody, answerName, answerUid, key)
                                answerArrayList.add(answer)
                            }
                        }

                        val question = Question(
                            title, body, name, uid, dataSnapshot.key ?: "",
                            mGenre, bytes, answerArrayList
                        )
                        mQuestionArrayList.add(question)
                        mAdapter.notifyDataSetChanged()
                    }
                }
            } else {
                val map = dataSnapshot.value as Map<String, String>
                val title = map["title"] ?: ""
                val body = map["body"] ?: ""
                val name = map["name"] ?: ""
                val uid = map["uid"] ?: ""
                val imageString = map["image"] ?: ""
                val bytes =
                    if (imageString.isNotEmpty()) {
                        Base64.decode(imageString, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }

                val answerArrayList = ArrayList<Answer>()
                val answerMap = map["answers"] as Map<String, String>?
                if (answerMap != null) {
                    for (key in answerMap.keys) {
                        val temp = answerMap[key] as Map<String, String>
                        val answerBody = temp["body"] ?: ""
                        val answerName = temp["name"] ?: ""
                        val answerUid = temp["uid"] ?: ""
                        val answer = Answer(answerBody, answerName, answerUid, key)
                        answerArrayList.add(answer)
                    }
                }

                val question = Question(
                    title, body, name, uid, dataSnapshot.key ?: "",
                    mGenre, bytes, answerArrayList
                )
                mQuestionArrayList.add(question)
                mAdapter.notifyDataSetChanged()
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            Log.d("onChildChanged",mGenre.toString())
            val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            for (question in mQuestionArrayList) {
                if (dataSnapshot.key.equals(question.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }
    // --- ここまで追加する ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //if(mGenre != 99) {
        setContentView(R.layout.activity_main)
        if(mGenre != 99) {
            fab = findViewById<FloatingActionButton>(R.id.fab)
        }

        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)


        fab.setOnClickListener { view ->
            // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
            if (mGenre == 0) {
                Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show()
            } else {
                Log.d("mGenreは", mGenre.toString())
            }
            // ログイン済みのユーザーを取得する




            if (user1 == null || mGenre != 99) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
                mAdapter.notifyDataSetChanged()
            } else {
                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
            }
        }

        // ナビゲーションドロワーの設定

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // --- ここから ---
        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備

            mListView = findViewById(R.id.listView)



            mAdapter = QuestionsListAdapter(this)
            mQuestionArrayList = ArrayList<Question>()


            // --- ここまで追加する ---
            mListView.setOnItemClickListener { parent, view, position, id ->
                // Questionのインスタンスを渡して質問詳細画面を起動する
                val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
                intent.putExtra("question", mQuestionArrayList[position])
                startActivity(intent)
            }

        mAdapter.notifyDataSetChanged()
        //mAdapter2.notifyDataSetChanged()

    }

    override fun onResume() {
        super.onResume()
        mNavigationView = findViewById(R.id.nav_view)
        mNavigationView.setNavigationItemSelectedListener(this)


        var user1 = FirebaseAuth.getInstance().currentUser
        if(user1 != null) {

            val menuNav = mNavigationView.menu
            val tmp = menuNav.findItem(R.id.nav_favorite)
            tmp.setVisible(true)
            // 即時反映したい
        } else {
            val menuNav = mNavigationView.menu
            val tmp = menuNav.findItem(R.id.nav_favorite)
            tmp.setVisible(false)
        }

        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        // 1:趣味を既定の選択とする
        if (mGenre == 0) {
            onNavigationItemSelected(navigationView.menu.getItem(0))
        }
        val favoriteRef = FirebaseAuth.getInstance().currentUser
        if(user1 != null) {
            var favoriteRef = mDatabaseReference.child("favorite").child(user1!!.uid)
            favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    ccc = p0.value as Map<String, String>
                }

            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val user = FirebaseAuth.getInstance().currentUser
        if (id == R.id.nav_hobby) {
            mToolbar.title = "趣味"
            mGenre = 1
        } else if (id == R.id.nav_life) {
            mToolbar.title = "生活"
            mGenre = 2
        } else if (id == R.id.nav_health) {
            mToolbar.title = "健康"
            mGenre = 3
        } else if (id == R.id.nav_compter) {
            mToolbar.title = "コンピューター"
            mGenre = 4
        } else if (id == R.id.nav_favorite) {
            if (user == null) {

                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
                mToolbar.title = "会員登録"
            } else {
                mToolbar.title = "お気に入り"
                mGenre = 99
            }
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)


            mQuestionArrayList.clear()
            mAdapter.setQuestionArrayList(mQuestionArrayList)
            //if(mGenre != 99) {
            mListView.adapter = mAdapter

            // 選択したジャンルにリスナーを登録する
            if (mGenreRef != null) {
                mGenreRef!!.removeEventListener(mEventListener)
            }
            if (mGenre == 99) {
                //mGenreRef = mDatabaseReference.child("favorite").child(user!!.uid)
                // お気に入り呼び出し
                val array: ArrayList<Int> = arrayListOf(0,1,2,3)
                for (i in array) {
                    mGenreRef = mDatabaseReference.child(ContentsPATH).child(i.toString())
                    mGenreRef!!.addChildEventListener(mEventListener)
                }




            //    mGenreRef!!.addChildEventListener(mEventListener)
            } else {
                mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
                mGenreRef!!.addChildEventListener(mEventListener)
            }
            // --- ここまで追加する ---

        return true
    }


}