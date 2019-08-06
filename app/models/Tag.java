package models;

import io.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import play.data.validation.Constraints;

@Entity
@Table(name = "Tag")
public class Tag extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public String name;
}
