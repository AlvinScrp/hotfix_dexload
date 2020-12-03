package com.a.dexload.qfix;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.a.fix.M;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_dexload_qfix);
        String str = M.a();
        ((TextView) findViewById(R.id.tvText)).setText(str);
    }
}