package com.etesync.journalmanager.model

import com.etesync.journalmanager.Constants
import com.etesync.journalmanager.JournalManager
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import java.io.Serializable

open class CollectionInfo : Serializable {
    // FIXME: Shouldn't be exposed, as it's already saved in the journal. We just expose it for when we save for db.
    @Expose
    var version = -1

    @Expose
    var type: String? = null

    var uid: String? = null

    @Expose
    var displayName: String? = null
    @Expose
    var description: String? = null
    @Expose
    var color: Int? = null

    @Expose
    var timeZone: String? = null

    @Expose
    var selected: Boolean = false

    init {
        version = Constants.CURRENT_VERSION
    }

    fun updateFromJournal(journal: JournalManager.Journal) {
        uid = journal.uid!!
        version = journal.version
    }

    fun toJson(): String {
        return GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this, CollectionInfo::class.java)
    }

    override fun toString(): String {
        return "CollectionInfo(version=" + this.version + ", type=" + this.type + ", uid=" + this.uid + ", displayName=" + this.displayName + ", description=" + this.description + ", color=" + this.color + ", timeZone=" + this.timeZone + ", selected=" + this.selected + ")"
    }

    companion object {
        fun fromJson(json: String): CollectionInfo {
            return GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(json, CollectionInfo::class.java)
        }
    }
}
