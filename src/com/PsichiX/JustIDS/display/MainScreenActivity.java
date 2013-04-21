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
import com.PsichiX.JustIDS.R;
import com.PsichiX.JustIDS.message.PlayerInformation.Player;
import com.PsichiX.JustIDS.services.WifiService;
import com.PsichiX.JustIDS.trash.AudioRecordTest;

public class MainScreenActivity extends Activity {
    private String TAG = MainScreenActivity.class.getName();

    public static String playerName;

    private Player[] allPlayers = new Player[0];
    private Player myPlayerId;

    LayoutInflater inflater;
    WifiService wifi;
    private ArrayAdapter<Player> playersAdapter;

    private class GameNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainScreenActivity.this.myPlayerId = (Player) intent.getSerializableExtra("MY_PLAYER");
            MainScreenActivity.this.allPlayers = (Player[]) intent.getSerializableExtra("ALL_PLAYERS");
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
            Log.d(TAG, "WIFI DISABLED!");
            this.finish();
            return;
        } else {
            Log.d(TAG, "WIFI ENABLED, carry on");
        }

        playerName = ProfileActivity.generateName();

        playersAdapter = new ArrayAdapter<Player>(this,R.layout.listitem_player) {
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
                Log.d(TAG, "clicked: PROFILE ");
                gotoProfile();
            }
        });

        Button btnFight = (Button) findViewById(R.id.button_fight);
        btnFight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked: FIGHT ");
                gotoFight();
            }
        });

        Button btnHowto = (Button) findViewById(R.id.button_howto);
        btnHowto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked: HOWTO ");
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
            for (Player playerId: allPlayers) {
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
        Intent intent = new Intent("com.PsichiX.JustIDS.joinGame");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        startActivity(new Intent(this, GameActivity.class));
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
