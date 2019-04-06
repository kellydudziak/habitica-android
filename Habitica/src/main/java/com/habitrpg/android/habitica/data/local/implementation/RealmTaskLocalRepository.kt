package com.habitrpg.android.habitica.data.local.implementation

import android.util.Log
import com.habitrpg.android.habitica.data.local.TaskLocalRepository
import com.habitrpg.android.habitica.models.tasks.*
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults

class RealmTaskLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), TaskLocalRepository {
    private val DEBUG_TAG = "RealmTaskLocalRepo";
    override fun getTasks(taskType: String, userID: String): Flowable<RealmResults<Task>> {
        Log.d(DEBUG_TAG, "CS215, in getTasks")

        return realm.where(Task::class.java)
                .equalTo("type", taskType)
                .equalTo("userId", userID)
                .sort("position")
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
                .retry(1)
    }

    override fun getTasks(userId: String): Flowable<RealmResults<Task>> {
        Log.d(DEBUG_TAG, "CS215, in getTasks")

        return realm.where(Task::class.java).equalTo("userId", userId)
                .sort("position")
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun saveTasks(userId: String, tasksOrder: TasksOrder, tasks: TaskList) {
        Log.d(DEBUG_TAG, "CS215, in saveTasks")

        val sortedTasks = ArrayList<Task>()
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.habits))
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.dailys))
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.todos))
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.rewards))
        removeOldTasks(userId, sortedTasks)

        val allChecklistItems = ArrayList<ChecklistItem>()
        sortedTasks.forEach { it.checklist?.let { it1 -> allChecklistItems.addAll(it1) } }
        removeOldChecklists(allChecklistItems)

        val allReminders = ArrayList<RemindersItem>()
        sortedTasks.forEach { it.reminders?.let { it1 -> allReminders.addAll(it1) } }
        removeOldReminders(allReminders)

        realm.executeTransactionAsync { realm1 -> realm1.insertOrUpdate(sortedTasks) }
    }

    override fun saveCompletedTodos(userId: String, tasks: MutableCollection<Task>) {
        Log.d(DEBUG_TAG, "CS215, in saveCompletedTodos")

        removeCompletedTodos(userId, tasks)
        realm.executeTransactionAsync { realm1 -> realm1.insertOrUpdate(tasks) }
    }

    private fun sortTasks(taskMap: MutableMap<String, Task>, taskOrder: List<String>): List<Task> {
        Log.d(DEBUG_TAG, "CS215, in sortTasks")

        val taskList = ArrayList<Task>()
        var position = 0
        for (taskId in taskOrder) {
            val task = taskMap[taskId]
            if (task != null) {
                task.position = position
                taskList.add(task)
                position++
                taskMap.remove(taskId)
            }
        }
        return taskList
    }

    private fun removeOldTasks(userID: String, onlineTaskList: List<Task>) {
        Log.d(DEBUG_TAG, "CS215, in removeOldTasks")

        val localTasks = realm.where(Task::class.java)
                .equalTo("userId", userID)
                .beginGroup()
                .beginGroup()
                .equalTo("type", Task.TYPE_TODO)
                .equalTo("completed", false)
                .endGroup()
                .or()
                .notEqualTo("type", Task.TYPE_TODO)
                .endGroup()
                .findAll()
                .createSnapshot()
        val tasksToDelete = localTasks.filterNot { onlineTaskList.contains(it) }
        realm.executeTransaction {
            for (localTask in tasksToDelete) {
                localTask.checklist?.deleteAllFromRealm()
                localTask.reminders?.deleteAllFromRealm()
                localTask.deleteFromRealm()
            }
        }
    }

    private fun removeCompletedTodos(userID: String, onlineTaskList: MutableCollection<Task>) {
        Log.d(DEBUG_TAG, "CS215, in removeCompletedTodos")

        val localTasks = realm.where(Task::class.java)
                .equalTo("userId", userID)
                .equalTo("type", Task.TYPE_TODO)
                .equalTo("completed", true)
                .findAll()
                .createSnapshot()
        val tasksToDelete = localTasks.filterNot { onlineTaskList.contains(it) }
        realm.executeTransaction {
            for (localTask in tasksToDelete) {
                localTask.checklist?.deleteAllFromRealm()
                localTask.reminders?.deleteAllFromRealm()
                localTask.deleteFromRealm()
            }
        }
    }

    private fun removeOldChecklists(onlineItems: List<ChecklistItem>) {
        Log.d(DEBUG_TAG, "CS215, in removeOldChecklists")

        val localItems = realm.where(ChecklistItem::class.java).findAll().createSnapshot()
        val itemsToDelete = localItems.filterNot { onlineItems.contains(it) }
        realm.executeTransaction {
            for (item in itemsToDelete) {
                item.deleteFromRealm()
            }
        }
    }

    private fun removeOldReminders(onlineReminders: List<RemindersItem>) {
        Log.d(DEBUG_TAG, "CS215, in removeOldReminders")

        val localReminders = realm.where(RemindersItem::class.java).findAll().createSnapshot()
        val itemsToDelete = localReminders.filterNot { onlineReminders.contains(it) }
        realm.executeTransaction {
            for (item in itemsToDelete) {
                item.deleteFromRealm()
            }
        }
    }

    override fun deleteTask(taskID: String) {
        Log.d(DEBUG_TAG, "CS215, in deleteTask")

        val task = realm.where(Task::class.java).equalTo("id", taskID).findFirstAsync()
        realm.executeTransaction {
            if (task.isManaged) {
                task.deleteFromRealm()
            }
        }
    }

    override fun getTask(taskId: String): Flowable<Task> {
        Log.d(DEBUG_TAG, "CS215, in getTask")

        return realm.where(Task::class.java).equalTo("id", taskId).findFirstAsync().asFlowable<RealmObject>()
                .filter { realmObject -> realmObject.isLoaded }
                .cast(Task::class.java)
    }

    override fun getTaskCopy(taskId: String): Flowable<Task> {
        Log.d(DEBUG_TAG, "CS215, in getTaskCopy")

        return getTask(taskId)
                .map { task ->
                    return@map if (task.isManaged && task.isValid) {
                         realm.copyFromRealm(task)
                    } else {
                         task
                    }
                }
    }

    override fun markTaskCompleted(taskId: String, isCompleted: Boolean) {
        Log.d(DEBUG_TAG, "CS215, in markTaskCompleted")

        val task = realm.where(Task::class.java).equalTo("id", taskId).findFirstAsync()
        realm.executeTransaction { task.completed = true }
    }

    override fun saveReminder(remindersItem: RemindersItem) {
        Log.d(DEBUG_TAG, "CS215, in saveReminder")

        realm.executeTransaction { it.insertOrUpdate(remindersItem) }
    }

    override fun swapTaskPosition(firstPosition: Int, secondPosition: Int) {
        val firstTask = realm.where(Task::class.java).equalTo("position", firstPosition).findFirst()
        val secondTask = realm.where(Task::class.java).equalTo("position", secondPosition).findFirst()
        if (firstTask != null && secondTask != null && firstTask.isValid && secondTask.isValid) {
            realm.executeTransaction {
                firstTask.position = secondPosition
                secondTask.position = firstPosition
            }
        }
    }

    override fun getTaskAtPosition(taskType: String, position: Int): Flowable<Task> {
        return realm.where(Task::class.java).equalTo("type", taskType).equalTo("position", position).findFirstAsync().asFlowable<RealmObject>()
                .filter { realmObject -> realmObject.isLoaded }
                .cast(Task::class.java)
    }

    override fun updateIsdue(daily: TaskList): Maybe<TaskList> {
        Log.d(DEBUG_TAG, "CS215, in updateIsdue")

        return Flowable.just(realm.where(Task::class.java).equalTo("type", "daily").findAll())
                .firstElement()
                .map { tasks ->
                    realm.beginTransaction()
                    tasks.filter { daily.tasks.containsKey(it.id) }.forEach { it.isDue = daily.tasks[it.id]?.isDue }
                    realm.commitTransaction()
                    daily
                }
    }

    override fun updateTaskPositions(taskOrder: List<String>) {
        if (taskOrder.isNotEmpty()) {
            val tasks = realm.where(Task::class.java).`in`("id", taskOrder.toTypedArray()).findAll()
            realm.executeTransaction { _ ->
                tasks.filter { taskOrder.contains(it.id) }.forEach { it.position = taskOrder.indexOf(it.id) }
            }
        }
    }

    override fun getErroredTasks(userID: String): Flowable<RealmResults<Task>> {
        return realm.where(Task::class.java)
                .equalTo("userId", userID)
                .equalTo("hasErrored", true)
                .sort("position")
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
                .retry(1)    }
}
