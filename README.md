<div align="center">

<img src="docs/media/banner.svg" alt="Voxel Particles" width="100%">

<a href="https://modrinth.com/mod/voxelparticles"><img src="docs/media/badges/modrinth.svg" alt="Modrinth"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/voxelparticles"><img src="docs/media/badges/curseforge.svg" alt="CurseForge"></a>
<a href="https://github.com/addavriance/VoxelParticles"><img src="docs/media/badges/github.svg" alt="GitHub"></a>
<a href="https://fabricmc.net/"><img src="docs/media/badges/fabric.svg" alt="Fabric"></a>
<a href="#supported-versions"><img src="docs/media/badges/minecraft.svg" alt="Minecraft 1.21.4 to 26.2"></a>
<video src="docs/media/break_block.mp4" width="100%"></video>

</div>

Vanilla particles are flat cards that always face the camera. Voxel Particles turns each one into a small
3D model built from the sprite's own pixels. The shape stays exact, holes included: a critical hit cross is
still a cross and a chip of stone is still a chip, but now it has sides you can see when it sways, spins or
lands on the ground.

Nothing to set up. No resource pack, no per-particle support: every sprite particle, vanilla or modded,
is picked up automatically.

## Showcase

<table>
  <tr>
    <td align="center"><video src="docs/media/campfire.mp4"></video><br><sub>Campfire smoke with real depth</sub></td>
    <td align="center" width="50%"><video src="docs/media/eating.mp4"></video><br><sub>Crumbs of the food you eat</sub></td>
  </tr>
  <tr>
    <td align="center"><video src="docs/media/potion.mp4"></video><br><sub>Potion and spell particles</sub></td>
    <td align="center"><video src="docs/media/modded.mp4"></video><br><sub>Particles from other mods</sub></td>
  </tr>
  <tr>
    <td align="center"><video src="docs/media/comparison.mp4"></video><br><sub>On and off, side by side</sub></td>
    <td align="center"><video src="docs/media/config.mp4"></video><br><sub>In-game settings</sub></td>
  </tr>
</table>

## Features

- **Any particle, any mod.** Vanilla and modded sprite particles become 3D with no extra support.
- **Real crumbs.** Block and item particles keep their own piece of the texture and its shape.
- **Four motions.** None, gentle sway (default), spin and tumble.
- **Physical debris.** Falling crumbs tumble in the air and land flat on the ground.
- **Any resource pack.** Custom textures turn 3D just like the vanilla ones.
- **Knows when to back off.** Distant and tiny particles stay flat, meshes are cached, and a watchdog
  pauses the effect for a few seconds if it ever gets too expensive.
- **Settings in game.** A screen made of vanilla widgets, opened from Mod Menu, plus an optional key to
  toggle the effect (unbound by default).

## Compatibility

| Mod or content | Status |
|---|---|
| [Sodium](https://modrinth.com/mod/sodium) | Works |
| [Iris](https://modrinth.com/mod/iris) and shader packs | Works |
| Custom resource packs | Works |
| Particles from other mods | Works, picked up automatically, nothing to register |

> **One exception:** particles that draw their own geometry instead of a sprite stay as they are.
> For example, the slash effects of [Better Combat](https://modrinth.com/mod/better-combat) remain flat.

## Supported versions

Fabric only for now. Client-side, servers don't need it. Requires [Fabric API](https://modrinth.com/mod/fabric-api);
[Mod Menu](https://modrinth.com/mod/modmenu) is optional and adds the settings screen.

| Minecraft | Status |
|---|---|
| 26.2 | Tested |
| 26.1.2 | Tested |
| 1.21.11 | Tested |
| 1.21.8 | Tested (the jar also declares 1.21.6 and 1.21.7, not tested there) |
| 1.21.5 | Tested |
| 1.21.4 | Tested |

## Configuration

Change settings in game through Mod Menu, or edit `config/voxelparticles.json`.

| Option | Default | What it does |
|---|---|---|
| `enabled` | `true` | Turns the effect on and off |
| `motion` | `SWAY` | `BILLBOARD` (faces the camera like vanilla), `SWAY`, `SPIN` or `TUMBLE` |
| `depth` | `1.0` | Thickness, `0.1` to `4`. At `1.0` every pixel is a cube |
| `shading` | `1.0` | How much darker the sides are than the front, `0` to `1` |
| `motionSpeed` | `1.0` | Speed of sway, spin and tumble, `0` to `5` |
| `maxResolution` | `16` | Detail: the largest model size per side, `4` to `64` |
| `lodDistance` | `64` | Particles farther than this many blocks are drawn flat, `0` for no limit |
| `tumbleDebris` | `true` | Crumbs tumble in the air and land flat |
| `blockParticles` | `true` | Makes block particles 3D |
| `itemParticles` | `true` | Makes item particles 3D |
| `excludedSprites` | see below | Sprite ids that always stay flat, `*` works as a wildcard |

<details>
<summary><b>Keeping a particle flat</b></summary>

<br>

Big effects like sweeps and explosions look better flat, so a few sprites are excluded out of the box:

```json
"excludedSprites": [
  "minecraft:sweep_*",
  "minecraft:sonic_boom_*",
  "minecraft:flash",
  "minecraft:explosion_*",
  "minecraft:shriek",
  "minecraft:vibration",
  "minecraft:missingno"
]
```

Add the id of any other particle sprite you want to keep flat, for example `"somemod:glow"`.

</details>

## Development

<details>
<summary><b>Build from source</b></summary>

<br>

Gradle has to run on Java 25 (the Loom version used requires it). The mod itself targets Java 21 or 25
depending on the Minecraft version.

```sh
./gradlew dist                            # every target, jars go to dist/
./gradlew build -Ptargets=1.21.4-fabric   # a single target
```

Project layout:

| Path | Contents |
|---|---|
| `core/` | Code that does not touch Minecraft |
| `mc/<name>/` | API layers shared by several Minecraft versions |
| `loader/` | Loader entry points |
| `versions/<minecraft>/` | One target each; `version.properties` lists the layers it uses |

</details>
