package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.dbOnly.Nationality;
import models.dbOnly.Passport;
import models.dbOnly.TravellerType;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a profile
 */
public class Profile {

    /**
     * Empty constructor
     */
    public Profile() {}

    /**
     * Constructor to build a profile object, from the objects found when querying from database
     * @param dbProfile
     * @param nationalities
     * @param passports
     * @param travellerTypes
     */
    public Profile(models.dbOnly.Profile dbProfile, List<CountryDefinition> nationalities, List<CountryDefinition> passports, List<TravellerTypeDefinition> travellerTypes) {
        this.userId = dbProfile.userId;
        this.firstName = dbProfile.firstName;
        this.lastName = dbProfile.lastName;
        this.middleName = dbProfile.middleName;
        this.dateOfBirth = dbProfile.dateOfBirth;
        this.gender = dbProfile.gender;
        this.nationalities = nationalities;
        this.passports = passports;
        this.travellerTypes = travellerTypes;
    }

    public Long userId;

    public String firstName;

    public String lastName;

    public String middleName;

    public String dateOfBirth;

    public String gender;

    public List<TravellerTypeDefinition> travellerTypes;

    public List<CountryDefinition> nationalities;

    public List<CountryDefinition> passports;

    public List<CountryDefinition> getNationalities() {
        return nationalities;
    }

    public int calculateAge() {
        LocalDate birthDate = LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age;
    }

    /**
     * Takes this object's list of nationalities, and converts it to objects the database handles
     * @return List of nationality bridging objects
     */
    @JsonIgnore
    public List<Nationality> getDBCompliantNationalities() {
        // Create new list to store nationality objects that are insertable to database
        List<Nationality> nationalities = new ArrayList<>();

        // Map all country definitions in nationalities to a nationality object
        for(CountryDefinition def : this.nationalities) {
            Nationality nat = new Nationality();
            nat.countryId = def.id;
            nat.userId = this.userId;
            nationalities.add(nat);
        }

        return nationalities;
    }

    /**
     * Takes this object's list of passports, and converts it to objects the database handles
     * @return List of passport bridging objects
     */
    @JsonIgnore
    public List<Passport> getDBCompliantPassports() {
        // Create new list to store passport objects that are insertable to database
        List<Passport> passports = new ArrayList<>();

        // Map all country definitions in passports to a passport object
        for(CountryDefinition def : this.passports) {
            Passport pass = new Passport();
            pass.countryId = def.id;
            pass.userId = this.userId;
            passports.add(pass);
        }

        return passports;
    }

    /**
     * Takes this object's list of traveller types, and converts it to objects the database handles
     * @return List of traveller type bridging objects
     */
    @JsonIgnore
    public List<TravellerType> getDBCompliantTravellerTypes() {
        // Create new list to store traveller type objects that are insertable to database
        List<TravellerType> travellerTypes = new ArrayList<>();

        // Map all traveller type definitions in traveller types to a db traveller type object
        for(TravellerTypeDefinition def : this.travellerTypes) {
            TravellerType type = new TravellerType();
            type.travellerTypeId = def.id;
            type.userId = this.userId;
            travellerTypes.add(type);
        }

        return travellerTypes;
    }
}