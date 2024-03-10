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
import io.invertase.firebase.crashlytics.ReactNativeFirebaseCrashlyticsNativeHelper
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HeadlessJsTaskWorker(appContext: Context, workerParameters: WorkerParameters) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val intent = Intent(applicationContext, HeadlessJsService::class.java)

        var type = inputData.getString("type");
        if (type.isNullOrEmpty()) {
            type = "appStart"
        }
        intent.putExtra("type", type)
        intent.putExtra("from", "worker")
        inputData.keyValueMap.forEach {
            when (it.value) {
                is Int -> intent.putExtra(it.key, it.value as Int)
                is String -> intent.putExtra(it.key, it.value as String)
                is Boolean -> intent.putExtra(it.key, it.value as Boolean)
                else -> ReactNativeFirebaseCrashlyticsNativeHelper.recordNativeException(Exception("Bad value type sent to HeadlessJsTaskWorker"))
            }
        }

        Log.i("HeadlessJsTaskWorker", "Will start executing Headless JS HeadlessJsTaskWorker service")
        suspendCoroutine { continuation ->
            var hasUnbound = false // needed because sometimes onAfterServiceDone is called more than once
            val connection = object : ServiceConnection {
                private fun onAfterServiceDone(fromCallback: String) {
                    if (hasUnbound) {
                        Log.i("HeadlessJsTaskWorker", "$fromCallback - Will not unbind from headless js service because already unbound previously")
                        return
                    }
                    hasUnbound = true

                    Log.i("HeadlessJsTaskWorker", "$fromCallback - Will unbind from headless js service")
                    applicationContext.unbindService(this)
                    continuation.resume(null)
                }

                override fun onServiceConnected(className: ComponentName, service: IBinder) {
                    Log.d("HeadlessJsTaskWorker", "Service connected")
                    val binder = service as HeadlessJsService.LocalBinder
                    binder.startRunning(intent) {
                        onAfterServiceDone("onServiceConnected:startRunning:onFinish")
                    }
                }

                override fun onServiceDisconnected(arg0: ComponentName) {
                    onAfterServiceDone("onServiceDisconnected")
                }
            }

            Log.d("HeadlessJsTaskWorker", "Will bind to service")
            applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        Log.d("HeadlessJsTaskWorker", "Will return from BackgroundUploadTaskWorker")
        return Result.success()
    }
}