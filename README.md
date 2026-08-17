本项目基于Cosmic来的汉化和优化，Cosmic地址：https://github.com/P0nk/Cosmic   

# BeiDou由来
北斗卫星导航系统（Beidou Navigation Satellite System，简称：BDS，又称为：COMPASS，中文音译名称：BeiDou）是中国自行研制的全球卫星导航系统，也是继GPS、GLONASS之后的第三个成熟的卫星导航系统。北斗卫星导航系统（BDS）和美国GPS、俄罗斯GLONASS、欧盟GALILEO，是联合国卫星导航委员会已认定的供应商。  
北斗卫星导航系统由空间段、地面段和用户段三部分组成，可在全球范围内全天候、全天时为各类用户提供高精度、高可靠定位、导航、授时服务，并且具备短报文通信能力。经过多年发展，北斗系统已成为面向全球用户提供全天候、全天时、高精度定位、导航与授时服务的重要新型基础设施。北斗系统定位导航授时服务，通过30颗卫星，免费向全球用户提供服务，全球范围水平定位精度优于9米、垂直定位精度优于10米，测速精度优于0.2米/秒、授时精度优于20纳秒。  
北斗这一词对于中国来说，有着特殊的意义。北斗，是中国的一个卫星导航系统，也是中国自主研制的第一个卫星导航系统。既然小伙伴说这个项目也要整个天体的名字，想了半天，就叫北斗好了！这也意味着我们要做的比HeavenMS和Cosmic更加优秀和强大！  

# 开发进展
[开发进展](https://github.com/BeiDouMS/BeiDou-Server/wiki/%E5%BC%80%E5%8F%91%E8%BF%9B%E5%BA%A6)

# gms-server 服务端
- 已实现自动创建数据库，执行初始化sql脚本，只要保证mysql是启动的即可  
- 已开放api端口8686
- 已引入swagger，swagger地址：http://localhost:8686/swagger-ui/index.html
- 接口由版本控制，如：v1 v2 v3。默认的swagger标签为name = ApiConstant.LATEST，默认的RequestMapping为："/" + ApiConstant.LATEST + "/xx"
- 接口如果增加新版本且接口不需要更新，只需要把ApiConstant.LATEST指向新版本即可。如果部分接口不兼容，需要把旧接口的Tag和RequestMapping都改成指定版本，如：ApiConstant.V1。其他的，只需要把ApiConstant.LATEST指向新版本即可。
- 支持多语言，脚本和wz针对多语言会读取不同的路径：wz-zh-CN，wz-en-US，script-zh-CN，script-en-US
- 不支持MySQL8以下的版本

## 开发环境
- OpenJDK 21：https://jdk.java.net/archive/
- Intellij IDEA 2023.3及以上：https://www.jetbrains.com/idea/
- MySQL8：https://github.com/SleepNap/NapMysqlTool/releases/latest 或者 https://downloads.mysql.com/archives/community/
- Maven：https://maven.apache.org/download.cgi
- git：https://git-scm.com/downloads
- DBeaver：https://dbeaver.io/download/ 或者 Navicat Lite：https://www.navicat.com/en/download/navicat-premium-lite

# gms-ui web端

## 开发环境部署

请根据自身实际情况选择性跳过已完成的步骤

**1 安装 NodeJS v20.15.0 （LTS 版）**

下载地址：https://nodejs.org/dist/v20.15.0/node-v20.15.0-x64.msi

**2 安装 Yarn**

```shell
npm install -g yarn
```

> 如提示npm命令不存在，可能是安装NodeJS时，安装程序配置的环境变量还没有生效，小白请使用重启大法

**3 初始化前端开发环境**

在命令行进入 gms-ui 目录，然后执行命令

```shell
yarn install
```

**4 启动开发环境**

```shell
yarn dev
```

## 备注
web中所有的图片均需要联网获取，感谢 https://maplestory.io 提供给的图片接口！  

# 客户端
服务端和客户端已经打包好了在[Release](https://github.com/BeiDouMS/BeiDou-Server/releases)中，大家直接下载即可。  
如果想下载北斗客户端的**早期Beta的版本**，可以[点击这里了解更多](https://github.com/BeiDouMS/BeiDou-Server/wiki/%E5%8C%97%E6%96%97%E5%AE%A2%E6%88%B7%E7%AB%AF%E5%8F%91%E5%B8%83) 

# docker
原服务端中docker相关配置已移除，配置已独立到[新的仓库](https://github.com/BeiDouMS/BeiDou-docker)，且支持[镜像拉取](https://github.com/BeiDouMS/BeiDou-docker/pkgs/container/beidou-server-all)。想参加docker开发，欢迎在新仓库进行pr。  
[了解更多](https://github.com/BeiDouMS/BeiDou-docker)

# Wiki
发现很多同学的问题基本在Wiki中都有答案，欢迎大家去看看。另外如果发现Wiki中没有的问题，欢迎提issue，或直接补充。已将Wiki开放为所有人都可以编辑。  
[Wiki地址](https://github.com/BeiDouMS/BeiDou-Server/wiki)

# 扩展运行时 / SoloMapling

北斗支持通过 **SPI 插件**加载外部扩展，而不必把大框架永久打进主 jar。当前首个完整插件是 [SoloMapling](https://github.com/MadaraGameDev/SoloMapling)（冒险岛 v83 假人 / 城镇 / 自由市场 / 练级 bot 框架；上游源码在 SoloMapling 仓库，本仓库提供可构建的 `solomapling-plugin` 模块）。

更细的模块说明见：`gms-server/src/main/java/org/gms/extension/README.md`。

## 机制概览

| 组件 | 作用 |
|------|------|
| `extension-api` | 宿主与插件共用的 SPI：`ServerExtension`、`HostRuntime`、配置 / 事件总线 / 命令注册 |
| `org.gms.extension.runtime` | 北斗实现：`BeiDouHostRuntime`、`ExtensionLoader`（扫描 `plugins/*.jar` + `ServiceLoader`） |
| `solomapling-plugin` | SoloMapling 完整框架，入口类 `SoloMaplingExtension` |
| `gms-server/plugins/` | 插件 jar 放置目录（jar 本身不入库，保留 `.gitkeep`） |

**启动顺序：** Spring Boot 就绪 → `ServerManager` 构建 `HostRuntime` 并 `load(plugins/)`（各插件 `onLoad`）→ `Server.init()` 拉起登录服与频道 → `notifyServerReady()`（各插件 `onServerReady`）→ SoloMapling 按配置延迟约 1s 执行 `EnvironmentManager` 多波刷图。

插件只依赖 `extension-api`；游戏逻辑仍由北斗宿主提供。假人是真实的 `Character` 对象，因此 gms-server 内仍保留少量 hook（`BotClient`、地图 / 交易 / 雇佣商店、命令桥接等）。

## 配置

在 `gms-server/src/main/resources/application.yml`：

```yaml
solomapling:
  plugins-enabled: true
  plugins-dir: plugins              # 相对 gms-server 工作目录
  spawn-bots-on-startup: true       # false 则只加载插件、不自动刷 bot
```

> 提示：大量 bot 会计入大区在线人数。若出现「大区人数已满」，请提高 `game_config` 中的 `channel_capacity`，或临时设 `spawn-bots-on-startup: false` 后重启。

## 构建与加载 SoloMapling

```bash
# 在 BeiDou-Server 仓库根目录
mvn -pl extension-api,gms-server,solomapling-plugin -am package -DskipTests
cp solomapling-plugin/target/solomapling-plugin-*-SNAPSHOT.jar gms-server/plugins/

# 工作目录必须是 gms-server（wz / scripts / SoloMapling 相对路径资源）
cd gms-server
java -Xmx4g \
  -Dspring.config.location=src/main/resources/application.yml \
  -jar target/BeiDou-boot.jar
```

说明：

- 可运行产物是带 classifier 的 **`BeiDou-boot.jar`**；主产物 `BeiDou.jar` 为瘦 jar，供插件模块编译依赖。
- 克隆假人模板角色名为 **`fmbot`**（Flyway `V1.9.3`）。
- 游戏内 GM ≥ 4 可用：`!smping`、`!env`、`!bot`、`!move`、`!fmbot`、`!gcmove`。

## 人口配置化（EnvironmentPopulation.yaml）

启动波次数量、FM/商人批次、**TrainingBot 出生枢纽**与**城镇氛围**均由同一 YAML 控制：

| 文件 | 作用 |
|------|------|
| `EnvironmentPopulation.yaml` | 波次开关、人数、`scale`、training cohorts、**town_presence.towns** |
| ~~`TownPresence.yaml`~~ | 已并入上文件；仅保留废弃 stub |

要点：

- `scale: 0.5` 可整体缩小 Henesys/FM/商人/Training 数量（城镇氛围不缩放）。
- `training.cohorts[].map` 是**出生枢纽**；真正练级图仍由 `TrainingMapFinder` 运行时发现。
- 城镇氛围：`waves.town_presence.towns`（已替代独立的 `TownPresence.yaml`）。
- 可选：`application.yml` → `solomapling.population-config: <path>` 覆盖配置路径。
- 游戏内：`!env population show|reload`、`!env townpresence reload`（GM≥4）。

详细说明见 `solomapling-plugin/src/main/java/soloMapling/Environment/CONFIG.md`。

无整服启动时的自动校验：

```bash
mvn -pl solomapling-plugin -am test -Dtest=EnvironmentPopulationConfigTest -Dsurefire.failIfNoSpecifiedTests=false
```

## 与 Cosmic 版 SoloMapling 的关系

| | Cosmic 集成 | BeiDou 集成 |
|--|-------------|-------------|
| 框架代码位置 | 编进 Cosmic 主工程 `soloMapling/` | 独立 jar：`solomapling-plugin` |
| 加载方式 | 启动即内嵌 | `plugins/` + SPI |
| 上游源码 | SoloMapling 仓库 | 同左；同步进本仓库 `solomapling-plugin` 后构建 |

上游框架改动请先落在 SoloMapling 仓库，再同步到本仓库的 `solomapling-plugin` 模块。
