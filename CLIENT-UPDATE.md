# 北斗客户端发布

> **版号** 从1开始每次更新都+1，只为了区分不同版本，没有严格定义是更新功能还是修复bug。

由于网盘空间有限，原则上客户端看心情打包一次，请自行依次下载补丁更新。

## 客户端下载
默认为英文客户端，如需汉化 
1. 编辑config.ini将SwitchChinese的值改为true；
2. 将CN包里的文件复制（覆盖）到Data目录下，即可。

- BeiDou V7： https://mega.nz/file/2aRjCDIb#FwzR04lyfE-6bdSXLYwY3YxSRVwJaXCjbOvbe3hysW8
- ~~BeiDou V6： https://mega.nz/file/DLQAxJCD#Au-hzXWSVywpeXetTZKx6_rNihXdbOfyDsRa9Ohmazg
  基本已汉化，尚未汉化部分可提issue或者其他方式告知。~~
- ~~BeiDou V3： https://mega.nz/file/qXw3iRCA#S8e21nDeomANjPfEUjhGtTzCzXsebU_fXmeEHY9ZXDQ
  完成度：能用，暂未汉化；支持中文输入；使用img资源。~~

## 更新记录及补丁下载
使用方法：按顺序下载补丁，直接覆盖到客户端内即可；**wz补丁仅供开发用**。

汉化需重新覆盖 CN 包

### 2024/7/20 V9
更新说明：
1. 新增 突破跳跃上限
2. 新增 免密模式（须服务端支持）
3. 更新 重新实现输入法IME的支持方案，解决老方案win11下不生效的问题（测试）
4. 更新 聊天框文字位置
5. 修复 部分NPC对话内容含中文时，内容被截断的问题
6. 更新 玩家名片卡中职业信息字体
7. 补充 V7 打包打漏的 List.wz 文件
- v9：https://mega.nz/file/THxxEDQR#oFHvCc708PLxwQCFDRCNktp0Yhhg0vaRCFpkr0HYhiw
- v9-wz：https://mega.nz/file/WeBCiL6D#CpGxfBKeC0EGFtYKY5-toeBkR4yuPgomZIcUAJf-m48

### 2024/7/12 V8
更新说明：
1. 新增 命中/回避 上限解除 
2. 修复 魔攻伤害计算没有破攻的问题 
3. 新增 Boss 血条下方增加百分比
- v8：https://mega.nz/file/LTBGGKrL#DjQuTAywtCOFjz0Xn_3dFvxI33a9PsER6-GFXMTuGa4

### 2024/7/12 V7
更新说明：
1. 新增 文字汉化 
2. 新增 任务汉化 (感谢 Tokyo 379008856)
3. 修复 输入法卡门 
4. 新增 魔法攻击面板上限配置
- v7：https://mega.nz/file/ueZnyKjK#GDkljCd_xo3MJMK6T8TsT-H2COx0hVcLNm52BMDdEeA
- v7-wz：https://mega.nz/file/mDYjTBAQ#3rI9oVJV2a7W7u26zjqlPGdntujpfjqiD4mgIUWq4RM

### 2024/6/20
更新说明：
1. 新增汉化：物品的物品锁ToolTip：封印至 XXX
2. 修复4处汉化后日期格式错误，由月日年的顺序修正为年月日的顺序
3. 商城位置回调至居中
4. 交易中心位置居中
- V6: https://mega.nz/file/LfYAXLxI#uIcnww5rStbx9zvaRrIJDqtM7tbKgVSqdjcZCVycfsg
- V6-wz： https://mega.nz/file/qTBU1ZzY#VctdSTdhGCoZ6H_PCQom2qhzrWVQgnahXYcalREShNo

### 2024/6/19
更新说明：
1. 汉化，并增加语言包。如需中文，在config.ini中设置SwitchChinese=true，并且将CN包中的资源复制(覆盖)到Data目录下
2. 商城界面移至左上角不再居中
3. 任务尚未汉化
- V5： https://mega.nz/file/iaoAiK4a#1okrn1V_HW_pkxlKNGQZXtGJu3tixY7b2vTtIHv4u18
- V5-wz： https://mega.nz/file/nH5UzCQT#NHx6y5ZTDDlZllXZBtOHGLdxDkkR8sGHzNBZHC22P1o

### 2024/6/18
更新说明：支持长键盘快捷键
- V4： https://mega.nz/file/7CwDQTqY#wzY33C2MQ8OS3_FJTjqPe9mFKmoZxPfinTMQ0vyf5cs
- ~~V4-wz： https://mega.nz/file/vaYSFLwa#-AmaJLzxDpQXzTt09bRpmbknoytTVwCSSkOjqS5hnaA V5-wz可替代~~

## 开发可选
仅供需要编辑开发的人使用

- ijl15 插件： https://github.com/leevccc/BeiDou-ijl15/tree/BeiDou
- BeiDou V7 wz：https://mega.nz/file/qKZhiIyB#VKGsmVkx1hdi5IEySHdmGgMuUei5t_3DcrmUmhKlDAg
- ``BeiDou V6 wz： https://mega.nz/file/XWQGCTDA#7Ar-5YTO1anZ7yfq2JfQCHA38Wp8AyAYPnSG4xhI8k4~~
- ~~BeiDou V3 wz： https://mega.nz/file/nbpl1JhT#uPw5eGVJM3tz245WUdCBJIRqkJcoDYzBsTBmEBmeMmM~~
