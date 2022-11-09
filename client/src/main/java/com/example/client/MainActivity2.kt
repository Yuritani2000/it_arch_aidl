package com.example.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.os.RemoteException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aidlserver.IRemoteService

class MainActivity2 : AppCompatActivity() {

    /** The primary interface we will be calling on the service.  */
    private var mService: IRemoteService? = null

    private lateinit var killButton: Button
    private lateinit var callbackText: TextView

    private var isBound: Boolean = false

    private lateinit var adapter: Adapter

    /**
     * Class for interacting with the main interface of the service.
     */
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = IRemoteService.Stub.asInterface(service)
            killButton.isEnabled = true
            callbackText.text = "Attached."

            Log.d("debug", "サービスがコネクトされた")

            // As part of the sample, tell the user what happened.
            Toast.makeText(
                this@MainActivity2,
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
                this@MainActivity2,
                "R.string.remote_service_disconnected",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val mBindListener = View.OnClickListener {
        val intent = Intent("com.example.aidlserver.MY_SERVICE")
        intent.setPackage("com.example.aidlserver")
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

    private val testProcess = View.OnClickListener {
        if (isBound) {
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
            Toast.makeText(this@MainActivity2, "リモートコール失敗", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTasksFromAidl(){
        val tasks = mService?.getTasks()
        tasks?.let{ tasks ->
            adapter.data = tasks
            adapter.notifyDataSetChanged()
        }
    }

    // ----------------------------------------------------------------------
    // Code showing how to deal with callbacks.
    // ----------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

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

        //RecyclerViewの取得
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

        adapter = Adapter(mutableListOf())
        recyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
    }
}


class Adapter(var data: List<String>): RecyclerView.Adapter<Adapter.ViewHolder>(){

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
//        val idTextView: TextView
        val taskTextView: TextView
        init {
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
//        holder.idTextView.text = item.id.toString()
        holder.taskTextView.text = item
    }

    override fun getItemCount(): Int {
        return data.size
    }
}