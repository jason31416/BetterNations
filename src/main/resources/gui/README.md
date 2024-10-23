# GUI Editing guideline:

- To fully translate the plugin, you must edit both the /lang and the /gui folders.
- You can always edit the arrangements of the items or even creating new items in a gui,
but make sure to keep the key of the item if you want them to function as normal.
### Basic GUI structure:
```yaml
testing-gui: # The key of the gui, you shouldn't change them.
  window:
    title: "" # The title of the gui
    size: 54 # The size of the GUI, usually must either be 1-9 or a multiple of 9 (max. 54)
  container:
    border: # This is an example of decorative items, they doesn't have any action associated with them
      name: " "
      material: GRAY_STAINED_GLASS_PANE
      # 'slots' can be a list of numbers or a single number representing the slots that the item will occupy
      slots: [1, 2, 3, 4, 5, 6, 7, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53]
    confirm:
      name: "&2Hello" # Bukkit-like color codes are supported in GUIs in all strings, but minimessage isn't
      lore: [] # This is optional, if the lore is empty you may always delete this field
      material: GREEN_WOOL # This must be an existing item's ID (capitalized name)
      slot: 8
    close:
      name: "&cCLOSE"
      material: BARRIER
      slot: 0
```
