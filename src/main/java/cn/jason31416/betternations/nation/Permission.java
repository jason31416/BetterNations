package cn.jason31416.betternations.nation;

public enum Permission {
    SUBURB_BUILD, // Build in the national territory (outside of towns)
    CHANGE_NATION_ATTRIBUTE, // Change name, color, etc.
    TOWN_BUILD, // Build inside towns
    TOWN_STRUCTURE, // Build/Manage structures inside towns
    TOWN_CLAIM, // Claim land for towns
    TOWN_UNCLAIM, // Unclaim land for towns
    NATION_CLAIM,
    NATION_UNCLAIM,
    NATION_STRUCTURE,
    CHANGE_RANK,
    KICK_PLAYER,
    TOWN_CREATE,
    MANAGE_ARMY
}
