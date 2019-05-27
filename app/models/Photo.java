package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.ebean.Model;
import java.util.Iterator;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;

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

    public Boolean isPublic;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime uploaded;

    public Boolean isProfile;

    @ManyToMany(mappedBy = "destinationPhotos")
    @JsonBackReference
    @JoinTable(
        name = "DestinationPhoto",
        joinColumns = @JoinColumn(name = "photo_id", referencedColumnName = "guid"),
        inverseJoinColumns = @JoinColumn(name = "destination_id", referencedColumnName = "id"))

    public List<Destination> destinationPhotos;

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
