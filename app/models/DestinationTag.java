package models;

import io.ebean.Model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;

@Entity
@Table(name = "DestinationTag")
public class DestinationTag extends Model {

    @Id
    public Long guid;

    @Required
    @Column(name = "destination_id")
    public Long destinationId;

    @ManyToOne
    @Constraints.Required
    public Tag tag;


}
