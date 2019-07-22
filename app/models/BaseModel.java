package models;

import io.ebean.Model;
import javax.persistence.Entity;
import io.ebean.annotation.SoftDelete;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.MappedSuperclass;

/**
 * A class that represents a country and holds information received from the database.
 */
@MappedSuperclass
public class BaseModel extends Model {
    @JsonIgnore
    @SoftDelete
    public Boolean deleted;
}
