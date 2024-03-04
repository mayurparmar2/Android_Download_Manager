package com.furthergrow.android_download_manager.myProject

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import kotlinx.android.synthetic.main.activity_view.mainConstrain
import kotlinx.android.synthetic.main.activity_view.pdfView
import kotlinx.android.synthetic.main.activity_view.txtProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import java.io.File


class ActivityView : AppCompatActivity() {
    var downloadId: Long? = -1L

    //    private val handler: Handler = Handler()
    private lateinit var updateJob: Job

    private var isDownloadComplete = false
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(com.furthergrow.android_download_manager.R.layout.activity_view)

        intent?.let {
            downloadId = intent.getLongExtra("downloadId", -1)
            if (downloadId != -1L) {
                registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                updateJob = CoroutineScope(Dispatchers.Main).launch {
                    while (!isDownloadComplete) {
                        updateDownloadProgress()
                        delay(1000) // Update every second
                    }
                }
            }
        }


    }

    val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val receivedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (receivedDownloadId == downloadId) {
                isDownloadComplete = true;
            }
        }
    }

    @SuppressLint("Range", "SetTextI18n")
    private fun updateDownloadProgress() {
        // Use the DownloadManager to get progress information
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId!!)
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS) as Int)
            Log.e("DownloadStatus", "Status: $status")
            if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {

                val downloaded_path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                mainConstrain.visibility = View.INVISIBLE
                pdfView.visibility = View.VISIBLE
                val file = File(getFilePathFromUri(this@ActivityView, Uri.parse(downloaded_path)))

                this.pdfView.fromFile(file).defaultPage(0).enableSwipe(true).swipeHorizontal(false)
                    .enableAnnotationRendering(true).scrollHandle(DefaultScrollHandle(this))
                    .autoSpacing(true).pageSnap(false).pageFling(false)
                    .fitEachPage(false)
                    .onPageChange(OnPageChangeListener { page, pageCount ->
//                            textView.text = (page + 1).toString() + " / " + pageCount
                    }).scrollHandle(null).enableAntialiasing(true).load()

                isDownloadComplete = true

//                pdfView.fromFile(pdfFile).load()
            } else {
                val progress =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                // Update UI with progress information
                val percentage = (progress * 100L / total).toInt()
                txtProgress.text = "Download Progress: $percentage%"
            }
        }
        cursor.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        updateJob.cancel()
    }

    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        var filePath: String? = null
        if ("content" == uri.scheme) {
            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            filePath = cursor.getString(columnIndex)
            cursor.close()
        } else if ("file" == uri.scheme) {
            filePath = File(uri.path).absolutePath
        }
        return filePath
    }
}