package cn.jason31416.betternations.nation;

import cn.jason31416.planetlib.message.Message;

import java.util.Set;

public enum TownRole {
    NONE(),
    GREENCARD(Permission.BUILD, Permission.STRUCTURE),
    RESIDENT(Permission.BUILD, Permission.STRUCTURE),
    MANAGER(Permission.BUILD, Permission.STRUCTURE, Permission.TOWN_CLAIM, Permission.TOWN_UNCLAIM),
    MAYOR(Permission.BUILD, Permission.STRUCTURE, Permission.TOWN_CLAIM, Permission.TOWN_UNCLAIM);
    public final Set<Permission> permissions;
    TownRole(Permission... permissions){
        this.permissions = Set.of(permissions);
    }
    public boolean hasPermission(Permission permission){
        return permissions.contains(permission);
    }
    public String getName(){
        return Message.getMessage("term.town-role."+name().toLowerCase()).toString();
    }
    public TownRole demote(){
        if(this==GREENCARD) return NONE;
        if(this==RESIDENT) return NONE;
        if(this==MANAGER) return RESIDENT;
        if(this==MAYOR) return MANAGER;
        return NONE;
    }
    public TownRole promote(){
        if(this==GREENCARD) return GREENCARD;
        if(this==RESIDENT) return MANAGER;
        if(this==MANAGER) return MAYOR;
        if(this==MAYOR) return MAYOR;
        return RESIDENT;
    }

}
