package models;

import io.ebean.Model;
import javax.persistence.Entity;
import io.ebean.annotation.SoftDelete;
import javax.persistence.MappedSuperclass;

/**
 * A class that represents a country and holds information received from the database.
 */
 @MappedSuperclass
public class BaseModel extends Model {
    @SoftDelete
    public Boolean deleted;
}
