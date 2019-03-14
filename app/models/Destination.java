package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This class models the destination table in the database, with all requirements met.
 * A finder is also supplied for easy and concise queries
 *
 * @author Harrison Cook
 */
@Entity
public class Destination extends Model {

    @Id
    @Constraints.Required
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    @Column(name="type") //type is a keyword in scala so cant get the column
    public String _type;

    @Constraints.Required
    public String district;

    @Constraints.Required
    public Double latitude;

    @Constraints.Required
    public Double longitude;

    @Constraints.Required
    public Long countryId;

    public static final Finder<Long, Destination> find = new Finder<>(Destination.class);
}
