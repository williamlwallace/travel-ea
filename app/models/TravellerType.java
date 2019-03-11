package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Table;

/**
 * A class that represents the TravellerType database table.
 */
@Entity
@Table(name="TravellerType")
public class TravellerType extends Model {

    @EmbeddedId //Required
    public TravellerTypeKey key;

    //Define the key
    @Embeddable //Required
    static public class TravellerTypeKey { //Only static so we can create TravellerTypeKey objects to search with
        @Constraints.Required
        public Long uid;

        @Column(name="travellerTypeId")
        @Constraints.Required
        public Long travellerTypeId;


        //Default constructor -- not strictly necessary
        public TravellerTypeKey() {}

        //This constructor will mainly be used to create keys to search with
        public TravellerTypeKey(Long uid, Long travellerTypeId) {
            this.uid = uid;
            this.travellerTypeId = travellerTypeId;
        }

        //We must include the equals method so that the program knows how to check if two TravellerTypeKeys are the same
        @Override
        public boolean equals(Object o) {
            //If the object checking against is itself
            if (this == o) return true;

            //If the object is not a TravellerTypeKey
            if (!(o instanceof TravellerTypeKey)) return false;

            //Cast the object to TravellerType key, now that we know it is one
            TravellerTypeKey object = (TravellerTypeKey) o;
            //Return whether the two component keys equal each other
            return Objects.equals(uid, object.uid) && Objects.equals(travellerTypeId, object.travellerTypeId);
        }

        //We must include the hashCode method so that the key can be hashed
        @Override
        public int hashCode() {
            return Objects.hash(uid, travellerTypeId);
        }
    }

    public static final Finder<TravellerTypeKey, TravellerType> find = new Finder<>(TravellerType.class);
}
