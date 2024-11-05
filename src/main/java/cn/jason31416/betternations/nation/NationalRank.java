package cn.jason31416.betternations.nation;

import cn.jason31416.planetlib.message.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record NationalRank(String name, int weight, Set<Permission> permissions) {
    public static final Map<String, NationalRank> ranks = new HashMap<>();
    public static final NationalRank NONE = new NationalRank("None", 0, Set.of());

    public NationalRank(String name, int weight, Set<Permission> permissions) {
        this.name = name;
        this.weight = weight;
        this.permissions = permissions;
        ranks.put(name.toLowerCase(), this);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public String getDisplayName() {
        return Message.getMessage("nation.rank." + name.toLowerCase(), name).toString();
    }

    public static NationalRank getRank(String name) {
        return ranks.getOrDefault(name.toLowerCase(), NONE);
    }
    public boolean isHigher(NationalRank other){
        return other.weight<this.weight;
    }
}
