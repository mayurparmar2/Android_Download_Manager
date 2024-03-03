package com.furthergrow.android_download_manager.myProject

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.furthergrow.android_download_manager.DownloadModel
import com.furthergrow.android_download_manager.R
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_pdfdawnloadlist.recyclerView
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

class PdfDownloadList() : AppCompatActivity(){
    val dModellist: ArrayList<DModel> = ArrayList()
    var downloadAdpater: DownloadAdpater? = null
    var realm: Realm? = null

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_pdfdawnloadlist)

        try {
            realm = Realm.getDefaultInstance()
            registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            downloadAdpater = DownloadAdpater(
                applicationContext,
                dModellist,
                object : DownloadAdpater.OnClickListenerMe {
                    override fun onClick() {

                    }
                })


            val downloadModelsLocal: List<DModel>? = getAllDownloads()
            downloadModelsLocal?.let {
                if (downloadModelsLocal.size > 0) {
                    dModellist.addAll(it)
                    for (i in dModellist.indices) {
                        if (dModellist.get(i)
                                .status.equals("Pending", ignoreCase = true) || dModellist.get(i)
                                .status.equals("Running", ignoreCase = true) || dModellist.get(i)
                                .status.equals("Downloading", ignoreCase = true)) {
                            val downloadStatusTask = DownloadStatusTask(dModellist.get(i))
                            runTask(downloadStatusTask, "" + dModellist.get(i).downloadId)
                        }
                    }
                }
            }

//            val manager = LinearLayoutManager(this)
//            manager.orientation = LinearLayoutManager.VERTICAL
//            recyclerView.layoutManager = manager
            recyclerView.adapter = downloadAdpater
            downloadFile("https://files.testfile.org/PDF/100MB-TESTFILE.ORG.pdf")
            downloadFile("https://files.testfile.org/PDF/100MB-TESTFILE.ORG.pdf")
            downloadFile("https://files.testfile.org/PDF/100MB-TESTFILE.ORG.pdf")
        } catch (e: Exception) {
            Log.e("mTAG", "onCreate: "+e)
        }

    }
    private fun getAllDownloads(): RealmResults<DModel>? {
        val realm = Realm.getDefaultInstance()
        return realm.where(DModel::class.java).findAll()
    }

    var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            downloadAdpater?.let {
                if (it.ChangeItemWithStatus("Completed", id)) {
                    val query = DownloadManager.Query()
                    query.setFilterById(id)
                    val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                    val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id))
                    cursor.moveToFirst()
                    val downloaded_path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    downloadAdpater!!.setChangeItemFilePath(downloaded_path, id)
                }
            }
        }
    }

    private fun downloadFile(url: String) {
        val filename = URLUtil.guessFileName(url, null, null)
        val downloadPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        val file = File(downloadPath, filename)
        var request: DownloadManager.Request? = null
        request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DownloadManager.Request(Uri.parse(url))
                .setTitle(filename)
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(file))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        } else {
            DownloadManager.Request(Uri.parse(url))
                .setTitle(filename)
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(file))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        }
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        val currentnum: Number? = realm!!.where<DModel>(DModel::class.java).max("id")
        val nextId: Int
        nextId = if (currentnum == null) {
            1
        } else {
            currentnum.toInt() + 1
        }
        val model = DModel()
        model.id = nextId.toLong()
        model.status = "Downloading"
        model.title = filename
        model.file_size = "0"
        model.progress = "0"
        model.isIs_paused = false
        model.downloadId = downloadId
        model.file_path = ""
        dModellist.add(model)
        downloadAdpater?.notifyItemInserted(dModellist!!.size - 1)
        realm!!.executeTransaction(Realm.Transaction { realm -> realm.copyToRealm(model) })
        val downloadStatusTask = DownloadStatusTask(model)
        runTask(downloadStatusTask, "" + downloadId)
    }

    fun runTask(downloadStatusTask: DownloadStatusTask, id: String) {
        try {
            downloadStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *arrayOf(id))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    inner class DownloadStatusTask(private val dModel: DModel) :
        AsyncTask<String, String, String>() {

        override fun doInBackground(vararg strings: String?): String? {
            strings[0]?.let { downloadFileProcess(it) }
            return null
        }

        private fun downloadFileProcess(downloadId: String?) {
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query()
                query.setFilterById(java.lang.Long.parseLong(downloadId!!))
                val cursor = downloadManager.query(query)
                cursor.moveToFirst()

                val bytesDownloaded =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val totalSize =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }

                val progress = ((bytesDownloaded * 100L) / totalSize).toInt()
                val status = getStatusMessage(cursor)
                publishProgress(progress.toString(), bytesDownloaded.toString(), status)
                cursor.close()
            }
        }

        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)
            realm?.executeTransaction { realm ->
                dModel.file_size = formatFileSize(values[1]?.toLong() ?: 0)
                dModel.progress = values[0]
                if (!dModel.status.equals("PAUSE", ignoreCase = true) &&
                    !dModel.status.equals("RESUME", ignoreCase = true)
                ) {
                    dModel.status = values[2]
                }
                downloadAdpater?.changeItem(dModel.downloadId)
            }
        }
    }

    fun formatFileSize(size: Long): String? {
        if (size <= 0) {
            return "0 B"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }



    private fun getStatusMessage(cursor: Cursor): String? {
        var msg = "-"
        msg = when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_FAILED -> "Failed"
                DownloadManager.STATUS_PAUSED -> "Paused"
                DownloadManager.STATUS_RUNNING -> "Running"
                DownloadManager.STATUS_SUCCESSFUL -> "Completed"
                DownloadManager.STATUS_PENDING -> "Pending"
                else -> "Unknown"
            }
        return msg
    }
}