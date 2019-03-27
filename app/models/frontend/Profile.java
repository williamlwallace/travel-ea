package models.frontend;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

/**
 * Presentation object used for displaying data in a template.
 *
 */
public class Profile {

    public String time;
    public String firstName;
    public String middleName;
    public String lastName;
    public String gender;
    public ArrayList<String> nationalities;
    public ArrayList<String> passports;
    public ArrayList<String> travelerTypes;
    //Should have date of birth
    private LocalDate DOB;


    public Profile(){}

    public Profile(String time, String firstName, String middleName, String lastName, String gender, LocalDate DOB, ArrayList<String> nationalities, ArrayList<String> passports, ArrayList<String> travelerTypes) {
        this.time = time;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.gender = gender;
        this.DOB = DOB;
        this.nationalities = nationalities;
        this.passports = passports;
        this.travelerTypes = travelerTypes;
    }

    public String getTime() { return time; }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() { return middleName; }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public ArrayList<String> getNationalities() {
        return nationalities;
    }

    public String getNationalitiesString() {
        return String.join(", ", nationalities);
    }

    public ArrayList<String> getPassports() {
        return passports;
    }

    public ArrayList<String> getTravelerTypes() { return travelerTypes; }

    public String getTravelerTypesString() {
        return String.join(", ", travelerTypes);
    }

    public void setDOB(LocalDate DOB) {
        this.DOB = DOB;
    }

    public LocalDate getDOB() {
        return DOB;
    }

    public int getAge() {
        return Period.between(DOB, LocalDate.now()).getYears();
    }

    public void setTime(String time) { this.time = time; }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setNationalities(ArrayList<String> nationalities) {
        this.nationalities = nationalities;
    }

    public void setPassports(ArrayList<String> passports) {
        this.passports = passports;
    }

    public void setTravelerTypes(ArrayList<String> travelerTypes) { this.travelerTypes = travelerTypes; }

}
