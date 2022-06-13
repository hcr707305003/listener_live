# listener_live
mirai插件 (监听虎牙、斗鱼直播)

### 项目安装前需要注意

##### 1.需要`mirai-console`扩展

##### 2.授权
 - 虎牙授权 `/permission permit * shiroi.top.plugin:command.huya:*`
 - 斗鱼授权 `/permission permit * shiroi.top.plugin:command.douyu:*`

##### 3.配置设置
 - 1.虎牙配置
```editorconfig
{
    #是否打开
    "isOpen": true,
    #授权的群
    "group": [
        #"*", # *代表所有群
        "群号",
    ],
    #监听时间(每五分钟监听一次)
    "listen": 5
}
```

 - 2.斗鱼配置
```editorconfig
{
    #是否打开
    "isOpen": true,
    #授权的群
    "group": [
        "*", # *代表所有群
    ],
    #监听时间(每五分钟监听一次)
    "listen": 5
}
```