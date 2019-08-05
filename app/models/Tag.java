package models;

import io.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;
import play.data.validation.Constraints;

@Entity
public class Tag extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public String name;
}
