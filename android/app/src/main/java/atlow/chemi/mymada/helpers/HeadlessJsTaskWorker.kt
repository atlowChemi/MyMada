package atlow.chemi.mymada.helpers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import atlow.chemi.mymada.recieversAndServices.HeadlessJsService
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HeadlessJsTaskWorker(appContext: Context, workerParameters: WorkerParameters) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
//        delay(500) // delaying because sometimes when the app is being backgrounded, isAppInForeground still returns true

        // Headless JS task crashes if in foreground
//        if (isAppInForeground(applicationContext)) {
//            Log.i("BackgroundTasksModule", "App is currently in the foreground, will not start background upload task")
//            return Result.success()
//        }

        Log.i("BackgroundTasksModule", "App is not in the foreground, will attempt to run headless js task")
        return runHeadlessJsTask()
    }

    private suspend fun runHeadlessJsTask(): Result {

        val intent = Intent(applicationContext, HeadlessJsService::class.java)

        var type = inputData.getString("type");
        if (type.isNullOrEmpty()) {
            type = "appStart"
        }
        intent.putExtra("type", type)
        intent.putExtra("from", "worker")


        Log.i("BackgroundTasksModule", "Will start executing Headless JS BackgroundUploadTaskService service")
        suspendCoroutine { continuation ->
            var hasUnbound = false // needed because sometimes onAfterServiceDone is called more than once
            val connection = object : ServiceConnection {
                private fun onAfterServiceDone(fromCallback: String) {
                    if (hasUnbound) {
                        Log.i("BackgroundTasksModule", "$fromCallback - Will not unbind from headless js service because already unbound previously")
                        return
                    }
                    hasUnbound = true

                    Log.i("BackgroundTasksModule", "$fromCallback - Will unbind from headless js service")
                    applicationContext.unbindService(this)
                    continuation.resume(null)
                }

                override fun onServiceConnected(className: ComponentName, service: IBinder) {
                    Log.d("BackgroundTasksModule", "Service connected")
                    val binder = service as HeadlessJsService.LocalBinder
                    binder.startRunning(intent) {
                        onAfterServiceDone("onServiceConnected:startRunning:onFinish")
                    }
                }

                override fun onServiceDisconnected(arg0: ComponentName) {
                    onAfterServiceDone("onServiceDisconnected")
                }
            }

            Log.d("BackgroundTasksModule", "Will bind to service")
            applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        Log.d("BackgroundTasksModule", "Will return from BackgroundUploadTaskWorker")
        return Result.success()
    }

//    private fun isAppInForeground(context: Context): Boolean {
//        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val appProcesses = activityManager.runningAppProcesses ?: return false
//        val packageName = context.packageName
//        for (appProcess in appProcesses) {
//            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
//                return true
//            }
//        }
//        return false
//    }
}