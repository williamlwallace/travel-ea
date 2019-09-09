package models.enums;

public enum NewsFeedEventType {

    // Destinations events
    /**
     * A public photo has been linked to a public destination
     * reference ID = ID of photo
     */
    LINK_DESTINATION_PHOTO,
    /**
     * A public destination has had its primary photo updated
     * reference ID = ID of photo
     */
    NEW_PRIMARY_DESTINATION_PHOTO,

    // User photo events
    /**
     * New profile picture has been set for a user
     * reference ID = ID of photo
     */
    NEW_PROFILE_PHOTO,
    /**
     * A new public picture has been uploaded by a user
     * reference ID = ID of photo
     */
    UPLOADED_USER_PHOTO
    ;
}
