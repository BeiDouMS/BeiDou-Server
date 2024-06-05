## 开发准备
### 数据库准备

如果数据库用户不是root用户，该用户需要拥有这些额外权限

performance_schema库user_variables_by_thread表的select权限

mysql库的show view权限

### idea 配置

如果是直接打开的Cosmic目录运行，需要在server的编译配置里设置 Working directory 为 gms-server