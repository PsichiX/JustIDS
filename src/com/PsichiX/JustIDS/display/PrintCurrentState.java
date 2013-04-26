package com.PsichiX.JustIDS.display;

import com.PsichiX.JustIDS.message.PlayerInformation.Player;

public class PrintCurrentState {

    public static String getCurrentPlayerAsString(Player player) {
        return "{" + player.getName() + ":" + player.getLifePoints() + " }";
    }

    public static String getCurrentStateAsString(Player myPlayer, Player otherPlayers[]) {
        StringBuilder sb = new StringBuilder(getCurrentPlayerAsString(myPlayer));
        sb.append(" [");
        for (Player player: otherPlayers) {
            sb.append(getCurrentPlayerAsString(player));
            sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
