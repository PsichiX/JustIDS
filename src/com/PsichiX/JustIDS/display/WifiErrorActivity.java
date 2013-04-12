package com.PsichiX.JustIDS.display;

import com.PsichiX.JustIDS.R;
import com.PsichiX.JustIDS.R.layout;
import com.PsichiX.JustIDS.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class WifiErrorActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_error);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_wifi_error, menu);
        return true;
    }
}
