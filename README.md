# JavaGameService
一个仍在构思和开发中的基于Java的游戏服务器框架实现，使用了Netty、ProtoBuf、Disruptor等。

Hello world!

数据库使用Mysql，缓存服务器使用Redis。数据库持久层使用Mybatis。

模块暂时划分为网关、交易、场景、战斗。为了保持较好的并发性能，各模块可能使用单线程进行消息的处理。

网关负责登录与注册，包括Token的分发和校验。

后期将把处理消息的函数抽离成通用的接口，使用反射机制配合XML文件对MsgCode进行映射处理。

游戏场景被划分为多个节点进行管理，每个节点负责固定的一部分区域，暂不支持节点管辖范围的动态扩大或缩小。

														———— For my graduation project