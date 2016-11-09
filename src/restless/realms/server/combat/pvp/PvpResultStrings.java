package restless.realms.server.combat.pvp;

import java.util.Random;


public class PvpResultStrings {
    private static Random random = new Random();
    
    private static String[] victoryMessages = {
        " beat down #%2#!",
        " trashed #%2#!",
        " ruined #%2#'s face!",
        " destroyed #%2#!",
        " made #%2# their girlfriend!",
        " owned #%2#!",
        " made #%2# sorry!",
        " ate #%2# for breakfast!",
        " punched #%2# in the throat!",
        " schooled #%2#!",
        " handed #%2# some tissues!",
        " showed #%2# their angry pants!",
        " broke #%2# in half!",
        " assaulted #%2# and won!",
        " broke #%2#'s bones!",
        " proved their worth against #%2#!",
        " punched #%2# in the snack cake!",
        " assassinated #%2#!",
        " made #%2# cry!"
    };

    private static String[] defeatMessages = {
        " fought #%1# off!",
        " defended against #%1#!",
        " held their own against #%1#'s onslaught!",
        " stood strong as #%1# attacked!",
        " holds off #%1#'s attack!",
        " successfully defends themselves against #%1#!",
        " doesn't flinch against #%1#'s assault!",
        " defends. #%1# loses!",
    };
    
    public static String getVictoryMessage(String defender) {
        int index = random.nextInt(victoryMessages.length);
        String message = victoryMessages[index];
        message = message.replace("%2", defender);
        return message;
    }

    public static String getDefeatMessage(String attacker) {
        int index = random.nextInt(defeatMessages.length);
        String message = defeatMessages[index];
        message = message.replace("%1", attacker);
        return message;
    }
}
