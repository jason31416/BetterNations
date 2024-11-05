package cn.jason31416.planetlib;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.Calendar;
import java.util.Collection;

public class Utils {
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }
    public static String getTimeString(long time){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        return String.format("%02d-%02d-%04d %02d:%02d", day, month, year, hour, minute);
    }
    public static String formatSeconds(int seconds){
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secondsLeft = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secondsLeft);
    }
    public static java.awt.Color toJavaColor(org.bukkit.Color color) {
        return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
    }
    public static String ListToString(@Nonnull Collection<?> strings) {
        StringBuilder string = new StringBuilder();
        for(Object s : strings) {
            if(!string.isEmpty()) string.append(", ");
            string.append(s.toString());
        }
        return string.toString();
    }
    public static boolean isBlockBetweenLocations(@Nonnull Location loc1, @Nonnull Location loc2) {
        Vector vec = loc2.subtract(loc1).toVector();
        Vector step = vec.normalize().multiply(0.5);
        for(int i = 0; i < vec.length() * 2; i++){
            if(!loc1.add(step.multiply(i)).getBlock().isPassable()) {
                return true;
            }
        }
        return false;
    }
}
