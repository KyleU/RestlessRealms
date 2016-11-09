package restless.realms.server.util;

import java.util.Random;

public class RandomUtils {
    private static Random r = new Random();
    
    public static int getInt(int minInclusive, int maxInclusive) {
        int delta = maxInclusive - minInclusive;
        int ret = minInclusive + (delta == 0 ? 0 : r.nextInt(delta));
        return ret;
    }

    public static int nextInt() {
        return r.nextInt();
    }

    public static boolean percentageCheck(int percentage) {
        return percentage == 100 ? true : percentage > r.nextInt(100);
    }

    public static boolean tenthPercentageCheck(int percentage) {
        return percentage == 1000 ? true : percentage > r.nextInt(1000);
    }

}
