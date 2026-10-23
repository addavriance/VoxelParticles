# Changelog

## 0.2.0

### Added
- Minecraft 1.20.1 support on Fabric and Forge (the Forge build also works on NeoForge 1.20.1)
- Forge support for 1.20.4

### Fixed
- Particles drawn with a more complex vertex format no longer crash the game, for example firing a cannon from Create Big Cannons on 1.21.1 NeoForge

## 0.1.0

### Added
- 3D voxel rendering for sprite particles, vanilla and modded
- Block, item and other particles configured separately
- Motion modes: none, sway (default), spin and tumble
- Falling block and item crumbs tumble in the air and land flat
- Settings screen with General, Blocks, Items and Other tabs
- Optional keybind to toggle the effect (unbound by default)
- `config/voxelparticles.json` with `excludedSprites`, `maxResolution` and `lodDistance`
- Resource pack support

### Performance
- Mesh cache per sprite shape
- Distance-based LOD: far particles stay flat
- Frame time budget for mesh building
