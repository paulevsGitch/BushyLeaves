<h1 align="center">Bushy Leaves</h1>
<p align="center">
  <img src="https://github.com/paulevsGitch/BushyLeaves/blob/main/screen.png" />
</p>

A small mod that will make leaves bushy and will enhance leaves rendering.
Leaves inside tree will be not rendered, and leaves that are visible will have
additional cross model that will make them more "bushy". Leaves covered with snow
will have special snowy texture.

This mod will automatically add leaves from other mods (if they are extending
Leaves block). If leaves are not added or added incorrectly you can add them
with the config (configs/bushyleaves.json);

### Default config:
```json
{
	"exclude": [],
	"include": []
}
```
Syntax of adding and removing blocks is same.

### Example of adding leaves:
```json
"include": [
	{ "block": "minecraft:wool" },
	{ "block": "minecraft:wood", "meta": 1 },
]
```
In this example leaves will be added to all 16 wool blocks (for each meta)
and for wood (tree log) with meta 1.

### Example of removing leaves:
```json
"exclude": [
	{ "block": "minecraft:leaves", "meta": 1 },
	{ "block": "othermod:mushroom" },
]
```
In this example leaves will be removed from vanilla spruce leaves (meta 1) and
from other mod block with ID "othermod:mushroom".