package hr.ferit.whereisbozidarkelava

class Values() {
    private val PHOTO_CODE: Int = 1
    private val REQUEST_LOCATION_PERMISSION: Int = 12

    private val AUTHORITY: String = "hr.ferit.android.fileprovider"

    fun getPhotoCode(): Int {
        return this.PHOTO_CODE
    }

    fun getRequestLocationPermission(): Int {
        return this.REQUEST_LOCATION_PERMISSION
    }

    fun getAuthorityValue(): String {
        return this.AUTHORITY
    }
}