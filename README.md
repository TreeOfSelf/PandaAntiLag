![icon](https://github.com/user-attachments/assets/9af1fccb-fbec-490d-a0db-0d5be3d04c08)

# PandaAntiLag

## Description

Stop common causes for lag on your Fabric server.
This mod aims to prevent common causes of lag by allowing you to limit the amount of projectile entities in an area, prevent someone from having too many ender pearls loaded, and also stagger mob ticks in an area with too many mobs.

This will stop people from making lag machines based on creating too many entities.
It will also stop people from using too much RAM or lagging a server out by loading too many chunks with ender pearls.
And the staggering of mob ticks allows for players to have thousands of mobs in an area, and only their mobs experience lag while the server TPS stays at 20.0

## Configuring

config/PandaAntiLag.json generated at runtime, an entry for each dimension will be made.

```
{
  "regionSize": 6,  //The square dimensions of how many chunks to consider a "lag region"
  "regionBuffer": 1, //How far lag regions bleed into each other for checking entity counts
  "minimumRegionMobs": 75, //Minimum amount of mobs in a region before it can be affected by mob tick staggering
  "projectileMax": 150, //Max amount of single type of projectile entity (like arrows) allowed in lag region
  "mobStaggerLenience": 200, //Higher values = mob count affects mob staggering less
  "tickTimeLenience": 10, //Higher values = MSPT affects mob staggering less
  "updateInterval": 10000, //How often to allow chunks to count entities (ms)
  "enderPearlUpdateInterval": 10000, //How often to check for a players max enderpearls (ms)
  "maxEnderPearlsPerPlayer": 20 //Max amount of ender pearls a player can have loaded
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
