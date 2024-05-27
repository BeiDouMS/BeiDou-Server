本项目基于Cosmic来的汉化和优化，Cosmic地址：https://github.com/P0nk/Cosmic

# gms-server 服务端
- 已实现自动创建数据库，执行初始化sql脚本，只要保证mysql是启动的即可  
- 已建立与gms-api的交互，开放端口8585

# gms-api 给web提供api
- 已实现与gms-server的交互，api服务的端口8686
- 已引入swagger，swagger地址：http://localhost:8686/swagger-ui/index.html
- 开发接口需要固定增加版本如：/v1 /v2 /v3，这样新版接口可以不用考虑兼容旧版本接口，只要旧版本接口不删，就可以一直调用旧版本

# gms-ui web端


