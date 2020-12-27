package com.a.robust;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.a.robust.data.M;
import com.a.robust.patchexecute.PatchExecutor;


public class MainActivity extends AppCompatActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_robust);
        context=MainActivity.this.getApplicationContext();
        showText();
        findViewById(R.id.btnHotfix).setOnClickListener(v -> {
            PatchExecutor.doPatch(context);
            showText();

        });
    }

    private void showText() {
        String str = new M().a(100) + "," + M.b();
        ((TextView) findViewById(R.id.tvText)).setText(str);
    }
}