package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.ebean.Model;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * This class models the destination table in the database, with all requirements met. A finder is
 * also supplied for easy and concise queries.
 */
@Entity
@Table(name = "Destination")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Destination extends Model {

    @Id
    @Constraints.Required
    public Long id;

    @ManyToOne
    @Constraints.Required
    public User user;

    @Constraints.Required
    public String name;

    @Constraints.Required
    @Column(name = "type") //type is a keyword in scala so cant get the column
    public String _type;

    @Constraints.Required
    public String district;

    @Constraints.Required
    public Double latitude;

    @Constraints.Required
    public Double longitude;

    @Constraints.Required
    @Column(name = "is_public")
    public boolean isPublic;

    @ManyToOne
    @Constraints.Required
    public CountryDefinition country;

    @ManyToMany(mappedBy = "destinationPhotos")
    @JsonBackReference
    @JoinTable(
        name = "DestinationPhoto",
        joinColumns = @JoinColumn(name = "destination_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "photo_id", referencedColumnName = "guid"))

    public List<Photo> destinationPhotos;
}
