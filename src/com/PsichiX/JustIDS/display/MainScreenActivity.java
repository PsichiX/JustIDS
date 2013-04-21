package com.PsichiX.JustIDS.display;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.PsichiX.JustIDS.R;
import com.PsichiX.JustIDS.game.GameStateMachine.GameStateNotificationEnum;
import com.PsichiX.JustIDS.message.PlayerInformation.Player;
import com.PsichiX.JustIDS.services.WifiService;
import com.PsichiX.JustIDS.simulator.SimulatedScenarioEnum;
import com.PsichiX.JustIDS.trash.AudioRecordTest;

public class MainScreenActivity extends Activity {

    private static boolean ENABLE_SIMULATION = true;

    private String TAG = MainScreenActivity.class.getName();

    public static String playerName;

    private Player[] allPlayers = new Player[0];
    private Player myPlayer;

    LayoutInflater inflater;
    WifiService wifi;
    private ArrayAdapter<Player> playersAdapter;

    private class GameNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainScreenActivity.this.myPlayer = (Player) intent.getSerializableExtra("MY_PLAYER");
            MainScreenActivity.this.allPlayers = (Player[]) intent.getSerializableExtra("ALL_PLAYERS");

            GameStateNotificationEnum gameStateNotificationEnum =
                    GameStateNotificationEnum.values()[intent.getIntExtra("NOTIFICATION_TYPE", -1)];
            switch (gameStateNotificationEnum) {
                case GAME_STARTED_OBSERVER:
                    // now I automatically became observer because 2 players joined the game
                    gotoSpectate();
                    break;
                default:
                    refreshPlayersView();
                    break;
            }
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

        setUpScreen();
        refreshPlayersView();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

        // Register now to receive notifications from Game Manager
        localBroadcastManager.registerReceiver(new GameNotificationReceiver(),
                new IntentFilter("com.PsichiX.JustIDS.ScreamFightNotificationService"));

        // I am ready to play now. Start new game with setting the name
        Intent intent = new Intent("com.PsichiX.JustIDS.readyToPlay");
        intent.putExtra("NAME", playerName);
        localBroadcastManager.sendBroadcast(intent);

    }

    private void setUpScreen() {
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

    public void refreshPlayersView() {
        LinearLayout playersNoneLL = (LinearLayout) findViewById(R.id.players_none);
        LinearLayout playersAreLL = (LinearLayout) findViewById(R.id.players_avalible);
        if (allPlayers.length > 0) {
            playersAdapter.clear();
            for (Player playerId: allPlayers) {
                playersAdapter.add(playerId);
            }
            playersAdapter.notifyDataSetChanged();
            playersNoneLL.setVisibility(View.GONE);
            playersAreLL.setVisibility(View.VISIBLE);
        } else {
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
        //TODO: temporary only - for tests. should be replaced by automatic training
        startActivity(new Intent(this, AudioRecordTest.class));
    }

    public void gotoSpectate() {
        startActivity(new Intent(this, SpectateActivity.class));
    }


    public void startSimulation(SimulatedScenarioEnum scenarioEnum) {
        Intent intent = new Intent("com.PsichiX.JustIDS.startSimulation");
        intent.putExtra("SCENARIO", scenarioEnum.ordinal());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_start_simulation_fight_back_stronger:
                startSimulation(SimulatedScenarioEnum.FIGHT_BACK_STRONGER);
                break;
            case R.id.menu_start_simulation_fight_back_weaker:
                startSimulation(SimulatedScenarioEnum.FIGHT_BACK_WEAKER);
                break;
            case R.id.menu_start_simulation_fight_continuous:
                startSimulation(SimulatedScenarioEnum.ATTACK_CONTINUOUSLY);
                break;
            case R.id.menu_start_simulation_fight_counter_attack:
                startSimulation(SimulatedScenarioEnum.COOUNTER_ATTACK_QUICKLY);
                break;
            case R.id.menu_start_simulation_fight_observer_only:
                startSimulation(SimulatedScenarioEnum.OBSERVER_ONLY);
                break;
            case R.id.menu_start_simulation_fight_passive:
                startSimulation(SimulatedScenarioEnum.DONT_FIGHT_BACK);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (ENABLE_SIMULATION) {
            getMenuInflater().inflate(R.menu.activity_main_screen, menu);
            return true;
        } else {
            return false;
        }
    }
}
