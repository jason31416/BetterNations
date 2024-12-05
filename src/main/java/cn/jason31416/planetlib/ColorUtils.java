package cn.jason31416.planetlib;

import org.bukkit.Color;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class ColorUtils {
    public record ColoredMaterials(Material wool, Material stained_glass, Material concrete, Material terracotta, Material banner){}
    public static Map<Color, ColoredMaterials> colorMap=new HashMap<>();
    static {
        colorMap.put(Color.fromRGB(22, 22, 27), new ColoredMaterials(Material.BLACK_WOOL, Material.BLACK_STAINED_GLASS, Material.BLACK_CONCRETE, Material.BLACK_TERRACOTTA, Material.BLACK_BANNER));
        colorMap.put(Color.fromRGB(161, 40, 35), new ColoredMaterials(Material.RED_WOOL, Material.RED_STAINED_GLASS, Material.RED_CONCRETE, Material.RED_TERRACOTTA, Material.RED_BANNER));
        colorMap.put(Color.fromRGB(249, 196, 38), new ColoredMaterials(Material.YELLOW_WOOL, Material.YELLOW_STAINED_GLASS, Material.YELLOW_CONCRETE, Material.YELLOW_TERRACOTTA, Material.YELLOW_BANNER));
        colorMap.put(Color.fromRGB(85, 109, 28), new ColoredMaterials(Material.GREEN_WOOL, Material.GREEN_STAINED_GLASS, Material.GREEN_CONCRETE, Material.GREEN_TERRACOTTA, Material.GREEN_BANNER));
        colorMap.put(Color.fromRGB(53, 57, 158), new ColoredMaterials(Material.BLUE_WOOL, Material.BLUE_STAINED_GLASS, Material.BLUE_CONCRETE, Material.BLUE_TERRACOTTA, Material.BLUE_BANNER));
        colorMap.put(Color.fromRGB(232, 235, 235), new ColoredMaterials(Material.WHITE_WOOL, Material.WHITE_STAINED_GLASS, Material.WHITE_CONCRETE, Material.WHITE_TERRACOTTA, Material.WHITE_BANNER));
        colorMap.put(Color.fromRGB(63, 68, 72), new ColoredMaterials(Material.GRAY_WOOL, Material.GRAY_STAINED_GLASS, Material.GRAY_CONCRETE, Material.GRAY_TERRACOTTA, Material.GRAY_BANNER));
        colorMap.put(Color.fromRGB(142, 142, 135), new ColoredMaterials(Material.LIGHT_GRAY_WOOL, Material.LIGHT_GRAY_STAINED_GLASS, Material.LIGHT_GRAY_CONCRETE, Material.LIGHT_GRAY_TERRACOTTA, Material.LIGHT_GRAY_BANNER));
        colorMap.put(Color.fromRGB(116, 73, 41), new ColoredMaterials(Material.BROWN_WOOL, Material.BROWN_STAINED_GLASS, Material.BROWN_CONCRETE, Material.BROWN_TERRACOTTA, Material.BROWN_BANNER));
        colorMap.put(Color.fromRGB(241, 118, 19), new ColoredMaterials(Material.ORANGE_WOOL, Material.ORANGE_STAINED_GLASS, Material.ORANGE_CONCRETE, Material.ORANGE_TERRACOTTA, Material.ORANGE_BANNER));
        colorMap.put(Color.fromRGB(111, 184, 26), new ColoredMaterials(Material.LIME_WOOL, Material.LIME_STAINED_GLASS, Material.LIME_CONCRETE, Material.LIME_TERRACOTTA, Material.LIME_BANNER));
        colorMap.put(Color.fromRGB(122, 42, 172), new ColoredMaterials(Material.PURPLE_WOOL, Material.PURPLE_STAINED_GLASS, Material.PURPLE_CONCRETE, Material.PURPLE_TERRACOTTA, Material.PURPLE_BANNER));
        colorMap.put(Color.fromRGB(239, 141, 172), new ColoredMaterials(Material.PINK_WOOL, Material.PINK_STAINED_GLASS, Material.PINK_CONCRETE, Material.PINK_TERRACOTTA, Material.PINK_BANNER));
        colorMap.put(Color.fromRGB(189, 69, 190), new ColoredMaterials(Material.MAGENTA_WOOL, Material.MAGENTA_STAINED_GLASS, Material.MAGENTA_CONCRETE, Material.MAGENTA_TERRACOTTA, Material.MAGENTA_BANNER));
        colorMap.put(Color.fromRGB(21, 138, 146), new ColoredMaterials(Material.CYAN_WOOL, Material.CYAN_STAINED_GLASS, Material.CYAN_CONCRETE, Material.CYAN_TERRACOTTA, Material.CYAN_BANNER));
        colorMap.put(Color.fromRGB(58, 175, 217), new ColoredMaterials(Material.LIGHT_BLUE_WOOL, Material.LIGHT_BLUE_STAINED_GLASS, Material.LIGHT_BLUE_CONCRETE, Material.LIGHT_BLUE_TERRACOTTA, Material.LIGHT_BLUE_BANNER));
    }
    public static ColoredMaterials getClosest(Color color){
        Color closestColor=null;
        double closestDistanceSqd=Double.MAX_VALUE;
        for(Color c:colorMap.keySet()) {
            double distancesqd = Math.pow(color.getRed() - c.getRed(), 2) + Math.pow(color.getGreen() - c.getGreen(), 2) + Math.pow(color.getBlue() - c.getBlue(), 2);
            if (distancesqd < closestDistanceSqd) {
                closestColor = c;
                closestDistanceSqd = distancesqd;
            }
        }
        return colorMap.get(closestColor);
    }
}
