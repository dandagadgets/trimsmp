# TrimSMP

Armor trims grant themed special abilities and powers on your Paper server.

## How it works

- **Full set required.** A power only activates when all four armor pieces
  (helmet, chestplate, leggings, boots) carry the **same trim pattern**.
  Mismatched patterns, or missing a piece, means no power.
- **Material sets the tier.** The trim **material** of the *weakest* piece in
  the set decides the power's tier, 1 (Common) through 5 (Legendary):

  | Tier | Materials |
  |---|---|
  | 1 - Common | Iron, Copper, Quartz |
  | 2 - Uncommon | Gold, Redstone, Lapis |
  | 3 - Rare | Amethyst, Emerald |
  | 4 - Epic | Diamond |
  | 5 - Legendary | Netherite |

  Mixing a netherite-trimmed helmet with three iron-trimmed pieces still only
  gives tier 1 - a genuinely matching, high-quality set is required to reach
  the top tier.
- **Manual powers.** A few patterns (Vex, Flow) have an active ability instead
  of (or in addition to) a passive one. Trigger it by **sneaking + swapping
  hands** (default F key) while the set is active; it's on a per-pattern
  cooldown that gets shorter at higher tiers.
- `/trimsmp info` shows your currently active power. `/trimsmp list` shows
  every pattern's theme. `/trimsmp reload` (requires `trimsmp.admin`, default
  op) reloads `config.yml`.

## The 18 powers

| Pattern | Power |
|---|---|
| Sentry | Spots nearby hostile mobs (makes them glow) and steels you with Resistance when surrounded. |
| Vex | Sneak+swap-hands blinks you forward a short, obstacle-aware distance. |
| Wild | Regeneration on natural ground, a speed burst while sprinting through it. |
| Coast | Water Breathing and swim speed (Dolphin's Grace) while in water. |
| Dune | Permanent Fire Resistance, plus Haste in desert/badlands biomes. |
| Ward | A damage-reducing shield that procs on a cooldown, easing the next hit. |
| Eye | Endermen won't be provoked by you; permanent Night Vision. |
| Tide | Haste and Night Vision while underwater, like a portable conduit. |
| Snout | Piglins and piglin brutes treat you as neutral. |
| Rib | Bonus damage against undead mobs; shrugs off the Wither effect. |
| Spire | Automatic Slow Falling and reduced (or negated, at high tier) fall damage. |
| Wayfinder | Action-bar compass to your bed/spawn; a speed boost on a sustained sprint. |
| Shaper | Haste while holding a mining tool. |
| Silence | A chance for hostile mobs to simply not notice you. |
| Raiser | Buffs nearby allied players with Strength and Speed. |
| Host | Regeneration while near villagers. |
| Flow | Sneak+swap-hands launches you forward on a gust of wind, landing softly. |
| Bolt | Bonus damage and a lightning flash on critical hits; immune to real lightning. |

Every numeric value (radii, durations, cooldowns, percentages) is tunable per
pattern in `config.yml` without recompiling.

## Building

This is a standard Paper plugin (Maven, Java 21, targeting Paper API 1.21.x).

```
mvn package
```

The built jar lands in `target/TrimSMP-1.0.0.jar` - drop it in your server's
`plugins/` folder.

**Note on this repository's dev container:** this particular sandboxed
environment's network policy blocks `repo.papermc.io`, so `mvn package`
cannot be verified to build *here*. The code was written and manually
reviewed against the Paper/Bukkit API, but you should run the build once
yourself (locally, in CI, or in an environment that can reach PaperMC's
repository) before deploying. If `paper-api` fails to resolve because the
pinned version in `pom.xml` (`<paper.api.version>`) has since been
superseded, bump it to whatever the current Paper API snapshot/release is
for your target Minecraft version.

## Requirements

- Java 21+
- Paper (or a Paper fork) 1.21.x - the plugin relies on Paper's `ArmorMeta`
  trim API and Adventure text components, not just vanilla Bukkit/Spigot.
