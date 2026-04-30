# 假人模块 (Fake Player Module)

## 概述

假人模块用于在指定地图显示穿着随机装扮的虚假玩家。这些假人通过 Netty 发包实现，不占用真实玩家连接，仅做展示用途。

## 功能特性

- ✅ 随机装扮生成（脸型、发型、装备、武器）
- ✅ 随机职业和等级
- ✅ 随机人气值
- ✅ 随机公会名称
- ✅ 随机玩家名字
- ✅ 支持多个假人在同一地图
- ✅ 通过 Netty 发包实现，无需真实连接
- ✅ REST API 管理接口

## 架构设计

```
org.gms.client.fake/
├── FakePlayer.java              # 假人对象
├── FakePlayerLook.java          # 假人外观数据
├── FakePlayerRandomizer.java    # 随机生成器
├── FakePlayerManager.java       # 管理器（单例）
├── FakePlayerConfig.java        # 配置类
└── FakePlayerPacketCreator.java # 数据包构造器

org.gms.service/
└── FakePlayerService.java       # 服务层集成

org.gms.controller/
└── FakePlayerController.java    # REST API控制器
```

## 配置说明

在 `application.yml` 中添加：

```yaml
fake-player:
  enabled: true
  maps:
    100000000: 5   # 维多利亚港 - 5个假人
    101000000: 3   # 射手村 - 3个假人
    102000000: 3   # 魔法密林 - 3个假人
  allow-chat: true
  chat-probability: 0.1
  show-on-enter: true
```

## API 接口

### 获取状态
```
GET /api/latest/fake-player/status
```

### 在地图生成假人
```
POST /api/latest/fake-player/spawn/{mapId}?count=3
```

### 生成随机假人
```
POST /api/latest/fake-player/spawn/random/{mapId}
```

### 获取地图假人列表
```
GET /api/latest/fake-player/map/{mapId}
```

### 获取假人详情
```
GET /api/latest/fake-player/{fakeId}
```

### 移除假人
```
DELETE /api/latest/fake-player/{fakeId}
DELETE /api/latest/fake-player/map/{mapId}
```

### 刷新假人
```
POST /api/latest/fake-player/refresh/{mapId}
```

### 预览随机假人
```
GET /api/latest/fake-player/preview
```

## 技术实现

### 数据包结构

假人使用与真实玩家相同的 SPAWN_PLAYER 包格式：

```
[Int] 角色ID (负数，避免与真实玩家冲突)
[Byte] 等级
[String] 名称
[String/Bytes] 公会信息
[Short] Buff数量
[Short] 职业ID
[Bytes] 外观数据 (性别、肤色、脸型、发型、装备)
[Int] 物品效果等
[Position] 地图坐标
[Byte] 动作/站姿
...
```

### ID 分配

- 真实玩家ID: > 0
- 假人ID: < 0 (从 -1000000 递减)

### 外观随机化

支持以下随机属性：

| 属性 | 范围 |
|------|------|
| 性别 | 男/女 |
| 肤色 | 0-10 (正常、暗色、蓝色、绿色等) |
| 脸型 | 20000-20029 |
| 发型 | 30000-30290 |
| 职业 | 所有冒险家职业 (1-4转) |
| 等级 | 根据职业转职阶段确定范围 |
| 装备 | 帽子、衣服、鞋子、手套、武器等 |
| 人气 | -100 ~ 1000 |

## 使用示例

### Java 代码

```java
// 获取管理器
FakePlayerManager manager = FakePlayerManager.getInstance();

// 在地图生成3个假人
List<FakePlayer> fakes = manager.spawnFakes(100000000, 3, null);

// 生成指定职业的假人
FakePlayer warrior = FakePlayerRandomizer.generateRandom(Job.HERO);
manager.spawnFake(100000000, new Point(100, 0), warrior);

// 展示假人给玩家
manager.showAllFakesInMap(playerClient, 100000000);

// 移除地图假人
manager.removeAllFakes(100000000);
```

### REST API

```bash
# 在市中心生成5个假人
curl -X POST "http://localhost:8686/api/latest/fake-player/spawn/100000000?count=5"

# 获取假人列表
curl "http://localhost:8686/api/latest/fake-player/map/100000000"

# 预览随机假人
curl "http://localhost:8686/api/latest/fake-player/preview"
```

## 扩展点

1. **自定义名字词库**: 修改 `FakePlayerRandomizer.NAME_PREFIXES/SUFFIXES`
2. **自定义装备池**: 修改 `EQUIP_POOLS` 和 `WEAPON_POOLS`
3. **自定义行为**: 继承 `FakePlayerService` 实现更多交互

## 注意事项

1. 假人ID使用负数，避免与真实玩家冲突
2. 假人数据不持久化，服务器重启后需要重新生成
3. 假人无AI行为，仅做静态展示
4. 假人点击信息返回有限的基本信息
5. 需要配置地图ID才能显示假人

## 未来扩展

- [ ] 假人简单移动行为
- [ ] 假人表情/动作
- [ ] 假人聊天内容自定义
- [ ] 假人持久化存储
- [ ] 假人定时刷新任务
- [ ] 假人交互事件处理
