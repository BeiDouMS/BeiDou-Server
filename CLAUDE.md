# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

BeiDou-Server 是一个冒险岛（MapleStory v83，GMS 协议）私服服务端，基于 [Cosmic](https://github.com/P0nk/Cosmic)（再往上追溯是 HeavenMS/OdinMS）做汉化与优化。协议版本 `ServerConstants.VERSION = 83`，当前版本号 `BEI_DOU_VERSION = "1.11"`。

仓库分两部分：
- `gms-server/`：Java 21 + Spring Boot 3 + Netty 的服务端，是主体。
- `gms-ui/`：Vue 3 管理后台（Arco Design Pro 模板），构建后可嵌入服务端 jar 同源发布。

**授权**：`net/`、`client/`、`server/`、`scripting/` 等目录是 OdinMS/Cosmic 遗留代码，文件头带 AGPL-3.0 版权声明，整个项目按 AGPL-3.0 发布（见 `LICENSE`）。BeiDou 自己加的是 `controller`/`service`/`dao`/`config`/`aop`/`manager` 这层 Spring 胶水。改动遗留代码时保留版权头。

## 开发环境与命令

### 服务端 gms-server
- JDK 21、Maven、MySQL 8+（不支持 MySQL 8 以下）。
- 默认连 `localhost:3306/beidou`，账号 `root`/`root`（见 `application.yml`）。库不存在时启动会自动创建并跑 Flyway。
- 构建：在仓库根或 `gms-server/` 执行 `mvn clean package`，产物 `gms-server/target/BeiDou.jar`（spring-boot repackage，主类 `org.gms.ServerApplication`）。
- 运行：`java -jar BeiDou.jar`，或 `gms-server/launch.bat`/`launch.sh`（脚本期望同目录下有捆绑 JRE `jdk-21.0.11+10-jre`）。
- 开发调试：IDE 直接运行 `org.gms.ServerApplication.main`，**Working directory 必须设为 `gms-server`**（否则相对路径找 wz/scripts/logs 会错）。
- 测试：`mvn test`。单个测试：`mvn -pl gms-server test -Dtest=CodeGen#genMapperAndEntity`。
  - 注意：`src/test/java/` 下的 `CodeGen`、`ExportPatch`、`XmlDiff`、`XmlNode`、`XmlSort` 是**开发工具**而非 CI 单测——`CodeGen` 直连本地 `beidou` 库生成实体/Mapper，`ExportPatch`/`Xml*` 是 wz 补丁工具的 Java 版。无 MySQL 运行时 `mvn test` 会失败。

### 前端 gms-ui
- Node 20.15.0 LTS + Yarn。
- `cd gms-ui && yarn install`
- 开发：`yarn dev`（Vite，端口 8787，自动打开浏览器）。
- 构建：`yarn build`（先 `vue-tsc --noEmit` 类型检查，再 `vite build --config ./config/vite.config.prod.ts`）。
- 类型检查：`yarn type:check`。
- Lint：通过 `lint-staged` 在提交时触发 eslint（airbnb-base）+ stylelint + prettier；commitlint 强制 conventional 提交信息。

### 数据库准备
非 root 用户需额外权限：`performance_schema.user_variables_by_thread` 的 select、`mysql` 库的 show view（见 `gms-server/README.md`）。

## 架构总览（读多个文件才能理清的部分）

### 双引擎同进程：Spring Boot + Netty 游戏服
一个 JVM 里跑两个引擎，靠 `ServerManager` 桥接：

1. `ServerApplication.main` 先手动解析 yml 自动建库（绕开 Flyway/JPA 自动配置在库不存在时拿连接报错的问题），再 `SpringApplication.run`。
2. `org.gms.manager.ServerManager`（Spring `ApplicationRunner`）在 Spring 就绪后调用 `Server.getInstance().init()` 拉起 Netty 游戏服（`LoginServer` + `ChannelServer`）；`destroy()` 时 `shutdownInternal` 关服。
3. `org.gms.net.server.Server` 是 OdinMS 式单例，**不是 Spring bean**，遗留代码通过 `ServerManager.getApplicationContext().getBean(...)` 反向获取 Spring 依赖。
4. REST 接口（见 `ServerController`）可在线控制游戏服生命周期：`/server/v1/startServer`、`/stopServer`、`/restartServer`、`/online`。

因此改动游戏服启动/关闭流程时，要同时看 `ServerManager`（Spring 侧）和 `Server`（遗留侧）。

### 动态游戏配置：GameConfig
`org.gms.config.GameConfig` 是单例（非 Spring bean），启动时从 `game_config` 表加载成 JSON 树，结构为 `type → subType → code → {value, clazz}`（如 `world.0.exp_rate`）。提供 `getServerXxx(key)` / `getWorldXxx(worldId, key)` 类型化取值。`GameConfig.update/remove/add` 支持热重载，部分参数（exp/meso/drop 等世界倍率）会即时写回 `World` 对象。新增可热重载的运营参数走这条路，而不是加 `application.yml`。

### 数据库与持久层
- MySQL 8+，库名 `beidou`（应用库）。Flyway 迁移脚本在 `src/main/resources/db/migration/V1.0.x__*.sql`，`validate-on-migrate: false`。新增表结构走新版本号迁移脚本。
- 持久层用 **MyBatis-Flex**（不是 MyBatis-Plus）：实体在 `org.gms.dao.entity`，后缀 `DO`，`@Table` + Lombok `@Data/@Builder`；Mapper 在 `org.gms.dao.mapper`，启动 `@MapperScan("org.gms.dao.mapper")`。连接池 Druid。
- 生成实体/Mapper：跑 `CodeGen#genMapperAndEntity`（test 作用域，mybatis-flex-codegen），改 `globalConfig.setGenerateTable(...)` 指定表名，实体后缀固定 `DO`。

### REST API 规范
- 端口 8686。Swagger：`http://localhost:8686/swagger-ui/index.html`——**默认关闭**（`application.yml` 里 `springdoc.api-docs.enabled` 和 `swagger-ui.enabled` 都是 false），开发时需手动开。
- 版本控制：路径模式 `/{controller}/{ApiConstant.LATEST}/{action}`，`@Tag(name = "/server/" + ApiConstant.LATEST)` 与路径一致。新增版本时给 `ApiConstant` 加常量并把 `LATEST` 指过去；不兼容的旧接口要把 Tag 和 RequestMapping 都钉到具体版本（如 `ApiConstant.V1`）。
- 响应统一 `ResultBody<T>`（`code/message/responseId/data`），成功码 `20000`（`BizExceptionEnum.SUCCESS`）。POST 请求体统一 `SubmitBody<T>`（`requestId/data`）信封。
- 鉴权：JWT（`Authorization: Bearer <token>`），`/auth/**` 放行。`AuthTokenFilter` 解析 token；Swagger 开启时可用 `swagger` 作 token 越权（仅测试，**生产必须关 Swagger**）。`ServerFilter` 做限流 + 封禁 IP 校验，并把请求体缓存为可重复读的 `CachedHttpServletRequest`。

### 游戏服内部要点
- Netty 传输：`LoginServer`/`ChannelServer` + 各自 Initializer；opcode 在 `constants/net`，包加密在 `net/encryption`。
- WZ/脚本加载（双语覆盖机制）：服务端默认加载 `gms-server/wz/`（英文基础）与 `gms-server/scripts/`（英文）；再按 `gms.service.language`（`zh-CN`/`en-US`）用 `wz-<lang>/`（如 `wz-zh-CN/`）、`scripts-<lang>/` 覆盖英文基础——即 `wz/`+`wz-zh-CN/` 合并出中文数据，`scripts/`+`scripts-zh-CN/` 合并出中文脚本。读取入口 `provider/wz`。
- 脚本引擎：**GraalVM JS**（非 Nashorn），NPC/quest/portal/reactor/event/item/map 脚本为 `.js`。`Server` 静态块里 `polyglot.engine.WarnInterpreterOnly=false` 抑制告警。

### i18n（服务端）
资源在 `src/main/resources/i18n/{exception,log,message}_{en_US,zh_CN}.properties`，通过 `I18nUtil.getExceptionMessage/getLogMessage/getMessage` 取。Jackson 时区 `Asia/Shanghai`。

## gms-ui 架构要点

- 技术栈：Arco Design Pro Vue 模板 = Vue 3 + Vite 3 + TypeScript + Pinia + vue-router 4 + vue-i18n 9 + axios + Arco Design Vue。Monaco Editor 用于代码/配置编辑。
- 入口 `src/main.ts`，路由 `src/router/`（`appRoutes` + `guard` 路由守卫），状态 `src/store/modules/`（Pinia），i18n `src/locale/{zh-CN,en-US}`（默认 zh-CN，回退 en-US）。
- **API 层约定**（`src/api/interceptor.ts`）：
  - 请求拦截器自动把 `config.data` 包成 `{requestId: uuid, data: ...}`——与后端 `SubmitBody<T>` 对齐；并自动加 `Authorization: Bearer <token>`（token 来自 `getToken()`）。上传（multipart）不包信封。
  - 响应拦截器：成功码 `20000`（与后端 `BizExceptionEnum.SUCCESS` 一致），否则 `Message.error` 并 reject；`responseType === 'blob'` 走文件下载（从 `Content-Disposition` 取文件名）；401 触发 `userStore.logoutCallBack()` 并跳首页。
  - 新增后端接口对应的前端调用放 `src/api/<模块>.ts`，路径形如 `/auth/v1/login`、`/account/v1/info`（带版本段）。
- **部署模型**：dev 下 `.env.development` 设 `VITE_API_BASE_URL='http://localhost:8686'`，前端 8787 直连后端，靠后端 `CorsConfig` 放行（`app.vue: http://localhost:8787`）。prod 下 `.env.production` 为空，`yarn build` 产出的 `dist/` 拷进 `gms-server/src/main/resources/static/`，由服务端 8686 同源托管（`ServerManager` 启动时检测 `static/index.html` 决定是否提示前端地址）。
- 图片资源联网取自 maplestory.io（见 README）。

## 编码规范（必须遵守）

1. **import 规范**：生成代码时统一用 `import` 引入类型，**禁止**用「包名+类名」内联写法（如 `java.util.List<...>` 直接写在签名里）。**仅当存在两个同名类目冲突时**才允许用全限定名消歧，否则一律 import。
2. **i18n 强制**：凡涉及日志打印、前端界面文本、异常信息等**面向人输出展示**的内容，必须走 i18n 资源文件（服务端 `I18nUtil`，前端 `vue-i18n` 的 `t()`），禁止偷懒硬编码字面量。后端日志用 `I18nUtil.getLogMessage(...)`，异常用 `BizExceptionEnum` + `I18nUtil.getExceptionMessage(...)`。
3. Java 21、UTF-8。Lombok 全量使用（`@Data`/`@Builder`/`@AllArgsConstructor`/`@NoArgsConstructor`/`@Slf4j`）。
4. JSON 用 fastjson2（`com.alibaba.fastjson2`）；HTTP 序列化用 Jackson。两者并存，按所在层选用。
5. 日志框架 log4j2（spring-boot-starter-logging 已排除）。日志里不要拼字符串字面量做展示文案，见第 2 条。
6. 遗留 OdinMS/Cosmic 代码改动时，匹配周边代码风格与注释密度，保留 AGPL 版权头。
7. 提交信息：后端历史多为中文简述（如「修复雇佣商店重复实例与关闭竞态」「格式化代码」）或「合并拉取请求 #N」；前端 commitlint 强制 conventional（`feat:`/`fix:` 等）。

## 非显而易见的实现细节

- **`saveCharToDB` 有两个版本**：角色保存实际生效的是 `UPDATE` 版本，而**不是** `CharacterService.insertSelective` 那版。排查角色存档问题时要认准实际写入路径。
- **账号/角色级联删除有坑**：`deleteCharacterEntry` 存在 NPE 风险，扩展值表（`ExtendValue`）会被复用，且鉴权与删除逻辑分离。改动账号/角色删除流程前先把这条链路读全。

## wz 补丁工作流

服务端 wz XML 的改动需要同步到客户端 `.img` 文件。仓库提供原生二进制工具 `.claude/skills/wz-patch-java/xml-img-patcher.exe`（GraalVM native-image 编译，无需 Java 运行时），完整用法见 `.claude/skills/wz-patch-java/SKILL.md`。典型一站式流程：`export --from=<commit或时间>` 从本仓库导出 xml+diff → `batch --full-xml-dir=...` 打到客户端 → `verify` 校验。

**客户端结构**（独立仓库 BeiDou-Client）：`Data/` 是中文基础数据，`EN/` 是英文 img 覆盖层--EN 的 img 覆盖进 Data 后客户端 UI/文本变英文。EN 只含文本类 img（String/Quest/UI），不含 Mob/Npc/Skill 等语言无关数据（中英文客户端共用 `Data/`）。

**映射**：服务端 `wz/`（英文基础）↔ 客户端 `EN/`；`wz-zh-CN/`（中文覆盖）↔ 客户端 `Data/`。diff 路径 `Mob.wz/8787126.img.xml.diff` 自动剥 `.wz` 段 → img 路径 `Mob/8787126.img`。

**坑（patch 语义）**：`patch` 的 ADD 是**合并/追加**到目标 .img，不是覆盖。若客户端 .img 已存在同名子树（典型：服务端新增的中文文件对应客户端已有的 mob img），ADD 会产生重复节点（脏数据）。只有 MODIFY 语义的 diff（服务端修改已有文件）能安全打到已存在 .img。打补丁前先用 `--dry-run` 看变更类型，ADD 整个子树的要确认客户端确实缺该子树。

## 常用命令速查

```bash
# 服务端
mvn clean package                          # 构建 gms-server/target/BeiDou.jar
mvn -pl gms-server test -Dtest=CodeGen#genMapperAndEntity   # 跑单个测试/代码生成
java -jar gms-server/target/BeiDou.jar     # 运行（需本地 MySQL 8 已启动）

# 前端
cd gms-ui && yarn install && yarn dev      # 开发，端口 8787
cd gms-ui && yarn build                    # 生产构建（产物 dist/，可拷入 server static/）
cd gms-ui && yarn type:check               # 类型检查

# wz 补丁
.claude/skills/wz-patch-java/xml-img-patcher.exe export --from=<hash或datetime> --repo=.
```
