package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.getBoolean
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper.Companion.readOwner
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper.Companion.writeOwner
import dev.ragnarok.fenrir.module.parcel.ParcelNative
import dev.ragnarok.fenrir.putBoolean
import dev.ragnarok.fenrir.readTypedObjectCompat
import dev.ragnarok.fenrir.writeTypedObjectCompat

class Story : AbsModel, ParcelNative.ParcelableNative {
    var id = 0
        private set
    var ownerId = 0L
        private set
    var date: Long = 0
        private set
    var expires: Long = 0
        private set
    var isIs_expired = false
        private set
    var accessKey: String? = null
        private set
    var target_url: String? = null
        private set
    var photo: Photo? = null
        private set
    var video: Video? = null
        private set
    var owner: Owner? = null
        private set

    constructor()
    internal constructor(parcel: Parcel) {
        id = parcel.readInt()
        ownerId = parcel.readLong()
        date = parcel.readLong()
        expires = parcel.readLong()
        isIs_expired = parcel.getBoolean()
        accessKey = parcel.readString()
        target_url = parcel.readString()
        video = parcel.readTypedObjectCompat(Video.CREATOR)
        photo = parcel.readTypedObjectCompat(Photo.CREATOR)
        owner = readOwner(parcel)
    }

    internal constructor(parcel: ParcelNative) {
        id = parcel.readInt()
        ownerId = parcel.readLong()
        date = parcel.readLong()
        expires = parcel.readLong()
        isIs_expired = parcel.readBoolean()
        accessKey = parcel.readString()
        target_url = parcel.readString()
        video = parcel.readParcelable(Video.NativeCreator)
        photo = parcel.readParcelable(Photo.NativeCreator)
        owner = readOwner(parcel)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeLong(ownerId)
        parcel.writeLong(date)
        parcel.writeLong(expires)
        parcel.putBoolean(isIs_expired)
        parcel.writeString(accessKey)
        parcel.writeString(target_url)
        parcel.writeTypedObjectCompat(video, flags)
        parcel.writeTypedObjectCompat(photo, flags)
        writeOwner(parcel, flags, owner)
    }

    override fun writeToParcelNative(dest: ParcelNative) {
        dest.writeInt(id)
        dest.writeLong(ownerId)
        dest.writeLong(date)
        dest.writeLong(expires)
        dest.writeBoolean(isIs_expired)
        dest.writeString(accessKey)
        dest.writeString(target_url)
        dest.writeParcelable(video)
        dest.writeParcelable(photo)
        writeOwner(dest, owner)
    }

    fun isStoryIsVideo(): Boolean {
        return photo == null && video != null
    }

    fun isStoryIsPhoto(): Boolean {
        return photo != null && video == null
    }

    fun isEmptyStory(): Boolean {
        return photo == null && video == null && target_url.isNullOrEmpty()
    }

    @AbsModelType
    override fun getModelType(): Int {
        return AbsModelType.MODEL_STORY
    }

    fun setPhoto(photo: Photo?): Story {
        this.photo = photo
        return this
    }

    fun setId(id: Int): Story {
        this.id = id
        return this
    }

    fun setVideo(video: Video?): Story {
        this.video = video
        return this
    }

    fun setOwnerId(ownerId: Long): Story {
        this.ownerId = ownerId
        return this
    }

    fun setDate(date: Long): Story {
        this.date = date
        return this
    }

    fun setExpires(expires_at: Long): Story {
        expires = expires_at
        return this
    }

    fun setIs_expired(is_expired: Boolean): Story {
        isIs_expired = is_expired
        return this
    }

    fun setAccessKey(access_key: String?): Story {
        accessKey = access_key
        return this
    }

    fun setOwner(author: Owner?): Story {
        owner = author
        return this
    }

    fun setTarget_url(target_url: String?): Story {
        this.target_url = target_url
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Story) return false
        return id == other.id && ownerId == other.ownerId
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + ownerId.hashCode()
        return result
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Story> = object : Parcelable.Creator<Story> {
            override fun createFromParcel(parcel: Parcel): Story {
                return Story(parcel)
            }

            override fun newArray(size: Int): Array<Story?> {
                return arrayOfNulls(size)
            }
        }
        val NativeCreator: ParcelNative.Creator<Story> =
            object : ParcelNative.Creator<Story> {
                override fun readFromParcelNative(dest: ParcelNative): Story {
                    return Story(dest)
                }
            }
    }
}