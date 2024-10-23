package cn.jason31416.planetlib.wrapper;

import java.util.Set;

public class SimpleLandGroup {
    private final Set<SimpleChunkLocation> locations = new java.util.HashSet<>();
    public SimpleLandGroup(Set<SimpleChunkLocation> locations) {
        this.locations.addAll(locations);
    }
    public SimpleLandGroup() {}
    public SimpleLandGroup addLand(SimpleChunkLocation location){
        locations.add(location);
        return this;
    }
    public Set<SimpleChunkLocation> getLands() {
        return locations;
    }
}
