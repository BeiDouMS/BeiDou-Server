package tools;

import client.Character;
import server.ItemInformationProvider;

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class LogHelper {

    public static String getTimeString(long then) {
        long duration = System.currentTimeMillis() - then;
        int seconds = (int) (duration / SECONDS.toMillis(1)) % 60;
        int minutes = (int) ((duration / MINUTES.toMillis(1)) % 60);
        return minutes + " Minutes and " + seconds + " Seconds";
    }

    public static void logLeaf(Character player, boolean gotPrize, String operation) {
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(new Date());
        String log = player.getName() + (gotPrize ? " used a maple leaf to buy " + operation : " redeemed " + operation + " VP for a leaf") + " - " + timeStamp;
        FilePrinter.print(FilePrinter.LOG_LEAF, log);
    }

    public static void logGacha(Character player, int itemid, String map) {
        String itemName = ItemInformationProvider.getInstance().getName(itemid);
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(new Date());
        String log = player.getName() + " got a " + itemName + "(" + itemid + ") from the " + map + " gachapon. - " + timeStamp;
        FilePrinter.print(FilePrinter.LOG_GACHAPON, log);
    }
}
