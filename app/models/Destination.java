package models;

import io.ebean.Model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * This class models the destination table in the database, with all requirements met. A finder is
 * also supplied for easy and concise queries
 */
@Entity
@Table(name = "Destination")
public class Destination extends Model {

    @Id
    @Constraints.Required
    public Long id;

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

    @ManyToOne
    @Constraints.Required
    public CountryDefinition country;

}
