# BetterNations Invasion mechanic

The invasion mechanics of the system is complex but mostly intuitive.
It was designed this way to provide some degree of RTS-like experience while merging some of the native minecraft mechanics to it.

---

## How to go to war

First, 'you' must be a nation to go in war with another nation in this system.
To do so, you can execute the command: `/n declarewar <opponent nation>`.
After this, you and the other nation can attack and occupy each other's territory.

---

## Units

Armies are essential if you wish to invade and conquer other nation's lands.
They can also be used to defend against such invasions.

The basic unit of armies are called "units", and the group of units that moves and attacks together are called "stacks".

Stacks are represented by actual in-game objects such as entities and blocks, and therefore always have a location.

**A stack can have four different states:**

- Stationary: The stack will not perform any action and will remain as a stained glass block of its closest variant to the nation's color. Enemies may attempt to break it, but the units will be spawned as entities to guard the camp. Stationary camps will also automatically attack other enemy armies located inside the chunk.
- Transport: This state of the stack is very vulnerable since the army entity only follows the player. The entity **cannot touch water** during transportation but boats can be used for water crossings. Damages took by the entity will also be reflected on the stack's health. Also, it will be killed if the entity is too far away from the player. This state of unit will consume its supply at double rate.
- Invasion: This state will invade an enemy chunk, and when the chunk's HP reaches 0, the chunk will be occupied by the army's nation. Only national claims can be invaded, and towns can only be sieged. However, this state doesn't deal damage to other stacks located within the chunk, so make sure to bring other guarding armies to protect it. Also, encircling any piece of enemy land and breaking its connectivity toward any of its towns will cause the whole region to be occupied.
- Siege: This state will attack an enemy town. Multiple sieges can be targeting a single town at once. Just like invasions, the town will be occupied when its HP reaches 0. Sieges happens in chunks around the town that belong to the attacker, thus the attacker must at least own a chunk adjacent to the town before they can begin siege.

### Unit controls

To invade, you must transport your stack to the target destination (such as a chunk/a town).

Units can be stored in its `stationary` form. When an allied player mine the block representing the stationary form, it will transform into its `transport` form and follow that player.

> Quick tip: You can apply spped potions to your transport units to make it walk faster, or even build networks of rails to easily move them between your towns.

When in its `transport` form, allied players can hit it to turn it back to `stationary`.

To merge units, shift-click existing `stationary` camps while transporting the other unit.

To invade the enemy chunk that the stack is in, you can right click it and click the actions button on the right top corner of the GUI, then select 'invade'.

> Quick tip: You don't have to invade the chunks one-by-one! In Beta 2.8.4, we've introduced an automatic invasion option in the invasion's GUI.
> 
> You can turn it on to make the unit invade any adjacent chunks that are owned by your enemy upon finishing the current invasion.

To invade an enemy town, you must first at least occupy one of the chunks adjacent to the town. Then, when the unit is there, you can select 'siege' in its action menu.

Both the `invasion` and the `siege` form of the unit have an additional action called `retreat` to abandon any progress made and turn back to its `transport` form.

> Quick tip: If you invade a piece of enemy territory that isn't connected to any enemy towns/any enemy chunks that contains `stationary` form of the stack, you can instantly conquer th whole 'enclave'.

---

## How to defend

There are many ways for you to prevent your enemy from invading your territory.

1. Manual defend: Defenders can break the block-formed stacks (`camp`, `invasion`, and `siege`) to start a `camp-breaking`. Units in it will be converted into their defending mob (As defined in the army.yml). By killing all of the mobs while not leaving the chunk, the camp will be broken and will be gone.
2. Unit combat: You can move your own stack to the same chunk that the enemy stack is in, and they will start a combat and attack each other.
3. Damaging its transport state: By damaging its transport state either by traps or by player, the stack's HP will decrease. If it was killed in that state, the stack will be gone.

Combat is calculated per "army tick", which can be display using `/n nextupdate`. In each "army tick", every unit deal damage to every single enemy units in the same chunk. The damage is calculated from the attacking unit's attack values and the armor type of the defending army. At the end (after every unit has dealt damage), any unit with HP ≤ 0 will be removed.

---

## Invasion preparations

A lot of things must be done before an invasion to prepare for it.

### Building your units

1. Craft the unit's equipment according to the crafting guide: `/n guide` (defined in item/)
2. Put the unit into its corresponding training facility (defined in army.yml)
3. Wait for it to be trained
4. Extract it in the training facility's GUI

### Army supplies

(This may be disabled in config.yml)

Supplies are necessary for units to stay alive. Units will have full supply when it is produced, and it can be consumed as time passes.
To feed the unit, you can put the unit at stationary state in a chunk that contains a "Granary" structure. Right click the structure while holding a food item can convert it into supply points, and the structure will slowly transfer its supply points to all friendly armies located in the same chunk.

***Units will die when it run out of supply!!!***

### Planning out wars before it starts

Some of the useful strategies includes:

- Utilize oceans/rivers for unit transportation - Although they can't swim in water, boats can make the transportation for stacks a lot easier!
- Scout out your enemy's units before the war - Knowing where your enemy's units are at and how strong they are can be very beneficial.
- Your main goal is to take out towns and enemy units - When all of their towns fall and units killed, their whole nation essentially becomes an 'enclave' for you to take!
- Use large stacks for a main invasion route to pierce through enemy towns, and small stacks in automatic invasion to take lands
- Make alliances to make sure that you are not alone in the invasion