package com.example.aidlserver

import androidx.room.Room

class Application : android.app.Application() {
    companion object{
        lateinit var database: TaskDatabase
    }

    override fun onCreate(){
        super.onCreate()
        database = Room.databaseBuilder(applicationContext, TaskDatabase::class.java, "task_database").build()
    }
}