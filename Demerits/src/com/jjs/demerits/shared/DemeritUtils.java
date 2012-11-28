package com.jjs.demerits.shared;

public class DemeritUtils {
  public static String getShortEmail(String email) {
    if (email.indexOf('@') > 0) {
      return email.substring(0, email.indexOf('@'));
    } 
    return email;
  }
}
