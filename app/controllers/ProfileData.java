package controllers;

import play.data.validation.Constraints;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * A form processing DTO that maps to the widget form.
 *
 * Using a class specifically for form binding reduces the chances
 * of a parameter tampering attack and makes code clearer, because
 * you can define constraints against the class.
 */
public class ProfileData {

    private String firstName;

    private String middleName;

    private String lastName;

    private String gender;

    private LocalDate DOB;

    private ArrayList<String> nationalities;

    private ArrayList<String> passports;

    private ArrayList<String> travelerTypes;


    public ProfileData() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() { return middleName; }

    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {return gender; }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDOB() {return DOB;}

    public ArrayList<String> getNationalities() { return nationalities; }

    public void setNationalities(ArrayList<String> nationalities) {
        this.nationalities = nationalities;
    }

    public ArrayList<String> getPassports() {
        return passports;
    }

    public void setPassports(ArrayList<String> passports) {
        this.passports = passports;
    }

    public ArrayList<String> getTravelerTypes() { return travelerTypes; }

    public void setTravelerTypes(ArrayList<String> travelerTypes) { this.travelerTypes = travelerTypes; }
}
