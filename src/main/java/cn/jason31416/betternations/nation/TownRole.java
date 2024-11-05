package cn.jason31416.betternations.nation;

import java.util.Set;

public enum TownRole {
    NONE(),
    RESIDENT(Permission.TOWN_BUILD),
    MANAGER(Permission.TOWN_BUILD, Permission.TOWN_STRUCTURE, Permission.TOWN_CLAIM, Permission.TOWN_UNCLAIM),
    MAYOR(Permission.TOWN_BUILD, Permission.TOWN_STRUCTURE, Permission.TOWN_CLAIM, Permission.TOWN_UNCLAIM);
    public final Set<Permission> permissions;
    TownRole(Permission... permissions){
        this.permissions = Set.of(permissions);
    }
    public boolean hasPermission(Permission permission){
        return permissions.contains(permission);
    }
}
