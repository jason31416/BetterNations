package cn.jason31416.betternations.manager.map;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleWorld;
import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.C;

import java.util.*;

public class MapDisplayManager {
    public static Map<SimpleWorld, MarkerSet> nations=new HashMap<>(), towns=new HashMap<>();
    private static Color adaptColor(org.bukkit.Color clr){
        return new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), clr.getAlpha()/255F);
    }
    public static class ChunkCluster extends HashSet<SimpleChunkLocation> {
        public SimpleChunkLocation corner=null;
        public int minx=Integer.MAX_VALUE, maxx=Integer.MIN_VALUE, minz=Integer.MAX_VALUE, maxz=Integer.MIN_VALUE;
        @Override
        public boolean add(SimpleChunkLocation chunk){
            if(corner==null||corner.x()>chunk.x()||(corner.x()==chunk.x()&&corner.z()>chunk.z())){
                corner = chunk;
            }
            minx=Math.min(minx, chunk.x());
            maxx=Math.max(maxx, chunk.x());
            minz=Math.min(minz, chunk.z());
            maxz=Math.max(maxz, chunk.z());
            return super.add(chunk);
        }
        private Vector2d getLeftTopCorner(SimpleChunkLocation chunk){
            return Vector2d.from(chunk.x()*16+0.5, chunk.z()*16+0.5);
        }
        private Vector2d getRightTopCorner(SimpleChunkLocation chunk){
            return Vector2d.from(chunk.x()*16+15.5, chunk.z()*16+0.5);
        }
        private Vector2d getRightBottomCorner(SimpleChunkLocation chunk){
            return Vector2d.from(chunk.x()*16+15.5, chunk.z()*16+15.5);
        }
        private Vector2d getLeftBottomCorner(SimpleChunkLocation chunk){
            return Vector2d.from(chunk.x()*16+0.5, chunk.z()*16+15.5);
        }
        public Shape drawShape(){
            Shape.Builder shape=Shape.builder();
            shape.addPoint(getLeftTopCorner(corner));
            SimpleChunkLocation.Direction dir = SimpleChunkLocation.Direction.EAST;
            SimpleChunkLocation cur=corner;
            do{
                SimpleChunkLocation nxt;
                switch (dir) {
                    case EAST -> {
                        if(this.contains(nxt=cur.getRelative(0, -1))){
                            shape.addPoint(getLeftTopCorner(cur));
                            dir= SimpleChunkLocation.Direction.NORTH;
                            cur=nxt;
                        }else if(this.contains(nxt=cur.getRelative(1, 0))){
                            cur=nxt;
                        }else{
                            shape.addPoint(getRightTopCorner(cur));
                            dir= SimpleChunkLocation.Direction.SOUTH;
                        }
                    }
                    case WEST -> {
                        if(this.contains(nxt=cur.getRelative(0, 1))){
                            shape.addPoint(getRightBottomCorner(cur));
                            dir= SimpleChunkLocation.Direction.SOUTH;
                            cur=nxt;
                        }else if(this.contains(nxt=cur.getRelative(-1, 0))){
                            cur=nxt;
                        }else{
                            shape.addPoint(getLeftBottomCorner(cur));
                            dir= SimpleChunkLocation.Direction.NORTH;
                        }
                    }
                    case SOUTH -> {
                        if(this.contains(nxt=cur.getRelative(1, 0))){
                            shape.addPoint(getRightTopCorner(cur));
                            dir= SimpleChunkLocation.Direction.EAST;
                            cur=nxt;
                        }else if(this.contains(nxt=cur.getRelative(0, 1))){
                            cur=nxt;
                        }else{
                            shape.addPoint(getRightBottomCorner(cur));
                            dir= SimpleChunkLocation.Direction.WEST;
                        }
                    }
                    case NORTH -> {
                        if(this.contains(nxt=cur.getRelative(-1, 0))){
                            shape.addPoint(getLeftBottomCorner(cur));
                            dir= SimpleChunkLocation.Direction.WEST;
                            cur=nxt;
                        }else if(this.contains(nxt=cur.getRelative(0, -1))){
                            cur=nxt;
                        }else{
                            shape.addPoint(getLeftTopCorner(cur));
                            dir= SimpleChunkLocation.Direction.EAST;
                        }
                    }
                }
            }while(!cur.equals(corner)||dir!=SimpleChunkLocation.Direction.EAST);
            return shape.build();
        }
    }
    private static SimpleChunkLocation getRoot(Map<SimpleChunkLocation, SimpleChunkLocation> p, SimpleChunkLocation loc){
        if(p.get(loc).equals(loc)) return loc;
        SimpleChunkLocation ret = getRoot(p, p.get(loc));
        p.put(loc, ret);
        return ret;
    }
    public static void drawNations(Collection<Nation> allNations){
        Map<SimpleChunkLocation, SimpleChunkLocation> p=new HashMap<>();
        // 并查集:P
        for(Nation n: allNations) for(SimpleChunkLocation loc: new HashSet<>(n.nationalChunks)) {
            p.put(loc, loc);
            for(SimpleChunkLocation adj: loc.getAdjacentChunks()){
                if(adj.getNation()==n&&p.containsKey(adj)&&!getRoot(p, loc).equals(getRoot(p, adj))){
                    p.put(getRoot(p, loc), getRoot(p, adj));
                }
            }
        }
        Map<SimpleChunkLocation, ChunkCluster> clusters = new HashMap<>();
        for(SimpleChunkLocation loc: p.keySet()){
            SimpleChunkLocation root=getRoot(p, loc);
            if(!clusters.containsKey(root)){
                clusters.put(root, new ChunkCluster());
            }
            clusters.get(root).add(loc);
        }
        // draw the markers
        for(ChunkCluster i: clusters.values()){
            Nation nation = i.corner.getNation();
            if(nation==null) continue;
            // find negative spaces
            Map<SimpleChunkLocation, SimpleChunkLocation> np = new HashMap<>();
            for(int j=i.minx-1;j<=i.maxx+1;j++) for(int k=i.minz-1;k<=i.maxz+1;k++){
                SimpleChunkLocation loc = SimpleChunkLocation.of(j, k, i.corner.world());
                if(!i.contains(loc)) {
                    np.put(loc, loc);
                    for (SimpleChunkLocation adj : loc.getAdjacentChunks()) {
                        if (np.containsKey(adj) && !getRoot(np, loc).equals(getRoot(np, adj))) {
                            np.put(getRoot(np, loc), getRoot(np, adj));
                        }
                    }
                    for (SimpleChunkLocation adj : loc.getDiagAdjacentChunks()) {
                        if (np.containsKey(adj) && !getRoot(np, loc).equals(getRoot(np, adj)) && (getRoot(np, adj).equals(getRoot(np, SimpleChunkLocation.of(i.minx-1, i.minz-1, i.corner.world())))||getRoot(np, loc).equals(getRoot(np, SimpleChunkLocation.of(i.minx-1, i.minz-1, i.corner.world()))))) {
                            np.put(getRoot(np, loc), getRoot(np, adj));
                        }
                    }
                }
            }
            Map<SimpleChunkLocation, ChunkCluster> negativeClusters = new HashMap<>();
            for(int j=i.minx-1;j<=i.maxx+1;j++) for(int k=i.minz-1;k<=i.maxz+1;k++){
                SimpleChunkLocation loc = SimpleChunkLocation.of(j, k, i.corner.world());
                if(!np.containsKey(loc)) continue;
                if(getRoot(np, loc).equals(getRoot(np, SimpleChunkLocation.of(i.minx-1, i.minz-1, i.corner.world())))) continue;
                if(!negativeClusters.containsKey(getRoot(np, loc))){
                    negativeClusters.put(getRoot(np, loc), new ChunkCluster());
                }
                negativeClusters.get(getRoot(np, loc)).add(loc);
            }
            Shape[] holes=new Shape[negativeClusters.size()];
            int idx=0;
            for (ChunkCluster j: negativeClusters.values()){
                holes[idx++]=j.drawShape();
            }
            // create shapemarker
            if(!nations.containsKey(i.corner.world())) nations.put(i.corner.world(), MarkerSet.builder().label(Message.getMessage("bluemap.markerset.nation").toString()).build());
            ShapeMarker marker = ShapeMarker.builder()
                    .shape(i.drawShape(), Config.getInt("bluemap.nation-layer-height"))
                    .holes(holes)
                    .lineColor(adaptColor(nation.getColor().setAlpha(255)))
                    .fillColor(adaptColor(nation.getColor().setAlpha(120)))
                    .lineWidth(Config.getInt("bluemap.line-width"))
                    .label(nation.getName())
                    .maxDistance(Config.getInt("bluemap.max-view-distance"))
                    .build();
            nations.get(i.corner.world()).put(nation.getId().toString()+"."+i.corner.x()+"-"+i.corner.z(), marker);
        }
    }
    public static void drawTowns(Collection<Town> allTowns){
        Map<SimpleChunkLocation, SimpleChunkLocation> p=new HashMap<>();
        // 并查集:P
        for(Town t: allTowns) for(SimpleChunkLocation loc: new HashSet<>(t.getTownChunks())) {
            p.put(loc, loc);
            for(SimpleChunkLocation adj: loc.getAdjacentChunks()){
                if(adj.getTown()==t&&p.containsKey(adj)&&!getRoot(p, loc).equals(getRoot(p, adj))){
                    p.put(getRoot(p, loc), getRoot(p, adj));
                }
            }
        }
        Map<SimpleChunkLocation, ChunkCluster> clusters = new HashMap<>();
        for(SimpleChunkLocation loc: p.keySet()){
            SimpleChunkLocation root=getRoot(p, loc);
            if(!clusters.containsKey(root)){
                clusters.put(root, new ChunkCluster());
            }
            clusters.get(root).add(loc);
        }
        // draw the markers
        for(ChunkCluster i: clusters.values()){
            Town town = i.corner.getTown();
            if(town==null) continue;
            // find negative spaces
            Map<SimpleChunkLocation, SimpleChunkLocation> np = new HashMap<>();
            for(int j=i.minx-1;j<=i.maxx+1;j++) for(int k=i.minz-1;k<=i.maxz+1;k++){
                SimpleChunkLocation loc = SimpleChunkLocation.of(j, k, i.corner.world());
                if(!i.contains(loc)) {
                    np.put(loc, loc);
                    for (SimpleChunkLocation adj : loc.getAdjacentChunks()) {
                        if (np.containsKey(adj) && !getRoot(np, loc).equals(getRoot(np, adj))) {
                            np.put(getRoot(np, loc), getRoot(np, adj));
                        }
                    }
                    for (SimpleChunkLocation adj : loc.getDiagAdjacentChunks()) {
                        if (np.containsKey(adj) && !getRoot(np, loc).equals(getRoot(np, adj)) && (getRoot(np, adj).equals(getRoot(np, SimpleChunkLocation.of(i.minx-1, i.minz-1, i.corner.world())))||getRoot(np, loc).equals(getRoot(np, SimpleChunkLocation.of(i.minx-1, i.minz-1, i.corner.world()))))) {
                            np.put(getRoot(np, loc), getRoot(np, adj));
                        }
                    }
                }
            }
            Map<SimpleChunkLocation, ChunkCluster> negativeClusters = new HashMap<>();
            for(int j=i.minx-1;j<=i.maxx+1;j++) for(int k=i.minz-1;k<=i.maxz+1;k++){
                SimpleChunkLocation loc = SimpleChunkLocation.of(j, k, i.corner.world());
                if(!np.containsKey(loc)) continue;
                if(getRoot(np, loc).equals(getRoot(np, SimpleChunkLocation.of(i.minx-1, i.minz-1, i.corner.world())))) continue;
                if(!negativeClusters.containsKey(getRoot(np, loc))){
                    negativeClusters.put(getRoot(np, loc), new ChunkCluster());
                }
                negativeClusters.get(getRoot(np, loc)).add(loc);
            }
            Shape[] holes=new Shape[negativeClusters.size()];
            int idx=0;
            for (ChunkCluster j: negativeClusters.values()){
                holes[idx++]=j.drawShape();
            }
            // create shapemarker
            if(!towns.containsKey(i.corner.world())) towns.put(i.corner.world(), MarkerSet.builder().label(Message.getMessage("bluemap.markerset.town").toString()).build());
            ShapeMarker marker = ShapeMarker.builder()
                    .shape(i.drawShape(), Config.getInt("bluemap.town-layer-height"))
                    .holes(holes)
                    .lineColor(new Color(170, 170, 170))
                    .fillColor(new Color(170, 170, 170, 0.3F))
                    .lineWidth(Config.getInt("bluemap.line-width"))
                    .label(town.getName())
                    .maxDistance(Config.getInt("bluemap.town-max-view-distance"))
                    .build();
            towns.get(i.corner.world()).put(town.getId().toString()+"."+i.corner.x()+"-"+i.corner.z(), marker);
        }
    }
    private static BukkitRunnable getRunnable(BlueMapAPI api){
        return new BukkitRunnable() {
            public void run() {
                nations.clear();
                towns.clear();
                drawNations(Nation.nations.values());
                drawTowns(Town.towns.values());
                for (SimpleWorld i : nations.keySet()) {
                    api.getWorld(i.getBukkitWorld()).ifPresent(world -> {
                        for (BlueMapMap map : world.getMaps()) {
                            map.getMarkerSets().put("bn.nations", nations.get(i));
                        }
                    });
                }
                for (SimpleWorld i : towns.keySet()) {
                    api.getWorld(i.getBukkitWorld()).ifPresent(world -> {
                        for (BlueMapMap map : world.getMaps()) {
                            map.getMarkerSets().put("bn.towns", towns.get(i));
                        }
                    });
                }
                BetterNations.instance.getLogger().info("\033[32mHooked into Bluemap!\033[0m");
            }
        };
    }
    public static void init(){
        BlueMapAPI.onEnable(api -> getRunnable(api).runTaskAsynchronously(BetterNations.instance));
    }
    public static void unload(){
        BlueMapAPI.getInstance().ifPresent(api -> {
            for(BlueMapMap map: api.getMaps()){
                map.getMarkerSets().remove("bn.nations");
                map.getMarkerSets().remove("bn.towns");
            }
        });
    }
    public static Set<Nation> nationUpdates=new HashSet<>();
    public static Set<Town> townUpdates=new HashSet<>();
    public static void updateNation(Nation nation){
        nationUpdates.add(nation);
    }
    public static void updateTown(Town town){
        townUpdates.add(town);
    }
    public static void update(){
        BlueMapAPI.getInstance().ifPresent(api -> {
            Set<Nation> nu=new HashSet<>(nationUpdates);
            Set<Town> tu=new HashSet<>(townUpdates);
            nationUpdates.clear();
            townUpdates.clear();
            for(BlueMapMap map: api.getMaps()){
                if(map.getMarkerSets().containsKey("bn.nations")){
                    for(String marker: new HashSet<>(map.getMarkerSets().get("bn.nations").getMarkers().keySet())){
                        try{
                            UUID id = UUID.fromString(marker.split("\\.")[0]);
                            Nation n = Nation.getNation(id);
                            if(nu.contains(n)) map.getMarkerSets().get("bn.nations").remove(marker);
                        }catch (Exception ignored){}
                    }
                }
                if(map.getMarkerSets().containsKey("bn.towns")){
                    for(String marker: new HashSet<>(map.getMarkerSets().get("bn.towns").getMarkers().keySet())){
                        try{
                            UUID id = UUID.fromString(marker.split("\\.")[0]);
                            Town t = Town.getTown(id);
                            if(tu.contains(t)) map.getMarkerSets().get("bn.towns").remove(marker);
                        }catch (Exception ignored){}
                    }
                }
            }
            drawNations(nu);
            drawTowns(tu);
        });
    }
    public static void reload(){
        unload();
        BlueMapAPI.getInstance().ifPresent(api -> getRunnable(api).runTaskAsynchronously(BetterNations.instance));
    }
}
