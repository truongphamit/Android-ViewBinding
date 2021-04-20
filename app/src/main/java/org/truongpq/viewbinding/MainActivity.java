package org.truongpq.viewbinding;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import org.truongpq.annotation.BindView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tvTitle)
    TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewBinding.bind(this);
        tvTitle.setText("What's yout name?");
    }
}