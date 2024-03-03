package com.furthergrow.android_download_manager.myProject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.furthergrow.android_download_manager.R
import io.realm.Realm

class DownloadAdpater(var context: Context,var list:ArrayList<DModel>,var onClick:OnClickListenerMe):RecyclerView.Adapter<DownloadAdpater.ViewHolder>(){


    interface OnClickListenerMe {
        fun onClick()
    }
    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
         var status: TextView
         var txtTitle: TextView
         var txtpercentage: TextView
         var progressBar: ProgressBar
        init {
            txtTitle = itemView.findViewById<TextView>(R.id.textView)
            status = itemView.findViewById<TextView>(R.id.textView1)
            txtpercentage = itemView.findViewById<TextView>(R.id.textView2)
            progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View = LayoutInflater.from(parent.context).inflate(R.layout.item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        holder.txtTitle.text = model.title
        holder.status.text = model.status
        holder.progressBar.progress = model.progress!!.toInt()


    }
    fun changeItem(downloadid: Long) {
        var i = 0
        for (model in list) {
            if (downloadid == model.downloadId) {
                notifyItemChanged(i)
            }
            i++
        }
    }

    fun ChangeItemWithStatus(message: String?, downloadid: Long): Boolean {
        var comp = false
        var i = 0
        for (downloadModel in list) {
            if (downloadid == downloadModel.downloadId) {
                val realm = Realm.getDefaultInstance()
                val finalI = i
                realm.executeTransaction { realm ->
                    list.get(finalI).status = message
                    notifyItemChanged(finalI)

                    realm.copyToRealmOrUpdate<DModel>(list.get(finalI))
                }
                comp = true
            }
            i++
        }
        return comp
    }

    fun setChangeItemFilePath(path: String?, id: Long) {
        val realm = Realm.getDefaultInstance()
        var i = 0
        for (downloadModel in list) {
            if (id == downloadModel.downloadId) {
                val finalI = i
                realm.executeTransaction { realm ->
                    list.get(finalI).file_path = path
                    notifyItemChanged(finalI)
                    realm.copyToRealmOrUpdate<DModel>(list.get(finalI))
                }
            }
            i++
        }
    }
}
//
//class DownloadAdapter(
//    var context: Context,
//    downloadModels: List<DownloadModel>,
//    var clickListener: ItemClickListener
//) :
//    RecyclerView.Adapter<ViewHolder>() {
//    var downloadModels: List<DownloadModel> = ArrayList()
//
//    init {
//        this.downloadModels = downloadModels
//    }
//
//    inner class DownloadViewHolder(itemView: View) : ViewHolder(itemView) {
//        var file_title: TextView
//        var file_size: TextView
//        var file_progress: ProgressBar
//        var pause_resume: Button
//        var sharefile: Button
//        var file_status: TextView
//        var main_rel: RelativeLayout
//
//        init {
//            file_title = itemView.findViewById<TextView>(R.id.file_title)
//            file_size = itemView.findViewById<TextView>(R.id.file_size)
//            file_status = itemView.findViewById<TextView>(R.id.file_status)
//            file_progress = itemView.findViewById<ProgressBar>(R.id.file_progress)
//            pause_resume = itemView.findViewById<Button>(R.id.pause_resume)
//            main_rel = itemView.findViewById<RelativeLayout>(R.id.main_rel)
//            sharefile = itemView.findViewById<Button>(R.id.sharefile)
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val vh: ViewHolder
//        val view: View =
//            LayoutInflater.from(parent.context).inflate(R.layout.download_row, parent, false)
//        vh = DownloadViewHolder(view)
//        return vh
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
//        val downloadModel = downloadModels[position]
//        val downloadViewHolder = holder as DownloadViewHolder
//        downloadViewHolder.file_title.text = downloadModel.title
//        downloadViewHolder.file_status.text = downloadModel.status
//        downloadViewHolder.file_progress.progress = downloadModel.progress.toInt()
//        downloadViewHolder.file_size.text = "Downloaded : " + downloadModel.file_size
//        if (downloadModel.isIs_paused) {
//            downloadViewHolder.pause_resume.text = "RESUME"
//        } else {
//            downloadViewHolder.pause_resume.text = "PAUSE"
//        }
//        if (downloadModel.status.equals("RESUME", ignoreCase = true)) {
//            downloadViewHolder.file_status.text = "Running"
//        }
//        downloadViewHolder.pause_resume.setOnClickListener {
//            if (downloadModel.isIs_paused) {
//                downloadModel.isIs_paused = false
//                downloadViewHolder.pause_resume.text = "PAUSE"
//                downloadModel.status = "RESUME"
//                downloadViewHolder.file_status.text = "Running"
//                if (!resumeDownload(downloadModel)) {
//                    Toast.makeText(context, "Failed to Resume", Toast.LENGTH_SHORT).show()
//                }
//                notifyItemChanged(position)
//            } else {
//                downloadModel.isIs_paused = true
//                downloadViewHolder.pause_resume.text = "RESUME"
//                downloadModel.status = "PAUSE"
//                downloadViewHolder.file_status.text = "PAUSE"
//                if (!pauseDownload(downloadModel)) {
//                    Toast.makeText(context, "Failed to Pause", Toast.LENGTH_SHORT).show()
//                }
//                notifyItemChanged(position)
//            }
//        }
//        downloadViewHolder.main_rel.setOnClickListener { clickListener.onCLickItem(downloadModel.file_path) }
//        downloadViewHolder.sharefile.setOnClickListener { clickListener.onShareClick(downloadModel) }
//    }
//
//    private fun pauseDownload(downloadModel: DownloadModel): Boolean {
//        var updatedRow = 0
//        val contentValues = ContentValues()
//        contentValues.put("control", 1)
//        try {
//            updatedRow = context.contentResolver.update(
//                Uri.parse("content://downloads/my_downloads"),
//                contentValues,
//                "title=?",
//                arrayOf(downloadModel.title)
//            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return 0 < updatedRow
//    }
//
//    private fun resumeDownload(downloadModel: DownloadModel): Boolean {
//        var updatedRow = 0
//        val contentValues = ContentValues()
//        contentValues.put("control", 0)
//        try {
//            updatedRow = context.contentResolver.update(
//                Uri.parse("content://downloads/my_downloads"),
//                contentValues,
//                "title=?",
//                arrayOf(downloadModel.title)
//            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return 0 < updatedRow
//    }
//
//    override fun getItemCount(): Int {
//        return downloadModels.size
//    }
//
//    fun changeItem(downloadid: Long) {
//        var i = 0
//        for (downloadModel in downloadModels) {
//            if (downloadid == downloadModel.downloadId) {
//                notifyItemChanged(i)
//            }
//            i++
//        }
//    }
//
//    fun ChangeItemWithStatus(message: String?, downloadid: Long): Boolean {
//        var comp = false
//        var i = 0
//        for (downloadModel in downloadModels) {
//            if (downloadid == downloadModel.downloadId) {
//                val realm = Realm.getDefaultInstance()
//                val finalI = i
//                realm.executeTransaction { realm ->
//                    downloadModels[finalI].status = message
//                    notifyItemChanged(finalI)
//                    realm.copyToRealmOrUpdate(downloadModels[finalI])
//                }
//                comp = true
//            }
//            i++
//        }
//        return comp
//    }
//
//    fun setChangeItemFilePath(path: String?, id: Long) {
//        val realm = Realm.getDefaultInstance()
//        var i = 0
//        for (downloadModel in downloadModels) {
//            if (id == downloadModel.downloadId) {
//                val finalI = i
//                realm.executeTransaction { realm ->
//                    downloadModels[finalI].file_path = path
//                    notifyItemChanged(finalI)
//                    realm.copyToRealmOrUpdate(downloadModels[finalI])
//                }
//            }
//            i++
//        }
//    }
//}
//
