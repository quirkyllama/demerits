package com.jjs.demerits;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class WriteDemerit extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_demerit);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_write_demerit, menu);
       
        return true;
    }
    
}
