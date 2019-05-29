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
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.support.design.widget.Snackbar
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Base64  //追加する
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import kotlinx.android.synthetic.*


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
    private var mGenreRef: DatabaseReference? = null
    var user1 = FirebaseAuth.getInstance().currentUser
    //private lateinit var modoruButton: Button

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            Log.d("testtest", "koko")
            if (mGenre != 99) {
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
            } else {
                val map = dataSnapshot.value
                val keydata = dataSnapshot.key
                Log.d("key",keydata.toString())
                Log.d("map",map.toString())
                val favorite = Favorite(
                    keydata.toString(), map.toString()
                )
                mFavoriteArrayList.add(favorite)
                mAdapter2.notifyDataSetChanged()
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
            //} else {
            //setContentView(R.layout.activity_favorite)
        //}
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


            if (user1 == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
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
            //drawer.addDrawerListener(toggle)
        Log.d("addDrawerListener",toggle.toString())

        //drawer.addDrawer
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // --- ここから ---
        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備

            mListView = findViewById(R.id.listView)

            //mListView2 = findViewById(R.id.listView2)

            mAdapter = QuestionsListAdapter(this)
            mAdapter2 = FavoriteListAdapter(this)
            mQuestionArrayList = ArrayList<Question>()
            mFavoriteArrayList = ArrayList<Favorite>()
            Log.d("aaa", mQuestionArrayList.toString())

            // --- ここまで追加する ---
            mListView.setOnItemClickListener { parent, view, position, id ->
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Log.d("kidou",mQuestionArrayList[position].toString())
                Log.d("kidou2",mQuestionArrayList[position].uid.toString())
                val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
                intent.putExtra("question", mQuestionArrayList[position])
                startActivity(intent)
            }

            Log.d("else","else")

        mAdapter.notifyDataSetChanged()
        mAdapter2.notifyDataSetChanged()

    }

    override fun onResume() {
        super.onResume()
        if(user1 != null) {
            mNavigationView = findViewById(R.id.nav_view)
            val menuNav = mNavigationView.menu
            val tmp = menuNav.findItem(R.id.nav_favorite)
            tmp.setVisible(true)
        }
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        // 1:趣味を既定の選択とする
        if (mGenre == 0) {
            Log.d("orusu","orusu")
            onNavigationItemSelected(navigationView.menu.getItem(0))
        }
        if(mGenre == 99) {
            Log.d("else","else")
            //setContentView(R.layout.activity_favorite)
            onNavigationItemSelected(navigationView.menu.getItem(99))

            //mListView2 = findViewById(R.id.listView2)

            //mAdapter2 = FavoriteListAdapter(this)
            //mFavoriteArrayList = ArrayList<Favorite>()
            //mAdapter2.notifyDataSetChanged()
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
        Log.d("onNavigationItemSele",mGenre.toString())
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

        // --- ここから ---
        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
            mQuestionArrayList.clear()
            mFavoriteArrayList.clear()
            mAdapter.setQuestionArrayList(mQuestionArrayList)
            mAdapter2.setFavoriteArrayList(mFavoriteArrayList)
            if(mGenre != 99) {
                mListView.adapter = mAdapter
            } else {
                setContentView(R.layout.content_main2)
                mListView2 = findViewById(R.id.listView2)
                //mListView.adapter = mAdapter2
                //mListView2 = findViewById(R.id.listView3)
                mListView2.adapter = mAdapter2
                mListView2.setOnItemClickListener { parent, view, position, id ->
                    // Questionのインスタンスを渡して質問詳細画面を起動する
                    Log.d("kidou","きどう")
                    val intent = Intent(applicationContext, FavoriteDetailActivity::class.java)
                    Log.d("kata",mFavoriteArrayList[position].id.toString())
                    intent.putExtra("favorite", mFavoriteArrayList[position].id)
                    startActivity(intent)
                }


                var modoruButton: Button = findViewById(R.id.modoru_button)
                modoruButton.setOnClickListener() {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                }

            }



            // 選択したジャンルにリスナーを登録する
            if (mGenreRef != null) {
                mGenreRef!!.removeEventListener(mEventListener)
            }
            if (mGenre == 99) {
                mGenreRef = mDatabaseReference.child("favorite").child(user!!.uid)
                Log.d("eee", user.uid.toString())
                mGenreRef!!.addChildEventListener(mEventListener)
            } else {
                mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
                mGenreRef!!.addChildEventListener(mEventListener)
            }
            // --- ここまで追加する ---

        return true
    }


}