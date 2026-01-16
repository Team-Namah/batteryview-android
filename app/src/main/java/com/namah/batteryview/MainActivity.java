package com.namah.batteryview;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.namah.batteryview.lib.BatteryView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BatteryView batteryView = findViewById(R.id.battery_view);
        EditText etLevel = findViewById(R.id.etLevel);

        etLevel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (!input.isEmpty()) {
                    try {
                        int level = Integer.parseInt(input);
                        batteryView.setBatteryLevel(level);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Set initial battery level
        batteryView.setBatteryLevel(100);
    }
}
