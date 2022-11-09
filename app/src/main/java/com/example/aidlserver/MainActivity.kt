package com.example.aidlserver

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.KeyEventDispatcher.Component
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room

private const val BUMP_MSG = 1

class MainActivity : AppCompatActivity() {

    /** The primary interface we will be calling on the service.  */
    private var mService: IRemoteService? = null

    private lateinit var killButton: Button
    private lateinit var callbackText: TextView
    private lateinit var adapter: Adapter

    private var isBound: Boolean = false

    /**
     * Class for interacting with the main interface of the service.
     */
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder){
            mService = IRemoteService.Stub.asInterface(service)
            killButton.isEnabled = true
            callbackText.text = "Attached."

            Log.d("debug", "サービスがコネクトされた")

            // As part of the sample, tell the user what happened.
            Toast.makeText(
                this@MainActivity,
                "R.string.remote_service_connected",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null
            killButton.isEnabled = false
            callbackText.text = "Disconnected."

            // As part of the sample, tell the user what happened.
            Toast.makeText(
                this@MainActivity,
                "R.string.remote_service_disconnected",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val mBindListener = View.OnClickListener {
        val intent = Intent(this@MainActivity, RemoteService::class.java)
        intent.action = IRemoteService::class.java.name
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        isBound = true
        callbackText.text = "Binding."
    }

    private val unbindListener = View.OnClickListener {
        if (isBound) {
            // Detach our existing connection.
            unbindService(mConnection)
            killButton.isEnabled = false
            isBound = false
            callbackText.text = "Unbinding."
        }
    }

    private val testProcess = View.OnClickListener{
        if(isBound){
            getTasksFromAidl()
            mService?.basicTypes(334, 2000000000000000000L, true, 33.4F, 33.4, "な阪関無")
        }
    }

    private val killListener = View.OnClickListener {
        try {
            mService?.pid()?.also { pid ->
                Process.killProcess(pid)
                callbackText.text = "Killed service process."
            }
        } catch (ex: RemoteException) {
            // Recover gracefully from the process hosting the
            // server dying.
            // Just for purposes of the sample, put up a notification.
            Toast.makeText(this@MainActivity, "リモートコール失敗", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTasksFromAidl(){
        val tasks = mService?.getTasks()
        var list = listOf<Task>()
        tasks?.forEach{ task ->
//            list.add( Task(0, task))
            Log.d("debug", "gotten task: $task")
        }

    }

    // ----------------------------------------------------------------------
    // Code showing how to deal with callbacks.
    // ----------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var bindProcessButton: Button = findViewById(R.id.bind_process)
        bindProcessButton.setOnClickListener(mBindListener)
        var unbindProcessButton: Button = findViewById(R.id.unbind_process)
        unbindProcessButton.setOnClickListener(unbindListener)
        killButton = findViewById(R.id.kill_process)
        killButton.setOnClickListener(killListener)
        killButton.isEnabled = false

        var testProcessButton: Button = findViewById(R.id.test_process)
        testProcessButton.setOnClickListener(testProcess)

        callbackText = findViewById(R.id.callback_text)
        callbackText.text = "Not Attached."

        /**
         * DB利用開始
         */

        val taskViewModel = TaskViewModel()
        // ダミーデータの挿入
        taskViewModel.saveTask(Task(0, "カレーを炊く"))
        taskViewModel.saveTask(Task(0, "大学に行く"))

        Handler(Looper.getMainLooper()).postDelayed({
            val tasks = taskViewModel.getTaskList()
            Log.d("debug", "task size: ${tasks.size}")

            val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

            adapter = Adapter(tasks)
            recyclerView.adapter = adapter

            val layoutmanager = LinearLayoutManager(this)
            recyclerView.layoutManager = layoutmanager
        }, 200)
    }
}

class Adapter(val data: List<Task>): RecyclerView.Adapter<Adapter.ViewHolder>(){

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val idTextView: TextView
        val taskTextView: TextView
        init {
            idTextView = view.findViewById(R.id.id)
            taskTextView = view.findViewById(R.id.task)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.idTextView.text = item.id.toString()
        holder.taskTextView.text = item.task
    }

    override fun getItemCount(): Int {
        return data.size
    }
}