# FPS Boost (Fabric 1.21.11)

A client-side, adaptive performance mod. It measures your real FPS every
second and automatically dials down (or restores) render distance,
particles, biome blend radius, and graphics quality to help you hit a
target frame rate — useful on lower-power/mobile Java setups.

**Note on "200+ fps":** the mod will push toward your target, but the
actual ceiling is still your device/GPU driver. On very weak hardware
(e.g. some mobile Java launchers running software rendering) even an
empty world may cap out well below 200. This mod removes wasted
rendering work; it can't exceed what your hardware can push.

## How to build (matches your existing GitHub Actions phone workflow)

1. Create a new GitHub repo, push this whole folder to it (root of repo,
   same as your Research Client project).
2. Go to the Actions tab, run "Build FPS Boost Mod" (or just push to
   `main` — it runs automatically).
3. Download the `fps-boost-jar` artifact once the run finishes.
4. Drop the `.jar` into your `mods/` folder alongside Fabric API.

**Requires Fabric API** to be installed too — this mod depends on it.

## In-game

- Press **F8** to toggle the mod on/off (shows a chat message).
- Config is saved to `config/fpsboost.json` — you can hand-edit
  `targetFps`, `minRenderDistance`, `maxRenderDistance`, or disable
  `adaptiveMode` to freeze settings where they are.

## If the GitHub Actions build fails

Minecraft's internal method names (via Yarn mappings) shift between
versions. If the build log shows a "cannot find symbol" error on a
method like `getViewDistance()` or `getGraphicsMode()`, that name
moved in this specific 1.21.11 mapping build. Paste the error back to
me — I'll adjust the exact call to match.
