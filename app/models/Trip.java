package models;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;

/**
 * A class that models the trip database table
 */
@Entity
@Table(name="Trip")
public class Trip extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public Long userId;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<TripData> tripDataList;
}
