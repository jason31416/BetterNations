package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.planetlib.Config;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

public class BStatsManager {
    public static void initialize(){
        int pluginId = 23491;
        Metrics metrics = new Metrics(BetterNations.instance, pluginId);
        metrics.addCustomChart(new SimplePie("language", () -> Config.getString("lang", "en_us")));
        metrics.addCustomChart(new SingleLineChart("nation_count", () -> Nation.nations.size()));
    }
}
