package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Model;
import io.ebean.annotation.SoftDelete;
import javax.persistence.MappedSuperclass;

/**
 * A class that represents a country and holds information received from the database.
 */
@MappedSuperclass
public class BaseModel extends Model {

    @JsonIgnore
    @SoftDelete
    public Boolean deleted = false;
}
