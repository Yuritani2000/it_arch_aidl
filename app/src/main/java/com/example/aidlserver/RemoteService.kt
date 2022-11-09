package com.example.aidlserver

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.util.Log
import androidx.room.Room

class RemoteService: Service() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder {
        // Return the interface
        Log.d("debug", "バインド完了")
        return binder
    }


    private val binder = object : IRemoteService.Stub() {
        override fun pid(): Int {
            return Process.myPid()
        }

        override fun getTasks(): List<String> {
//            val database = Room.databaseBuilder(applicationContext, TaskDatabase::class.java, "task_database").build()
//            val taskViewModel = TaskViewModel2(database.taskDao())
//            val mutableList = mutableListOf<String>()
//
//            return taskViewModel.getTaskList()
////            taskViewModel.getTaskList().forEach{ task ->
////                    task.task?.let { taskStr ->
////                        mutableList.add(taskStr)
////                    }
////            }
////            return mutableList
            return mutableListOf<String>("服を着替える", "大学に行く", "歯を磨く", "本を読む", "勉強をする")
        }

        override fun basicTypes(
            anInt: Int,
            aLong: Long,
            aBoolean: Boolean,
            aFloat: Float,
            aDouble: Double,
            aString: String
        ) {
            // Does nothing
            Log.d("debug", "anInt: ${anInt}")
            Log.d("debug", "aLong: ${aLong}")
            Log.d("debug", "aBoolean: ${aBoolean}")
            Log.d("debug", "aFloat: ${aFloat}")
            Log.d("debug", "aDouble: ${aDouble}")
            Log.d("debug", "aString: ${aString}")
        }
    }
}
