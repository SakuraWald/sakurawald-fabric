# Fuji
<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/src/main/resources/assets/fuji/icon.png" width="128" alt="mod icon">

Fuji is a minecraft mod that provides many essential and useful modules for vanilla survival.

> **_If minecraft gets updated, you can mail sakurawald@gmail.com for fuji update reminder._**

> This is a **server-side only** mod, but you can use it in a **single-player world**. (Yes, the single-player world also includes a logic-server)
> - For a server-maintainer: You only need to install this mod at the server-side, and the players don't need to install this mod at their client-side
> - For a player: You only need to install this mod at the client-side, and then you can use the modules in your single-player world.

# Feature
1. **Vanilla-Respect**: all the modules do the least change to the vanilla game (Never touch the game-logic).
2. **Fully-Modular**: you can disable any module completely if you don't like it (The commands and events will all be disabled, just like the code never exists, without any performance issue).
3. **High-Performance**: all the codes are optimized for performance, and the modules are designed to be as lightweight as
   possible (From data-structure, algorithm, lazy-load and cache to improve performance greatly).
4. **Easy-to-Use**: all the modules are designed to be easy to use, and the commands are designed to be easy to remember, even the language file is designed to be easy to understand.

# Modules

_**By default, all the modules are disabled, and this mod does nothing to the vanilla minecraft.**_

<details>
<summary>Click here to display all the modules</summary>

#### PvpModule
provides a command to toggle the pvp status. (/pvp [on/off/status/list])
<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/pvp-toggle.gif" alt="module presentation gif">

#### ResourceWorldModule
create and manage auto-reset resource world for overworld, the_nether and the_end.  (/rw [tp/delete/reset])
<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/resource-world.gif" alt="module presentation gif">

#### ChatModule
A simple chat system.

> - Support mini-message based parser
> - Support mention players
> - Support chat-history -> new joined player can see the chat-history
> - Support per-player message-format settings -> /chat format
> - Support quick-codes
>   - Insert "pos" -> current position
> - Support display
>   - Insert "item" -> item display (support shulker-box)
>   - Insert "inv" -> inventory display
>   - Insert "ender" -> enderchest display
> - Support MainStats placeholders

<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/chat-style.gif" alt="module presentation gif">
<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/display.gif" alt="module presentation gif">

#### TopChunksModule
Provides a command /chunks to show the most laggy chunks in the server.
<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/top-chunks.gif" alt="module presentation gif">

#### BetterFakePlayerModule
(Carpet required) provides some management for fake-player.

> - FakePlayerNameSuffixAndPrefix
> - FakePlayerManipulateLimit
>   - Type `/player who` to see the owner of fake-player
>   - Only the owner of the fake-player can manipulate the fake-player
> - FakePlayerSpawnLimit -> caps can be set to change dynamically 
> - FakePlayerRenewTime
>   - Every fake-player only lives for 12 hrs until you renew it (This avoids the fake-player to be a long-term laggy entity)
>   - Type `/player renew` to renew the fake-player

<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/better-fake-player.gif" alt="module presentation gif">

#### BetterInfoModule
(Carpet required) provides /info entity and add nbt-query for /info block
<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/better-info.gif" alt="module presentation gif">

#### TeleportWarmupModule
provides a teleport warmup for all player-teleport to avoid the abuse of teleport (Including damage-cancel, combat-cancel, distance-cancel).
<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/teleport-warmup.gif" alt="module presentation gif">

#### SkinModule
provides /skin command, and even an option to use local random-skin for fake-player (This fixes a laggy operation when spawning new fake-player and fetching the skin from mojang server).

#### DeathLogModule
provides /deathlog command, which can log and restore the death-log for all players.
<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/death-log.gif" alt="module presentation gif">

#### BackModule
provides /back command (Support smart-ignore by distance).

#### TpaModule
provides /tpa and /tpahere (Full gui support, and easy to understand messages).
<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/tpa.gif" alt="module presentation gif">

#### WorksModule
provides /works command, some bit like /warp but this module provides a very powerful hopper and minecart-hopper counter for every technical player to sample their contraption.
<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/works.gif" alt="module presentation gif">

#### WorldDownloaderModule
provides /download command for every player who wants to download the nearby chunks around him. (Including rate-limit and attack-protection. This command is safe to use, because everytime the command will copy the original-region-file into a temp-file, and only send the temp-file, which does nothing to the original-region-file)
<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/download.gif" alt="module presentation gif">

#### MainStatsModule
This module sums up some basic stats, like: total_playtime, total_mined, total_placed, total_killed and total_moved (We call these 5 stats `MainStats`). You can use these placeholders in ChatStyleModule and MOTDModule

<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/main-stats.gif" alt="module presentation gif">

#### NewbieWelcomeModule
This module broadcasts a welcome-message and random teleport the new player and sets its respawn location.

#### CommandCooldownModule
Yeah, you know what this module does. (Use this module to avoid some heavy-command abuse)

#### MotdModule
A simple MOTD that supports fancy and random motd, and supports some placeholders like MainStats
<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/motd.gif" alt="module presentation gif">

#### HeadModule
provides /head command to buy player heads.

#### ProfilerModule
provides /profiler to sample the server health. (Including os, vm, cpu, ram, tps, mspt and gc)

#### CommandPermissionModule
this module modifies ALL commands (even the command is registered from other mods) and adds a prefix-permission (we called it fuji-permission) for the command. If the player has fuji-permission, then we check fuji-permission for that command, otherwise check the command's original requires-permission. 

> Tips: if you don't know how to determine command-node name, you can just type `/lp group default permission fuji.` and let luckperms tell you what command-node names you can use.
> - Allow the default group to use a command by adding a fuji-permission (e.g. /seed) -> `/lp  group default permission set fuji.seed true`
> - Disallow the default group to use a command by adding a fuji-permission (e.g. /help) -> `/lp  group default permission set fuji.help false`
> - Disallow the default group to use a sub-command from a command by adding a fuji-permission (e.g. /player [player] mount) -> `/lp group default permission set fuji.player.player.mount false`

#### BypassThingsModule
provides options to bypass some annoyed things.

- bypass-chat-rate-limit -> avoid "Kicked for spamming"
- bypass-move-speed-limit -> avoid "Moved too quickly!"
- bypass-max-player-limit -> avoid server max-player limit

#### OpProtectModule
auto deop an op-player when he leaves the server.

#### MultiObsidianPlatformModule
makes every EnderPortal generate its own Obsidian Platform (Up to 128 in survival-mode, you can even use creative-mode to build more Ender Portal and more ObsidianPlatform. Please note that: all the obsidian-platform are vanilla-respect, which means they have the SAME chunk-layout and the SAME behaviour as vanilla obsidian-platform which locates in (100,50,0))
<img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/multi-obsidian-platform.gif" alt="module presentation gif">

#### StrongerPlayerListModule
a fix patch for ServerWorld#PlayerList, to avoid CME in player-list (e.g. sometimes tick-entity and tick-block-entity will randomly crash the server because of player-list CME)

#### WhitelistFixModule
for offline whitelist, this makes whitelist ONLY compare the username and ignore UUID!

#### CommandSpyModule
log command issue into the console.

#### SchedulerModule
where you can add schedule jobs by cron expression, set the random command-list to be executed.

> If `left_trigger_times` < 0, then it means infinity times.

#### ConfigModule
provides `/fuji reload` to reload configs.

#### TestModule
provides `/test` command only for test purpose. (Disable this by default, and you don't need to enable this unless you know what you are doing)

#### HatModule
provides `/hat` command

#### FlyModule
provides `/fly` command

#### GodModule
provides `/god` command

#### LanguageModule
provides multi-language support for your players.
(Disable this module will force all the players to use the default language)

- The default language is en_us.
- Respect the player's client-side language-setting.
- If the player's client-side language-setting is not supported, then use the default language.
- Lazy-load support, which means if a language is not required, then it will not be loaded.
- Dynamic-reload support, you need to enable `ConfigModule` to use reload command.

#### ReplyModule
provides `/reply` command to quickly reply messages to the player who recently `/msg` you! 

#### AfkModule
provides `/afk` command to set your afk status and auto-afk

#### SuicideModule
provides `/suicide` command.

#### CommandInteractiveModule
provides interactive sign command. You can insert `//` plus commands in any sign, and then right-click it to execute the command quickly.

- If the sign contains `//`, then you must press `shift` to edit this sign
- You can add some comments before the first `//`
- You can use all the four lines to insert `//` (Every `//` means one command)
- Placeholder `@u` means the user of this sign

#### SeenModule
provides `/seen` command.

#### MoreModule
provides `/more` command.

#### ExtinguishModule
provides `/extinguish` command.

#### HomeModule
provides `/home` command.

#### PingModule
provides `/ping` command.

#### SystemMessageModule
This module hijacks the vanilla system-message so that you can modify any system-message you want.
(Actually, you can hijack almost all the language messages in the vanilla `en_us.json` language file)
The system messages including:
- Player join and leave server message
- Player advancement message
- Player death message
- Player command feedback
- Player white-list message
- ...

  <img src="https://github.com/SakuraWald/fuji-fabric/raw/1.20.2/.github/images/system_message.gif" alt="module presentation gif">

#### EnderChestModule
provides `/enderchest` command.

#### WorkbenchModule
provides `/workbench` command.

#### EnchantmentModule
provides `/enchantment` command.

#### GrindStoneModule
provides `/grindstone` command.

#### StoneCutterModule
provides `/stonecutter` command.

#### AnvilModule
provides `/anvil` command.

#### BedModule
provides `/bed` command.

#### SitModule
provides `/sit` command and sit interact.

#### CommandAliasModule
provides command alias so that you can alias existed commands in a short name.

#### CommandRewriteModule
provides command rewrite so that you can use regex to rewrite the command players issued.

</details>

# Wiki
See [configuration](https://github.com/sakurawald/fuji-fabric/wiki/Configuration)

See [permission](https://github.com/sakurawald/fuji-fabric/wiki/Permission)

# Reference
At the early stage of this project, we reference some source and ideas from the following projects:
1. https://www.zrips.net/cmi/
2. https://essentialsx.net/
3. https://modrinth.com/mod/essential-commands
4. https://modrinth.com/mod/skinrestorer
5. https://modrinth.com/mod/headindex
6. https://modrinth.com/mod/sit
