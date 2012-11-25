package com.jjs.demerits;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;
import com.jjs.demerits.client.DemeritClient;

public class GCMIntentService extends GCMBaseIntentService {
  private final DemeritClient client = new DemeritClient();
  
  public GCMIntentService() {
    super();
    System.err.println("GCMIntentService Created!");
  }

  public GCMIntentService(String... senderIds) {
    super(senderIds);
  }

  @Override
  protected void onError(Context arg0, String arg1) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void onMessage(Context arg0, Intent arg1) {
    System.err.println("Got Message from GCM!");
    // TODO Auto-generated method stub

  }

  @Override
  protected void onRegistered(Context arg0, String register) {
    System.err.println("GCM Registered!");
  }

  @Override
  protected void onUnregistered(Context arg0, String arg1) {
    System.err.println("GCM Unregistered!");

  }

}
