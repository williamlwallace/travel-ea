package models;

import io.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import play.data.validation.Constraints;

/**
 * A class that models the TreasureHunt database table.
 */
@Table(name = "TreasureHunt")
@Entity
public class TreasureHunt extends Model {

    @Id
    public Long id;

    @Constraints.Required
    @ManyToOne
    @JoinTable(
        name = "User",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    public User user;

    @Constraints.Required
    @ManyToOne
    @JoinTable(
        name = "Destination",
        joinColumns = @JoinColumn(name = "destination_id", referencedColumnName = "id"))
    public Destination destination;

    @Constraints.Required
    public String riddle;

    @Constraints.Required
    public String startDate;

    @Constraints.Required
    public String endDate;
}
