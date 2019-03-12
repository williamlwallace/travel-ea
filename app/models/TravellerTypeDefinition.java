package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A class that represents a traveller type and holds information received from database
 */
@Entity
@Table(name="TravellerTypeDefinition")
public class TravellerTypeDefinition extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public String description;

    public static final Finder<Long, TravellerTypeDefinition> find = new Finder<>(TravellerTypeDefinition.class);
}