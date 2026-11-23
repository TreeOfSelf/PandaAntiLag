![icon](https://github.com/user-attachments/assets/9af1fccb-fbec-490d-a0db-0d5be3d04c08)

# PandaAntiLag

## Description

Stop common causes for lag on your Fabric server.
This mod aims to prevent common causes of lag by allowing you to limit the amount of projectile entities in an area, prevent someone from having too many ender pearls loaded, stagger mob ticks in an area with too many mobs, and stagger vehicle ticks in an area with too many vehicles.

This will stop people from making lag machines based on creating too many entities.
It will also stop people from using too much RAM or lagging a server out by loading too many chunks with ender pearls.
And the staggering of mob ticks allows for players to have thousands of mobs in an area, and only their mobs experience lag while the server TPS stays at 20.0

## Configuring

config/PandaAntiLag.json generated at runtime

```
{
  "regionSizeBits": 2, //Lag regions are 2^this chunks wide (2 = 4x4 chunks)
  "regionBuffer": 1, //How many neighbouring regions are counted together with a region
  "minimumRegionMobs": 75, //Minimum amount of mobs in a region before it can be affected by mob tick staggering
  "minimumRegionVehicle": 75, //Minimum amount of vehicles in a region before it can be affected by vehicle tick staggering
  "projectileMax": 150, //Max amount of single type of projectile entity (like arrows) allowed in lag region
  "mobStaggerLenience": 200, //Higher values = mob count affects mob staggering less
  "vehicleStaggerLenience": 200, //Higher values = vehicle count affects vehicle staggering less
  "tickTimeLenience": 10, //Higher values = MSPT affects mob and vehicle staggering less (crowded regions only)
  "portalSpawnCapMultiplier": 2.0, //Portals stop spawning piglins when nearby monsters >= minimumRegionMobs * this (0 = off)
  "maxPortalLoadsPerPlayer": 10, //Most portal-loaded areas one player can be responsible for (0 = off)
  "movementWhileThrottled": true, //Falling, swimming and path-following mobs still move on ticks the throttle skips
  "skipPushWhileThrottled": true, //Those movement-only ticks don't push the mobs around them
  "movementTickMinSpeed": 0.2, //Horizontal speed (blocks/tick) that earns a throttled mob a movement tick
  "despawnWithoutPlayers": true, //Mobs that would despawn far from players also despawn when their dimension has no players
  "enderPearlUpdateInterval": 10000, //How often to check for a players max enderpearls (ms)
  "maxEnderPearlsPerPlayer": 20, //Max amount of ender pearls a player can have loaded
  "waitForChunksBeforePlayerTick": true, //A player's tick waits for the chunks around them instead of freezing the server
  "waitForChunksBeforePlayerMove": true, //Same for movement packets
  "asyncEntityUpgrade": true, //Upgrade old stored mobs on a worker thread when their chunk loads
  "overloadStartMspt": 50.0, //Last resort: when the median of the last 100 ticks is over this, slow every mob (0 = off, otherwise at least 50)
  "overloadStepMspt": 5.0, //Each step over overloadStartMspt makes mobs tick one time less often
  "overloadMaxDivisor": 10, //Most the last resort can slow mobs
  "spawnOverloadMaxDivisor": 1, //Also slow natural spawning while the last resort is active, at most this much (1 = never)
  "poiSearchBackoffTicks": 0 //Villagers ignore beds/job sites/bells they couldn't reach for this long, plus up to the same again (0 = vanilla)
}
```

## Try it out
`hardcoreanarchy.gay`   (Deathban Anarchy)  
`sky.hardcoreanarchy.gay`   (Skyblock Anarchy)

## Support

[Support discord here!]( https://discord.gg/3tP3Tqu983)

## Try it out 

Demo server at **hardcoreanarchy.gay**


## License

[CC0](https://creativecommons.org/public-domain/cc0/)
