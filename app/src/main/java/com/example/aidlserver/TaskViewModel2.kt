package com.example.aidlserver

import kotlinx.coroutines.*

class TaskViewModel2(private val dao: TaskDao) {
    private var taskList = listOf<Task>()

    init {
        loadTaskList()
    }

    fun getTaskList(): List<Task>{
        return taskList
    }

    private fun updateTaskList(newList: MutableList<Task>){
        taskList = newList.toList()
    }

    fun loadTaskList(){
        val mutableTaskList = mutableListOf<Task>()
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO){
                dao.getAll().forEach{ task ->
                    mutableTaskList.add(task)
                }
            }
            updateTaskList(mutableTaskList)
        }
    }

    fun saveTask(task: Task){
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO){
                dao.insert(task)
            }
            loadTaskList()
        }
    }

    fun deleteTask(task: Task){
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO){
                dao.delete(task)
            }
            loadTaskList()
        }
    }
}