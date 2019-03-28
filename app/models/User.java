package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

public class User {

    public Long id;

    public String username;

    public String password;

    public String salt;

    public Boolean admin = false;
    
}