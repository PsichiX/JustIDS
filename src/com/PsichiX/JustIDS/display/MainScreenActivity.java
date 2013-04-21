package com.PsichiX.JustIDS.display;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.PsichiX.JustIDS.MainActivity;
import com.PsichiX.JustIDS.R;
import com.PsichiX.JustIDS.SpectateActivity;
import com.PsichiX.JustIDS.message.PlayerInformation.PlayerId;
import com.PsichiX.JustIDS.services.WifiService;
import com.PsichiX.JustIDS.trash.AudioRecordTest;

public class MainScreenActivity extends Activity {
    private String tag = "MainScreenActivity";

    public static String playerName;

    private PlayerId[] allPlayers = new PlayerId[0];
    private PlayerId myPlayerId;

    LayoutInflater inflater;
    WifiService wifi;
    private ArrayAdapter<PlayerId> playersAdapter;

    private class GameNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainScreenActivity.this.myPlayerId = (PlayerId) intent.getSerializableExtra("MY_PLAYER");
            MainScreenActivity.this.allPlayers = (PlayerId[]) intent.getSerializableExtra("ALL_PLAYERS");
            setPlayersView();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        playerName = ProfileActivity.generateName();

        wifi = new WifiService(this);
        if (!wifi.isWifiInWorkingState()) {
            startActivity(new Intent(this, WifiErrorActivity.class));
            Log.d(tag, "WIFI DISABLED!");
            this.finish();
            return;
        } else {
            Log.d(tag, "WIFI ENABLED, carry on");
        }

        playerName = ProfileActivity.generateName();

        playersAdapter = new ArrayAdapter<PlayerId>(this,R.layout.listitem_player) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row;
                row = inflater.inflate(R.layout.listitem_player, null);
                TextView name = (TextView) row.findViewById(R.id.listitem_player_name);
                name.setText(getItem(position).getName());
                return row;
            }
        };

        setPlayersView();
        Intent intent = new Intent("com.PsichiX.JustIDS.readyToPlay");
        intent.putExtra("NAME", playerName);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(new GameNotificationReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.ScreamFightNotificationService"));
        localBroadcastManager.sendBroadcast(intent);
        setUpScreen();
    }

    /**
     * Listenery na guzikach, adaptery itp
     */
    private void setUpScreen() {
        //Pozostala konfiguracja ekranu
        TextView playerNameView = (TextView) findViewById(R.id.txt_yourname);
        playerNameView.setText(playerName);

        playerNameView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "clicked: PROFILE ");
                gotoProfile();
            }
        });

        Button btnFight = (Button) findViewById(R.id.button_fight);
        btnFight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "clicked: FIGHT ");
                gotoFight();
            }
        });

        Button btnHowto = (Button) findViewById(R.id.button_howto);
        btnHowto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "clicked: HOWTO ");
                gotoHowto();
            }
        });

        ListView playersList = (ListView) findViewById(R.id.players_list);
        playersList.setAdapter(playersAdapter);
    }

    /**
     * Do wywo�ania przy od�wierzeniu listy graczy
     */
    public void setPlayersView() {
        LinearLayout playersNoneLL = (LinearLayout) findViewById(R.id.players_none);
        LinearLayout playersAreLL = (LinearLayout) findViewById(R.id.players_avalible);
        if (allPlayers.length > 0) {
            //JE�LI GRACZE SA W SIECI, TO SUPER:
            playersAdapter.clear();
            for (PlayerId playerId: allPlayers) {
                playersAdapter.add(playerId);
            }
            playersAdapter.notifyDataSetChanged();
            playersNoneLL.setVisibility(View.GONE);
            playersAreLL.setVisibility(View.VISIBLE);
        } else {
            //JESLI GRACZY NIE MA:
            playersAdapter.clear();
            playersNoneLL.setVisibility(View.VISIBLE);
            playersAreLL.setVisibility(View.GONE);
            playersAdapter.notifyDataSetChanged();
        }
    }

    public void gotoProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    public void gotoFight() {
        startActivity(new Intent(this, MainActivity.class));
    }

    public void gotoHowto() {
        //TODO: testowo tylko.
        startActivity(new Intent(this, AudioRecordTest.class));
    }

    public void gotoSpectate() {
        startActivity(new Intent(this, SpectateActivity.class));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_screen, menu);
        return true;
    }
}
