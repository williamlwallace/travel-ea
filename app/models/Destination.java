package models;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * This class models the destination table in the database, with all requirements met.
 * A finder is also supplied for easy and concise queries
 */
public class Destination {

    public Long id;

    public String name;

    public String _type;

    public String district;

    public Double latitude;

    public Double longitude;

    public CountryDefinition country;

}
