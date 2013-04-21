package com.PsichiX.JustIDS.display;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import com.PsichiX.JustIDS.R;

public class SpectateActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_spectate, menu);
        return true;
    }
}
