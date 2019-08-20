package models.enums;

import models.DestinationTag;
import models.PhotoTag;
import models.TripTag;

/**
 * This enum contains the different types of tag type, used for searching for as specific tag type
 */
public enum TagType {
    DESTINATION_TAG(DestinationTag.class),
    PHOTO_TAG(PhotoTag.class),
    TRIP_TAG(TripTag.class)
    ;

    private Class<? extends Object> classType;

    TagType(Class<? extends Object> classType) {
        this.classType = classType;
    }

    public Class<? extends Object> getClassType() {
        return classType;
    }
}
