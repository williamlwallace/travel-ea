package models;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * A class that represents a user's nationalities
 */
@Entity
@Table(name="Nationality")
public class Nationality extends Model {

    @Id
    public Long countryId;

    @Constraints.Required
    public Long userId;

    @OneToOne
    @JoinTable(
        name="CountryDefinition",
        joinColumns=@JoinColumn(name="country_id", referencedColumnName="id"))
    public CountryDefinition country;
}