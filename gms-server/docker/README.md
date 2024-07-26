# 通过Docker部署BeiDouMS
### 先决条件
* 已安装 Docker && Docker-compose

### 示例
```
// 容器相关内容都在如下目录
cd BeiDou/gms-server/docker/

// 创建镜像&&容器
docker compose create

// 首次运行需要先启动mysql
docker compose start db
// 再启动服务端
docker compose start maplestory

// 后续运行只需要
docker compose start

// 服务器配置文件位置
// BeiDou/gms-server/docker/application.yml

// 脚本位置
// BeiDou/gms-server/scripts
// BeiDou/gms-server/scripts-zh-CN

// wz位置
// BeiDou/gms-server/wz-zh-CN
// BeiDou/gms-server/wz

// 数据库文件位置
// BeiDou/gms-server/docker/docker-db-data/
```
