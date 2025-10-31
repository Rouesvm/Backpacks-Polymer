<div align="center">

# ![**Banner!**](https://cdn.modrinth.com/data/cached_images/62c875db9287b8218707bb3fc1f117a30fcf1466.png) 

**A server-sided mod that adds backpacks using [Polymer](https://github.com/Patbox/polymer) by [Patbox]([https://modrinth.com/user/Patbox](https://modrinth.com/user/Patbox))**

 <a href="https://modrinth.com/mod/serverbacksnow"><img src="https://img.shields.io/badge/dynamic/json?color=158000&amp;label=downloads&amp;prefix=+%20&amp;query=downloads&amp;url=https://api.modrinth.com/v2/project/E7IsBILg&amp;logo=modrinth"></a> 

</div>

## Overview

<details>
<summary>Additions</summary>
  
### This mod adds 5 items.
- A small backpack, that has **9 slots**.
- A medium backpack, that has **18 slots**.
- A large backpack, that has **27 slots**.
- A ender backpack, a backpack that lets you **access your ender chest from anywhere!**
- A global backpack, **a universal inventory** that is only **connected with anyone that has this item!**
### It also adds an enchantment!
- Capacity, an enchantment that increases your backpack's storage by 9 additional slots per level! (Note: Can be applied on medium and large backpack only).

Also, Regular backpacks are dyeable like bundles!
</details>

<details>
<summary>Recipes</summary>
  
![Small Backpack Recipe](https://cdn.modrinth.com/data/cached_images/6a814116d700faead8d5054e3be8333921024dc9.png)
![Medium Backpack Recipe](https://cdn.modrinth.com/data/cached_images/47e038118e5e04647a704c7b62cccafd2fbedcb9.png)
![Large Backpack Recipe](https://cdn.modrinth.com/data/cached_images/064a51ae9be575a12f9c4d2e62ac57df51197808.png)
![Ender Backpack Recipe](https://cdn.modrinth.com/data/cached_images/5e6d4f863900fe40f1497f16411e32704bd83af7.png)
![Global Backpack Recipe](https://cdn.modrinth.com/data/cached_images/ed6bd26666a5e2f9fd647c8334f15065dc8a7787.png)

</details>

<details>
<summary>Adding Textures</summary>

Write **/polymer generate** command.
Resource pack will be located in your server folder as polymer-resourcepack.zip or you can enable auto-host in polymer's config.

</details>


<details>
<summary>Adding Geyser Support</summary>

-- Note, if you are using mod version 1.2.4b or higher, geyser support is now automatic. (so you don't need to do this.)

**If you are using version 1.21.4 or higher.**


1. Download the bedrock resource pack from the [modrinth release page](https://modrinth.com/mod/serverbacksnow/version/1.2+release), scroll down till you find files and download backpack.zip.
2. Copy the bedrock resource pack to config/Geyser-Fabric/packs
3. Optionally add the [language files](https://github.com/Rouesvm/Backpacks-Polymer/tree/1.21.4/src/main/resources/data/serverbackpacks/lang) to Geyser for localization, follow the guides [here](https://geysermc.org/wiki/geyser/translations#modifyingadding-minecraft-java-translations).
4. That's it!

**If you are using version 1.21.1 or lower.**

1. Build your java resource pack with [polymer](https://polymer.pb4.eu/polymer/resource-packs/#building-resource-pack).
2. Convert it to a bedrock resource pack using tools like [java2bedrock.sh](https://github.com/Kas-tle/java2bedrock.sh).
3. Follow the [guide](https://geysermc.org/wiki/geyser/faq/#general-questions) on Geyser for bedrock edition players to load the pack.
4. Optionally add the [language files](https://github.com/Rouesvm/Backpacks-Polymer/tree/1.21.1/src/main/resources/data/serverbackpacks/lang) to Geyser for localization, follow the guides [here](https://geysermc.org/wiki/geyser/translations#modifyingadding-minecraft-java-translations).

</details>

## Example in game
![Small Backpack](https://cdn.modrinth.com/data/E7IsBILg/images/55b280b5523d01d6a5c34479ec36f5d07ca24c5c.png)

## Credits
Fork based on [Server Backpacks](https://modrinth.com/mod/servback) by [Octal](https://modrinth.com/user/Octal), although as of right now, all of the code is rewritten.

Taken code from and what code:
* Cosmetic display entity code from ([Filament](https://modrinth.com/mod/filament))
* Bedrock blockstate registry code from ([Hydraulic](https://github.com/GeyserMC/Hydraulic))
