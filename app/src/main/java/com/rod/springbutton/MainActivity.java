package com.rod.springbutton;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpringButton button = (SpringButton) findViewById(R.id.spring_button);

        String[] buttonTexts = new String[] {"Button1", "Button2", "Button3", "Button4", "Button5"};
        button.setButtons(buttonTexts);
        button.setOnButtonClickListener(new SpringButton.OnButtonClickListener() {
            @Override
            public void onButtonClick(View view, int pos) {
                Toast.makeText(MainActivity.this, String.format("clicked pos:%d, tag=%s", pos, view.getTag()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
