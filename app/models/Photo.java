package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.ebean.Model;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * A class that models the Photo database table.
 */
@Table(name = "Photo")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Photo extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long guid;

    public Long userId;

    public String filename;

    public String thumbnailFilename;

    public String caption;

    public Boolean isPublic;

    public Boolean usedForProfile;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime uploaded;

    @ManyToMany(mappedBy = "destinationPhotos")
    @JsonBackReference("photo-reference")
    @JoinTable(
        name = "DestinationPhoto",
        joinColumns = @JoinColumn(name = "photo_id", referencedColumnName = "guid"),
        inverseJoinColumns = @JoinColumn(name = "destination_id", referencedColumnName = "id"))

    public List<Destination> destinationPhotos;

    @JsonBackReference("profilePhotoReference")
    @OneToMany(mappedBy = "profilePhoto")
    public List<Profile> profilePicProfiles;

    @JsonBackReference("coverPhotoReference")
    @OneToMany(mappedBy = "coverPhoto")
    public List<Profile> coverPicProfiles;

    @ManyToMany(mappedBy = "photos")
    @JoinTable(
        name = "PhotoTag",
        joinColumns = @JoinColumn(name = "photo_id", referencedColumnName = "guid"),
        inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
    public List<Tag> tags;

    @ManyToMany(mappedBy = "destinationPrimaryPhotoPending")
    @JsonBackReference("dest-primary-photo-pending-reference")
    @JoinTable(
        name = "PendingDestinationPhoto",
        joinColumns = @JoinColumn(name = "guid", referencedColumnName = "guid"),
        inverseJoinColumns = @JoinColumn(name = "dest_id", referencedColumnName = "id"))

    public List<Destination> destPrimaryPhotoPending;

    /**
     * Removes given destination from photo.
     */
    public Boolean removeDestination(Long destId) {
        Iterator<Destination> iter = destinationPhotos.iterator();
        while (iter.hasNext()) {
            Destination dest = iter.next();
            if (dest.id.equals(destId)) {
                iter.remove();
                return true;
            }
        }
        return false;
    }
}
