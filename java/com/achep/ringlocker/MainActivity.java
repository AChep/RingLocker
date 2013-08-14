package com.achep.ringlocker;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import com.achep.ringlocker.locker.LockerView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   setContentView(new LockerView(this));
        startService(new Intent(this, KeyguardService.class));
    }
    
}
