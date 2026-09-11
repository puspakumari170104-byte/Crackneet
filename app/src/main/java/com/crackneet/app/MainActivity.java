package com.crackneet.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView status = findViewById(R.id.status);
        Button neet = findViewById(R.id.neetButton);
        Button jee = findViewById(R.id.jeeButton);
        neet.setOnClickListener(v -> status.setText("NEET: Biology • Chemistry • Physics\nStatement • Assertion–Reason • Numerical"));
        jee.setOnClickListener(v -> status.setText("JEE Main: Physics • Chemistry • Mathematics\nStatement • Assertion–Reason • Numerical"));
    }
}
