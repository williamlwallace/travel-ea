package models.frontend;

/**
 * Stores information linked to an account.
 */
public class Account {
    public String email;
    public String password;
    public Profile profile;

    public Account(String email, String password) {
        this.email = email;
        this.password = password;
        this.profile = new Profile();
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
