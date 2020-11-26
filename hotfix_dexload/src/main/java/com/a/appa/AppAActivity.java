package com.a.appa;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.a.fix.M;

public class AppAActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_a);
        String str = M.a();
        ((TextView) findViewById(R.id.tvText)).setText(str);
    }
}