package models;

import io.ebean.Model;
import play.data.validation.Constraints;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A class that represents a profile
 */
public class Profile {

    public Long userId; //Unique user id

    public String firstName;

    public String lastName;

    public String middleName;

    public String dateOfBirth;

    public String gender;

    public List<TravellerTypeDefinition> travellerTypes;

    public List<CountryDefinition> nationalities;

    public List<CountryDefinition> passports;

    public int calculateAge() {
        LocalDate birthDate = LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age;
    }

    public List<CountryDefinition> getNationalities() {
        return nationalities;
    }
}