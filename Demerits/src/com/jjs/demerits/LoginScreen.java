package com.jjs.demerits;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.jjs.demerits.client.DemeritClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginScreen {
  private static final String PREF_NAME = "ZipioPreferences8";
  private static final String SAVED_EMAIL = "email";
  private static final String SAVED_PASSWORD = "password";
  public static final int ACCOUNT_DIALOG = 0;

  private final Activity activity;
  private final DemeritClient client;
  
  private List<String> emails;  
  private String email;
  private String password;
  private boolean authenticated = false;
  private Callback callback;
  
  public LoginScreen(Activity activity, DemeritClient client) {
    this.activity = activity;
    this.client = client;
  }

  public interface Callback {
    public void gotCredentials();
  }

  public void init(Callback callback) {
    this.callback = callback;
    readCredentials();
    if (authenticated) {
      System.err.println("Authenticated!");
      callback.gotCredentials();
      return;
    }

    showLoginScreen();
    System.err.println("Get email: " + email);

    if (email == null) {
      getEmailFromAccount();
    } else {
      updateEmail(email);
    }
  }

  private EditText getPasswordForm() {
    return (EditText) activity.findViewById(R.id.password);
  }

  private EditText getEmailForm() {
    return (EditText) activity.findViewById(R.id.email);
  }

  private void showLoginScreen() {

    activity.setContentView(R.layout.login_screen);
    activity.findViewById(R.id.loginProgress).
    setVisibility(View.INVISIBLE);

    TextView.OnEditorActionListener onPasswordActionListener = new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String email = "" + getEmailForm().getText();
        System.err.println("X New email: " + email);
        if (!email.isEmpty()) {
          LoginScreen.this.email = email;
        }
        String password = "" + getPasswordForm().getText();
        if (!password.isEmpty()) {
          LoginScreen.this.password = password;
          login();
        }
        return false;
      }      
    };
    TextView.OnEditorActionListener onEmailActionListener = new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String email = "" + getEmailForm().getText();
        if (!email.isEmpty()) {
          LoginScreen.this.email = email;
          if (password != null) {
            login();
          }
        }
        return false;
      }      
    };   
    getEmailForm().setOnEditorActionListener(onEmailActionListener);
    getPasswordForm().setOnEditorActionListener(onPasswordActionListener);
  }

  private void login() {
    activity.findViewById(R.id.loginProgress).
    setVisibility(View.VISIBLE);
    client.setCredentials(email, password);
    callback.gotCredentials();
  }

  public boolean isShowing() {
    return !authenticated;
  }

  public static String getEmail(Context context) {
    SharedPreferences preferences = 
        context.getSharedPreferences(
                PREF_NAME, Activity.MODE_PRIVATE);
    return preferences.getString(SAVED_EMAIL, null);
  }
  
  private void readCredentials() {
    SharedPreferences preferences = 
            activity.getBaseContext().getSharedPreferences(
                    PREF_NAME, Activity.MODE_PRIVATE);
    email = preferences.getString(SAVED_EMAIL, null);
    System.err.println("Email from saved bundle: " + email);
    if (email != null) {
      password = preferences.getString(SAVED_PASSWORD, null);
      if (password != null) {
        authenticated = true;
        client.setCredentials(email,  password);
      }
    }
  }

  private void getEmailFromAccount() {
    System.err.println("Get email from account");
    Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
    Account[] accounts = AccountManager.get(activity).getAccounts();
    emails = new ArrayList<String>();
    for (int i = 0; i < accounts.length; i++) {
      if (emailPattern.matcher(accounts[i].name).matches() &&
              !emails.contains(accounts[i].name)) {
        emails.add(accounts[i].name);
      }
    }
    emails.add("Other");
    activity.showDialog(ACCOUNT_DIALOG);
  }

  public Dialog createAccountDialog() {
    if (emails == null) {
      System.err.println("Emails are null!");
      return null;
    }
    AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
    System.err.println("Setting up PW");
    dialog.setTitle("Pick Email Account");
    dialog.setItems(emails.toArray(new String[emails.size()]),
            new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (which == emails.size() - 1) {
          updateEmail(null);
        } else {
          updateEmail(emails.get(which));
        }
        dialog.dismiss();
      }
    });

    return dialog.create();
  }

  public void onPause() {
    if (email != null && email.length() > 0) {
      SharedPreferences.Editor preferences = 
              activity.getBaseContext().getSharedPreferences(
                      PREF_NAME, Activity.MODE_PRIVATE).edit();
      preferences.putString(SAVED_EMAIL,  email);

      System.err.println("Save state: " + email);
      preferences.putString(SAVED_PASSWORD, password);
      preferences.commit();
    }
  }

  public void updateEmail(String email) {
    this.email = email;
    getEmailForm().setText(email);
    final EditText view = (email == null) ? getEmailForm() : getPasswordForm();
    view.requestFocus();
    (new Handler()).postDelayed(new Runnable() {
      public void run() {
        view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
        view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
      }
    }, 100);
  }

  public void loginFailed() {
    showLoginScreen();
    Toast.makeText(activity, "Login Failed", Toast.LENGTH_SHORT).show();
  }
}