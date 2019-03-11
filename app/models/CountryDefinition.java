package models;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;

/**
 * A class that represents a country and holds information received from the database
 */
@Entity
@Table(name="CountryDefinition")
public class CountryDefinition extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public String name;
}
