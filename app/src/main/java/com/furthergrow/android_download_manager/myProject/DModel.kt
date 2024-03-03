package com.furthergrow.android_download_manager.myProject

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
open class DModel : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var downloadId: Long = 0
    var title: String? = null
    var file_path: String? = null
    var progress: String? = null
    var status: String? = null
    var file_size: String? = null
    var isIs_paused:Boolean = false
}
