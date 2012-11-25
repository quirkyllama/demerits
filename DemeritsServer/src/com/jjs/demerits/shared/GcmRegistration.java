package com.jjs.demerits.shared;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class GcmRegistration {

  @PrimaryKey
  @Persistent
  private String email;
  
  @Persistent
  private String registrationId;
  
  @Persistent
  private long registrationDate;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(String registrationId) {
    this.registrationId = registrationId;
  }

  public long getRegistrationDate() {
    return registrationDate;
  }

  public void setRegistrationDate(long registrationDate) {
    this.registrationDate = registrationDate;
  }  
}
