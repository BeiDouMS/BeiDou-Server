# handbook 资料梳理

更新时间：2026-08-18 14:25:48 +08:00

这份文档用于后续修改游戏系统时快速定位 handbook、玩法代码、脚本和数据库入口。

## 总入口

- [handbook/](./handbook)
- `!id` 命令读取入口：[`IdCommand`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/command/commands/gm2/IdCommand.java)

当前 `!id` 已接入的类型：

- `map`
- `etc`
- `npc`
- `use`
- `weapon`

## 修改导航表

| 系统 | handbook | 玩法入口 | 数据入口 | 脚本入口 | DB |
| --- | --- | --- | --- | --- | --- |
| 地图 / 场景 | [Map.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Map.txt) | [`MapleMap`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/server/maps/MapleMap.java), [`MapFactory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/server/maps/MapFactory.java), [`ChangeMapHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/ChangeMapHandler.java) | [`MapId`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/constants/id/MapId.java) | [`MapScriptManager`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/scripting/map/MapScriptManager.java), [`MapScriptMethods`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/scripting/map/MapScriptMethods.java) | 主要看 WZ / 地图脚本，通常不依赖独立业务表 |
| 怪物 | [Mob.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Mob.txt) | [`MobSkillFactory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/server/life/MobSkillFactory.java), [`MobAttackInfoFactory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/server/life/MobAttackInfoFactory.java), [`MobDamageMobHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/MobDamageMobHandler.java), [`MobBanishPlayerHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/MobBanishPlayerHandler.java) | [`MobId`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/constants/id/MobId.java), [`MonsterbookDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/MonsterbookDO.java) | 主要是怪物行为数据，通常不靠单独脚本 | [`drop_data`]、[`drop_data_global`]、[`monsterbook`] |
| NPC | [NPC.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/NPC.txt) | [`NpcService`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/service/NpcService.java), [`NPCTalkHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/NPCTalkHandler.java), [`NPCMoreTalkHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/NPCMoreTalkHandler.java), [`NPCShopHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/NPCShopHandler.java) | [`NpcId`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/constants/id/NpcId.java) | [`NPCScriptManager`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/scripting/npc/NPCScriptManager.java), [`NPCConversationManager`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/scripting/npc/NPCConversationManager.java) | [`shops`]、[`shopitems`]、[`playernpcs`]、[`playernpcs_equip`]、[`playernpcs_field`] |
| 任务 | [Quest.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Quest.txt) | [`QuestService`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/service/QuestService.java), [`QuestActionHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/QuestActionHandler.java) | [`QueststatusDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/QueststatusDO.java), [`QuestactionsDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/QuestactionsDO.java), [`QuestrequirementsDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/QuestrequirementsDO.java), [`QuestprogressDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/QuestprogressDO.java) | [`QuestScriptManager`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/scripting/quest/QuestScriptManager.java), [`QuestActionManager`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/scripting/quest/QuestActionManager.java) | [`queststatus`]、[`questactions`]、[`questrequirements`]、[`questprogress`] |
| 技能 | [Skill.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Skill.txt) | [`SkillFactory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/SkillFactory.java), [`Skill`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/Skill.java), [`SkillBookHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/SkillBookHandler.java), [`SkillEffectHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/SkillEffectHandler.java), [`SkillMacroHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/SkillMacroHandler.java) | [`SkillsDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/SkillsDO.java), [`SkillmacrosDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/SkillmacrosDO.java) | 主要看 WZ / 技能数据，通常不靠单独脚本 | [`skills`]、[`skillmacros`]、[`cooldowns`]、[`keymap`] |
| 宠物 | [Pet.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Pet.txt) | [`PetDataFactory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/PetDataFactory.java), [`Pet`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/Pet.java), [`PetCommandHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/PetCommandHandler.java), [`PetFoodHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/PetFoodHandler.java), [`PetLootHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/PetLootHandler.java) | [`PetsDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/PetsDO.java), [`PetignoresDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/PetignoresDO.java) | 主要看宠物数据，通常不靠单独脚本 | [`pets`]、[`petignores`] |
| 道具 - 消耗 / 杂项 / 点券 / 摆设 | [Use.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Use.txt), [Etc.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Etc.txt), [Cash.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Cash.txt), [Setup.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Setup.txt) | [`ItemFactory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/ItemFactory.java), [`Inventory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/Inventory.java), [`Item`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/Item.java), [`UseItemHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/UseItemHandler.java), [`UseCashItemHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/UseCashItemHandler.java), [`ItemPickupHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/ItemPickupHandler.java), [`ItemMoveHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/ItemMoveHandler.java), [`ScrollHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/ScrollHandler.java), [`ScriptedItemHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/ScriptedItemHandler.java), [`ItemRewardHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/ItemRewardHandler.java) | [`InventoryitemsDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/InventoryitemsDO.java), [`InventoryequipmentDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/InventoryequipmentDO.java), [`ShopsDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/ShopsDO.java), [`ShopitemsDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/ShopitemsDO.java), [`SpecialcashitemsDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/SpecialcashitemsDO.java), [`ModifiedCashItemDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/ModifiedCashItemDO.java), [`GiftsDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/GiftsDO.java), [`NxcodeItemsDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/NxcodeItemsDO.java), [`DueypackagesDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/DueypackagesDO.java), [`DueyitemsDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/DueyitemsDO.java) | [`ItemScriptManager`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/scripting/item/ItemScriptManager.java), [`ItemScriptMethods`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/scripting/item/ItemScriptMethods.java) | 主要看 WZ、掉落、脚本与背包流转，DB 主要是背包、商店、点券、赠礼、收件箱 |
| 装备 | [Equip/Weapon.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Weapon.txt), [Equip/Accessory.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Accessory.txt), [Equip/Cap.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Cap.txt), [Equip/Cape.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Cape.txt), [Equip/Coat.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Coat.txt), [Equip/Face.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Face.txt), [Equip/Glove.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Glove.txt), [Equip/Hair.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Hair.txt), [Equip/Longcoat.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Longcoat.txt), [Equip/Pants.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Pants.txt), [Equip/PetEquip.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/PetEquip.txt), [Equip/Ring.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Ring.txt), [Equip/Shield.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Shield.txt), [Equip/Shoes.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Shoes.txt), [Equip/Taming.txt](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/handbook/Equip/Taming.txt) | [`Equip`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/Equip.java), [`EquipSlot`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/constants/inventory/EquipSlot.java), [`EquipType`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/constants/inventory/EquipType.java), [`InventoryMergeHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/InventoryMergeHandler.java), [`InventorySortHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/InventorySortHandler.java) | [`InventoryequipmentDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/InventoryequipmentDO.java), [`InventoryitemsDO`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/dao/entity/InventoryitemsDO.java) | 主要看 WZ 与背包数据，独立脚本通常较少 |

## 补充说明

- `DB` 一栏写的是常见入口，不是完整表清单。
- `handbook` 只负责索引，真正规则通常在玩法代码、脚本和数据库里。
- 如果后续你要改某个系统，我可以直接按这张表继续拆成“改动点清单”。

## 改动清单

| 系统 | 常改文件 | 相关表 | 风险点 |
| --- | --- | --- | --- |
| 地图 / 场景 | [`MapleMap`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/server/maps/MapleMap.java), [`MapFactory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/server/maps/MapFactory.java), [`ChangeMapHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/ChangeMapHandler.java), [`MapScriptManager`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/scripting/map/MapScriptManager.java) | 无固定主表 | 地图脚本和地图数据分离，改切图逻辑时要同时看脚本和地图加载 |
| 怪物 | [`MobSkillFactory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/server/life/MobSkillFactory.java), [`MobAttackInfoFactory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/server/life/MobAttackInfoFactory.java), [`MobDamageMobHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/MobDamageMobHandler.java) | [`drop_data`]、[`drop_data_global`]、[`monsterbook`] | 掉落、图鉴、技能行为常常彼此联动，改一处容易漏另一处 |
| NPC | [`NpcService`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/service/NpcService.java), [`NPCTalkHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/NPCTalkHandler.java), [`NPCShopHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/NPCShopHandler.java), [`NPCScriptManager`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/scripting/npc/NPCScriptManager.java) | [`shops`]、[`shopitems`]、[`playernpcs`]、[`playernpcs_equip`]、[`playernpcs_field`] | 对话、商店、玩家商人是三条链路，改动前先确认是脚本还是服务逻辑 |
| 任务 | [`QuestService`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/service/QuestService.java), [`QuestActionHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/QuestActionHandler.java), [`QuestScriptManager`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/scripting/quest/QuestScriptManager.java) | [`queststatus`]、[`questactions`]、[`questrequirements`]、[`questprogress`] | 任务状态机最容易漏状态回写，改奖励和完成条件时要核对持久化 |
| 技能 | [`SkillFactory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/SkillFactory.java), [`SkillEffectHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/SkillEffectHandler.java), [`SkillMacroHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/SkillMacroHandler.java) | [`skills`]、[`skillmacros`]、[`keymap`]、[`cooldowns`] | 技能效果、快捷键和冷却常常互相影响，改数值后要一起验 |
| 宠物 | [`PetDataFactory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/PetDataFactory.java), [`PetFoodHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/PetFoodHandler.java), [`PetLootHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/PetLootHandler.java) | [`pets`]、[`petignores`] | 自动拾取、喂养、指令都可能影响玩家体验，改动要注意默认行为 |
| 道具 / 背包 | [`ItemFactory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/ItemFactory.java), [`Inventory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/Inventory.java), [`ItemPickupHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/ItemPickupHandler.java), [`UseItemHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/UseItemHandler.java), [`ItemRewardHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/ItemRewardHandler.java) | [`inventoryitems`]、[`inventoryequipment`]、[`shops`]、[`shopitems`]、[`specialcashitems`]、[`modified_cash_item`]、[`gifts`]、[`dueypackages`]、[`dueyitems`] | 背包、商店、点券、收件箱是多入口共用，改动时要防止类型分支不一致 |
| 装备 | [`Equip`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/Equip.java), [`EquipSlot`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/constants/inventory/EquipSlot.java), [`ScrollHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/ScrollHandler.java), [`InventoryMergeHandler`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/net/server/channel/handlers/InventoryMergeHandler.java) | [`inventoryequipment`]、[`inventoryitems`] | 强化、穿戴、整理都共享装备实例，改属性逻辑时要防止复制/持久化不同步 |

## 快速查找顺序

1. 先查 [handbook/](./handbook) 定位 ID。
2. 再查对应系统的玩法入口和 handler。
3. 如果涉及数据存储，再看 DB 表和 DAO。
4. 如果涉及对话、任务、地图、道具脚本，再查 `scripting/`。

## 改动优先级清单

如果你要先开刀一个系统，建议按这个顺序：

1. **NPC / 商店 / 对话**
   - 入口最集中，脚本和服务边界清晰
   - 适合先熟悉对话流、商店流、脚本流
2. **道具 / 背包**
   - 改动频率高，覆盖面大
   - 适合先确认背包、掉落、商店、点券的共用链路
3. **任务**
   - 逻辑清晰，但状态回写多
   - 适合系统性梳理状态机与脚本联动
4. **技能**
   - 数值和效果影响面广
   - 适合在熟悉角色/战斗流程后再动
5. **地图 / 怪物**
   - 和 WZ、脚本、战斗逻辑耦合较强
   - 适合最后做大范围联动调整
6. **宠物 / 装备**
   - 细分逻辑多，依赖背包和掉落系统
   - 适合在前面几类打通后一起收口

## 系统改动模板

每次改一个系统时，可以按这个模板推进：

### 1. 先定位资料

- 查 handbook 里的 ID / 名称
- 明确要改的是哪一类对象
- 记录关联对象：NPC、地图、任务、道具、技能、怪物

### 2. 再定位入口

- 找玩法入口：`service` / `server` / `client`
- 找网络入口：`net/server/channel/handlers`
- 找脚本入口：`scripting/*`

### 3. 再定位数据

- 查对应的 `dao/entity`
- 查相关 `mapper`
- 查是否有缓存、工厂类、单例类

### 4. 再确认联动点

- 是否会影响其他系统
- 是否会影响 DB 持久化
- 是否会影响脚本调用
- 是否会影响客户端显示或提示

### 5. 最后验证

- 本地编译是否通过
- 相关流程是否能跑通
- 是否需要补 handbook 或脚本说明

## 装备词条系统（第一阶段）

- 数据迁移：[`V1.11.6__create_equipment_affix.sql`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/resources/db/migration/V1.11.6__create_equipment_affix.sql)
- 装备运行时词条：[`EquipmentAffix`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/EquipmentAffix.java)
- 配置读取：[`EquipmentAffixConfigLoader`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/EquipmentAffixConfigLoader.java)
- 装备持久化：[`ItemFactory`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/ItemFactory.java)

当前已支持装备品质和词条实例的数据库读写，以及怪物/反应堆装备掉落的品质和词条生成；Boss 伤害和无视防御已在 [`MapleMap.damageMonster`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/server/maps/MapleMap.java) 的统一伤害入口应用，经验率、金币率、掉落率和 Boss 减伤已接入角色倍率/受击流程。

词条系统现在区分：

- 装备品质：保存在 `inventoryequipment.rarity`，决定词条数量和允许的最高词条品质。
- 词条类型：保存在 `inventory_equipment_affix.affix_code`，表示属性或特殊效果。
- 词条品质：保存在 `inventory_equipment_affix.affix_tier`，独立决定该条词条的数值区间。
- T5–T8 数值区间由 [`V1.11.13__add_affix_tier_5_to_8.sql`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/resources/db/migration/V1.11.13__add_affix_tier_5_to_8.sql) 补齐；品质允许的最高 T 等阶仍由 `equipment_rarity_config.max_affix_tier` 控制。
- 对应迁移：[`V1.11.7__split_affix_tier.sql`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/resources/db/migration/V1.11.7__split_affix_tier.sql)
- 词条命名：[`V1.11.8__create_affix_names.sql`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/resources/db/migration/V1.11.8__create_affix_names.sql)，按词条类型和 T1–T8 分别配置名称键。
- 玩家查看：装备拾取成功后会通过聊天提示显示词条；普通玩家可使用 `@inspect` 列出装备栏，使用 `@inspect <装备栏位>` 查看完整词条，也可使用 `@affix` 别名。命令由 [`V1.11.9__add_inspect_command.sql`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/resources/db/migration/V1.11.9__add_inspect_command.sql) 和 [`V1.11.10__add_affix_command_alias.sql`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/resources/db/migration/V1.11.10__add_affix_command_alias.sql) 注册。
- 固定属性词条与混沌值已在运行时分离：`Equip` 的原始属性保存装备本体/卷轴变化，词条贡献根据 `inventory_equipment_affix` 动态叠加到对外属性；数据库兼容加载旧装备时会从已保存总值中扣除词条贡献，混沌卷轴通过现有 getter/setter 只改变本体值。
- 词条生命周期：[`V1.11.11__add_affix_lock.sql`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/resources/db/migration/V1.11.11__add_affix_lock.sql) 增加锁定状态；[`V1.11.12__add_affix_lifecycle_commands.sql`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/resources/db/migration/V1.11.12__add_affix_lifecycle_commands.sql) 注册 `@reroll <栏位>`、`@lockaffix <栏位> <词条序号>` 和 `@salvage <栏位>`。重铸费用按装备品质以 1.8 倍指数增长，按锁定词条数量再乘以 1.6 的指数倍率；重铸保留已锁定词条，锁定状态持久化，分解装备返还金币。
- 词条工匠 NPC：复用已有 `9977777` NPC 外观，在自由市场 `910000000` 增加一个 NPC 位置；脚本在该地图显示词条服务菜单，在其他地图保留原开发者 NPC 功能。配置位于 [`910000000.img.xml`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/wz/Map.wz/Map/Map9/910000000.img.xml) 和 [`9977777.js`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/scripts/npc/9977777.js)。
- 分解金币由 [`EquipmentValueCalculator.java`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/EquipmentValueCalculator.java) 统一计算：WZ/NPC 基础售价按 60%计入，再乘装备品质倍率并叠加词条 T 等阶溢价；强化属性和混沌变化不计入，结果按百位取整并限制在金币上限内。

### 当前状态（2026-08-18）

- **已完成并可试玩**：装备品质随机生成、20 种词条类型、T1–T8 命名和数值区间、词条持久化、词条贡献与装备本体属性分离、拾取提示、`@inspect`/`@affix`、重铸、锁定、分解，以及自由市场词条工匠 NPC。
- **词条生效范围**：词条属于具体装备实例，数据通过 `inventoryitemid` 关联；属性词条叠加到装备属性，经验/掉落/金币、Boss 增伤、Boss 减伤和无视防御只统计 `InventoryType.EQUIPPED` 中已穿戴的装备，背包装备不会提供角色效果。
- **无视防御实现**：由于 v83 客户端提交的是已计算伤害，服务端在 [`MapleMap.damageMonster`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/server/maps/MapleMap.java) 使用怪物防御计算有限的伤害补偿；装备无视率封顶 80%，该效果造成的最终伤害最多为原伤害的 2 倍，不直接修改怪物防御属性。
- **数据库更新**：服务端启动时由 Flyway 自动执行未完成迁移；T5–T8 区间由 [`V1.11.13__add_affix_tier_5_to_8.sql`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/resources/db/migration/V1.11.13__add_affix_tier_5_to_8.sql) 补齐。
- **掉落来源概率**：[`V1.11.14__add_equipment_drop_source_weights.sql`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/resources/db/migration/V1.11.14__add_equipment_drop_source_weights.sql) 增加普通掉落、Boss 掉落、副本掉落和百宝箱四套品质权重；普通怪物沿用原概率，Boss 与副本概率已按需求对调，百宝箱装备使用独立的 GACHAPON 权重。生成入口由 [`EquipmentDropSource`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/client/inventory/EquipmentDropSource.java) 标识。
- **百宝箱装备**：[`GachaponService`](/home/qtf8184/ms/BeiDou-Server.worktrees/check-compile/gms-server/src/main/java/org/gms/service/GachaponService.java) 抽到装备时会先随机基础属性，再生成品质和词条后放入背包；非装备奖励仍沿用原有百宝箱奖池流程。
- **暂缓事项**：装备词条系统暂不继续扩展新词条。后续如重新开发，优先处理重铸/分解事务与并发保护、交易和商店状态校验、NPC 服务层抽取，以及基于实战数据的 T5–T8 数值平衡。
