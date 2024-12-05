package cn.jason31416.betternations.manager;

import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.update.UpdateTask;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class BorderDisplayManager implements UpdateTask.RunnableTask {
    private void drawBorder(SimpleChunkLocation chunk, int dx, int dz, Player player, Color color, int shifting) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, (float) Config.getDouble("border-display.particle-size", 1.5));
        if(dx==1){ // eastern border of the chunk
            int x = chunk.x()*16 + 15 - shifting, z = chunk.z()*16+shifting;
            for(int i=0; i<16-2*shifting; i++){
                player.spawnParticle(Particle.REDSTONE, SimpleLocation.of(x+0.5, player.getWorld().getHighestBlockYAt(x, z+i)+1.1, z+i+0.5, chunk.world()).getBukkitLocation(), 1, dustOptions);
            }
        }
        if(dx==-1) { // western border of the chunk
            int x = chunk.x()*16 + shifting, z = chunk.z()*16+shifting;
            for(int i=0; i<16-2*shifting; i++){
                player.spawnParticle(Particle.REDSTONE, SimpleLocation.of(x+0.5, player.getWorld().getHighestBlockYAt(x, z+i)+1.1, z+i+0.5, chunk.world()).getBukkitLocation(), 1, dustOptions);
            }
        }
        if(dz==1) { // southern border of the chunk
            int x = chunk.x()*16+shifting, z = chunk.z()*16 + 15 - shifting;
            for(int i=0; i<16-2*shifting; i++){
                player.spawnParticle(Particle.REDSTONE, SimpleLocation.of(x+i+0.5, player.getWorld().getHighestBlockYAt(x+i, z)+1.1, z+0.5, chunk.world()).getBukkitLocation(), 1, dustOptions);
            }
        }
        if(dz==-1) { // northern border of the chunk
            int x = chunk.x() * 16+shifting, z = chunk.z() * 16 + shifting;
            for (int i = 0; i < 16-2*shifting; i++) {
                player.spawnParticle(Particle.REDSTONE, SimpleLocation.of(x + i + 0.5, player.getWorld().getHighestBlockYAt(x + i, z) + 1.1, z + 0.5, chunk.world()).getBukkitLocation(), 1, dustOptions);
            }
        }
    }
    private void checkChunk(SimpleChunkLocation chunk, int dx, int dz, Player player) {
        if(Config.getBoolean("border-display.nation-border", false)&&chunk.getNation()!=null&&chunk.getRelative(dx, dz).getNation() != chunk.getNation()) {
            drawBorder(chunk, dx, dz, player, chunk.getNation().getColor(), 0);
        }else if(Config.getBoolean("border-display.town-border", false)&&chunk.isTownChunk()&&chunk.getRelative(dx, dz).getTown() != chunk.getTown()){
            drawBorder(chunk, dx, dz, player, Color.fromRGB(180, 180, 180), 0);
        }
    }
    @Override
    public void run() {
        int range=Config.getInt("border-display.range", 3);
        for (Player player : Bukkit.getOnlinePlayers()){
            for(int i=-range; i<=range; i++){
                for(int j=-range; j<=range; j++){
                    if(i*i+j*j>range*range) continue;
                    SimpleChunkLocation chunk = SimpleLocation.of(player.getLocation()).getChunkLocation().getRelative(i, j);
                    checkChunk(chunk, 0, 1, player);
                    checkChunk(chunk, 1, 0, player);
                    checkChunk(chunk, 0, -1, player);
                    checkChunk(chunk, -1, 0, player);
                }
            }
        }
    }
}
