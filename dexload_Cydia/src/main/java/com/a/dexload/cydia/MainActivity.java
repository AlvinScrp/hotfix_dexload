package com.a.dexload.cydia;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.a.fix.M;

public class MainActivity extends AppCompatActivity {

//    static {
//        System.loadLibrary("cydiahook");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_dexload_cydia);
        String str = M.a();
        ((TextView) findViewById(R.id.tvText)).setText(str);
    }
}