package controllers;

import play.data.validation.Constraints;

/**
 * Stores user created destinations
 */
public class DestData {

    @Constraints.Required
    private String name;
    private String destType;
    private String district;
    private double latitude;
    private double longitude;
    private String country;


    public DestData() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return destType;
    }

    public void setType(String destType) {
        this.destType = destType;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

}
