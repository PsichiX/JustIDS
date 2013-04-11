package com.PsichiX.JustIDS.display;

import java.util.Calendar;
import java.util.Random;

import com.PsichiX.JustIDS.R;
import com.PsichiX.JustIDS.R.layout;
import com.PsichiX.JustIDS.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ProfileActivity extends Activity {
	
	TextView nameView;
	Button btnEnter;
	EditText nameInput;
	Random rnd;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		nameView = (TextView) findViewById(R.id.txt_yourname);
		
		Button btnGenerate = (Button) findViewById(R.id.button_profile_generate);
		btnGenerate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				generateName();
			}
		});
		
		nameInput = (EditText) findViewById(R.id.txt_nameinput); 
		btnEnter = (Button) findViewById(R.id.button_profile_enter);
		btnEnter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnEnter.setVisibility(View.GONE);
				nameInput.setVisibility(View.VISIBLE);
			}
		});
		
		nameInput.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO on enter
				nameView.setText(v.getText());
				btnEnter.setVisibility(View.VISIBLE);
				nameInput.setVisibility(View.GONE);
				return true;
			}
		});
	}

	private void generateName() {
		String[] first = {"Son", "Kim", "Ve", "Obi", "Qui"};
		String[] middle = {"Go", "Dzong", "Wan", "Gon", "Ge"};
		String[] last = {"Ku", "Il", "Un", "Sen", "Jin", "Ke", "Ta"};
		
		if(rnd==null)  rnd = new Random(Calendar.getInstance().get(Calendar.MILLISECOND));
		String name = first[rnd.nextInt(first.length-1)] + " " +
			middle[rnd.nextInt(middle.length-1)] +
			last[rnd.nextInt(last.length-1)];
		
		nameView.setText(name);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_profile, menu);
		return true;
	}
}