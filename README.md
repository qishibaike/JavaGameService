# JavaGameService
一个仍在开发中的基于Java的开源游戏服务器框架实现，使用了Netty、ProtoBuf、Disruptor等

Hello world!

数据库使用Mysql，缓存服务器使用Redis。

模块暂时划分为网关、交易、场景、战斗。

网关负责登录与注册，包括Token的分发和校验。

后期将把处理消息的函数抽离成通用的接口，使用反射机制配合XML文件对MsgCode进行映射处理。

														———— For my graduation project