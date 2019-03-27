package models.frontend;

/**
 * Presentation object used for displaying data in a template.
 *
 */
public class Destination {
    public String name;
    public String destType;
    public String district;
    public double latitude;
    public double longitude;
    public String country;


    public Destination(){}

    public Destination(String name, String destType, String district, double latitude, double longitude, String country) {
        this.name = name;
        this.destType = destType;
        this.district = district;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
    }
}
