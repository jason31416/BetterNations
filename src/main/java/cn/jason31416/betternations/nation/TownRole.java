package cn.jason31416.betternations.nation;

import java.util.Set;

public enum TownRole {
    NONE(),
    RESIDENT(Permission.BUILD),
    MANAGER(Permission.BUILD, Permission.STRUCTURE, Permission.TOWN_CLAIM, Permission.TOWN_UNCLAIM),
    MAYOR(Permission.BUILD, Permission.STRUCTURE, Permission.TOWN_CLAIM, Permission.TOWN_UNCLAIM);
    public final Set<Permission> permissions;
    TownRole(Permission... permissions){
        this.permissions = Set.of(permissions);
    }
    public boolean hasPermission(Permission permission){
        return permissions.contains(permission);
    }
}
