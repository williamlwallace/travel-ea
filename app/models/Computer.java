package models;

import play.data.format.Formats;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Computer entity managed by Ebean
 */
@Entity 
public class Computer extends BaseModel {

  private static final long serialVersionUID = 1L;

  @Constraints.Required
  public String name;

  @Formats.DateTime(pattern="yyyy-MM-dd")
  public Date introduced;

  @Formats.DateTime(pattern="yyyy-MM-dd")
  public Date discontinued;

  @ManyToOne
  public Company company;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getIntroduced() {
    return introduced;
  }

  public void setIntroduced(Date introduced) {
    this.introduced = introduced;
  }

  public Date getDiscontinued() {
    return discontinued;
  }

  public void setDiscontinued(Date discontinued) {
    this.discontinued = discontinued;
  }

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }
}

