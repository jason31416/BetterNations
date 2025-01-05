# BetterNations gameplay

In essence, the plugin provides a protection and invasion system for players to claim and manage claims.

---

## Nations & Towns

Lands in the plugin are owned by nations.
Nations are the essential entity of the BetterNations system.
They are basically a collection of players, lands, towns, etc.

To create a nation, you simply need to prepare some virtual currency.
Pick a good spot for your capital (usually your base is fine) and type `/n create <name your nation!>`.
If a beacon was placed at your position, the creation was successful.

Nation colors are randomized upon creation, but it can still be changed via `/n color` command.
To rename your nation, use `/n rename`. If you wish to remove the nation and everything it owns (including town claims, etc.), you may use `/n disband`.

Towns are essentially a part of its nation, and they provide special functionalities that will be further elaborated in the invasion section.
In addition to the capital, other towns can be created with the `/n town create <name>` command.

Town cores can be moved with `/n town core`, and you can teleport to your town by `/n tp <town>`.
If you wish to remove a town, you can type `/n town remove` while standing in the town (Though the last town cannot be removed since each nation must contain at least one town).

**To simplify:**
- Nations: A collection of lands, towns and players
- Towns: A subset of the lands in a nation

---

## Claiming

Land claiming is based on chunks (16x16 block regions covering from sky to bedrock). Their boundaries can be seen by pressing `F3+G` in game.

You must be in a nation to claim, since the lands are owned by the nations instead of directly owned by you.

To claim a chunk that wasn't claimed by someone else, you can stand in the target chunk and use the command `/n claim` (which will cost virtual currency depend on your server's configuration)

To un-claim a chunk, you can use the command `/n unclaim`.
In addition, for larger claiming operations, there are some subcommands that can help such as `/n claim square <size>` and `/n claim auto`.

Towns works similarly, but towns must be connected so every new claim must be adjacent to an existing town. To claim/unclaim for towns, you may use `/n town claim` and `/n town unclaim` respectively.

After successfully claiming, you should see a title displayed at the center of your screen, which will also be displayed every time you enter a claimed chunk.

**To simplify:**
- Claims are based on chunks
- Towns must be contiguous

---

## Nation types, ranks, and permissions

When adding more players into the game, things get more complicated. 

### Inviting players

To join a nation, the player must use `/n join <nation name>` to send a request to join a nation.
Then, a [resolution](#resolutions) will be created and must be passed for the player to join.

If you wish to leave a nation, you can use `/n leave`. If you wish to kick other players, `/n kick <player>` will create a resolution (Although the highest ranked player in the nation cannot be kicked but must first be demoted).


### Ranks & Permissions

Nations have ranks, which can vary depends on the type of the nation (which will be explained in the next section).
The founder of the nation is automatically assigned to the highest position, while newly joined players will be put at the lowest level.
Ranks of a player can be changed via `/n rank <player> <rank>`. This will create a resolution.
Note that since a nation can only have one single person as the highest ranked "owner", the current owner would be demoted if another player's rank is set to it.

By default, every player in the nation have permission to build in the national claims (chunks claimed by the nation but not claimed by any town).
Allied nation members also have the building permission in the national claims.
Any neutral/enemy players will not be allowed to build in the claims.

Different rank can also affect player's political rights, such as the right to vote, managing structures/armies, claiming and unclaiming.

In addition to the national-wide rank, each town also have its own role system. Players inside the nation can have either none, resident, manager or mayor role.
Mayor is typically the founder of the town, but it can also be transferred. Role-managements are done inside the town core GUI (can be opened by right-clicking the core), and only the mayor can change members' roles.

There is a special role called "greencard" which is granted to trusted people outside of the nation, which have the same permissions as the resident role. *This feature is WIP.*

A few other details include:
TNTs cannot affect blocks inside towns (but can still blow up in national claims, but note that TNT cannons must be utilized to do so, as enemies cannot directly place those explosives).
Furthermore, pistons cannot push across different nations' territories.

**To simplify:**
- Members of a nation have roles that determine their rights and permissions inside the nation.
- Towns have a separate role system in addition to the national one, which determine if a player can access things inside town claims.

### Resolutions

Resolutions are typically an action that requires some consideration and might be required to be voted to pass.
In some political systems such as autocracy, the dictator is the only one that is allowed to vote, while in others such as democracy, every citizen may vote.

### Nation Types

As mentioned before, different nations can be of different types, or "political systems".
The type of the nation determine its power structure, such as different ranks.

***Autocracy:***
This is one of the most extreme form of government, in which the only one that can make decisions is the dictator.
Other players may be allowed to build and manage structures/claims etc., but the dictator have complete power over them.

***Monarchy:***
This system allows the king to assign ranks such as general and knights to other members.
Although the king still have absolute power over other members and can demote/kick them at any time, generals and knights have some power and can make basic decisions such as player invitation/claiming/etc.
This is the default type.

***Republic:***
This system allows a group of players, or "representatives" to pass resolutions. Although the president is leading the nation, representatives may always vote him/her down and replace with a new president.
In addition to this, representatives also have many more powers in the nation.

***Democracy:***
This system allows every citizen to vote (although by default newly-joined members doesn't have any right at all, not even building, and must be promoted to a citizen).
In this system, 1/2 of the members must vote for a resolution to pass, so its not really efficient in any perspective.

***Anarchy:***
This system is the other extreme. Every citizen in the nation is granted every right, which means that anyone in the nation can declare war, kick players, demote/promote/invite/kick others, etc.
It can be somewhat chaotic but if your nation only contains your best friends, it can also be an effective choice.

---

## Wars, armies, and treaties

The plugin's military and technology system can be somewhat complicated, but they are pretty straightforward once you tried playing around with them.

You can always invade other nations and conquer their land or towns. Before any attack, war must be declared via `/n declarewar <other nation>`.

### Recipes

The plugin introduced some new recipes that can be crafted via crafting table or furnaces.
To see the crafting recipes, you may always use `/n recipe` to open a GUI of the recipe book.

### Armies

Armies are essential if you wish to invade and conquer other nation's lands. They can also be used to defend against such invasions.

The basic unit of armies are called "units", and the group of units that moves and attacks together are called "stacks".

**They can have four different states:**

- Stationary: The stack will not perform any action and will remain as a stained glass block of its closest variant to the nation's color. Enemies may attempt to break it, but the units will be spawned as entities to guard the camp. Stationary camps will also automatically attack other enemy armies located inside the chunk.
- Transport: This state of the stack is very vulnerable since the army entity only follows the player. The entity **cannot touch water** during transportation but boats can be used for water crossings. Damages took by the entity will also be reflected on the stack's health. Also, it will be killed if the entity is too far away from the player. This state of unit will consume its supply at double rate.
- Invasion: This state will invade an enemy chunk, and when the chunk's HP reaches 0, the chunk will be occupied by the army's nation. Only national claims can be invaded, and towns can only be sieged. However, this state doesn't deal damage to other stacks located within the chunk, so make sure to bring other guarding armies to protect it. Also, encircling any piece of enemy land and breaking its connectivity toward any of its towns will cause the whole region to be occupied.
- Siege: This state will attack an enemy town. Multiple sieges can be targeting a single town at once. Just like invasions, the town will be occupied when its HP reaches 0. Sieges happens in chunks around the town that belong to the attacker, thus the attacker must at least own a chunk adjacent to the town before they can begin siege.

To control the units, you can right click the unit's entity/block form and a GUI should be opened displaying options. By default, there is a control button on the top right of the GUI that allows switching between states.

To merge units, shift-click existing stationary camps while transporting the other unit.

**Building Armies**

First, the equipments of the corresponding army type must be crafted, and then you can put the equipment into the corresponding training structures such as barracks.

**Unit combat**

There are several ways to damage and destroy enemy units, including damaging its transport entity, "breaking camps" (e.g. breaking its block form and deal damages to the "guards")

There is also another method that doesn't require player involvements. As mentioned before, when armies are at the "stationary" state (as a stained glass block), they will automatically attack other block formed (non-transport) enemy armies in the same chunk.
Red particles should spawn between the two units that are in combat.

Combat is calculated per "army tick", which can be display using `/n nextupdate`. In each "army tick", every unit deal damage to every single enemy units in the same chunk. The damage is calculated from the attacking unit's attack values and the armor type of the defending army. At the end (after every unit has dealt damage), any unit with HP ≤ 0 will be removed.

### Treaties

Treaties are collections of terms. Each term can affect two nations, and a treaty must be signed by every affected nation for it to take effect.
Any affected nation can veto the treaty.

To create a treaty, anyone can type `/n treaty create <treaty name>`. After that, using `/n treaty view <treaty name>` to view/edit/sign the treaty.

**Treaty terms:**
- Ally: Two nations will become allies, allowing each other to access their national claims. Alliance relation can be revoked via `/n revoke <nation>`.
- Peace: Two nations will stop war and become neutral.
- Other terms are WIP

### Army supplies

Supplies are necessary for units to stay alive. Units will have full supply when it is produced, and it can be consumed as time passes.
To feed the unit, you can put the unit at stationary state in a chunk that contains a "Granary" structure. Right click the structure while holding a food item can convert it into supply points, and the structure will slowly transfer its supply points to all friendly armies located in the same chunk.

***Units will die when it run out of supply!!!***

