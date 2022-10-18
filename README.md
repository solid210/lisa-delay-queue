# lisa-delay-queue
### 项目介绍

基于redis-stream实现的延迟消息队列。

本来想取名redis stream delay queue的，不过太不个性了，因此给这个项目取名叫Lisa。

别问我Lisa是谁，Lisa是万里挑一的那个你我她。



该项目包含三个服务：`manager`、`producer`、`consumer`，各自分工明确。

`manager`负责消息的调度

`producer`是消息的生产者。除了生产消息，什么都不做。

`consumer`是消息的消费者。除了消费消息（也包括ack），什么都不做。



该项目分别封装了三个服务的boot-starter模块，所以只要引入maven依赖并且启动类注解加上`@SpringBootApplication`就可以正常使用了。



项目依赖Redis，使用到Redis的数据结构有：zset、stream、hash、string。

##### 所有针对redis复杂的数据操作（例如一次操作中包含数据转移，删除等操作）都是基于redis脚本的，保证原子化操作。



具体如下**（后续会补上架构图）**：

#### waiting queue（等待队列）

数据结构zset。field是msgId，score是延迟消息触发时间。

`manager`会定时扫描waiting queue中的field，当到达延迟消息发送时间后，会将该记录从waiting queue中移除并加入到ready queue中。

#### ready queue（就绪队列）

数据结构stream。不做任何操作，完全交给consumer去消费，consumer关闭了自动ack机制，需要手动ack（没有异常就ack了）

#### retry queue（重试队列）

数据结构zset。

`manager`会定期扫描一段时间ready queue中未ack的数据，然后从stream中移除，如果msgId的重试次数未不为0，则放入retry queue，如果为0，说明已经消耗完了所有的重试次数，msgId会被打入冷宫（移动到garbage中），用于复查问题。

`manager`也会定期扫描到达重试时间的数据，将此类数据从重试队列中移除，放入ready queue中。

#### retry count（重试次数）

数据结构hash。用于存储msgId剩余的重试次数

#### garbage key（垃圾key）

数据结构set。用于存储重试失败并被废弃的msgId。这里的数据不会再被使用，需要手动check。

#### K-V

存储关系：MessageId--MessageBody。

前几种数据格式仅仅存储MessageId（即msgId），而真正的消息对象是以KV形式存储的。

当ready queue中的消息被正常消费并收到ack消息之后，msgId对于的对象才会被删除。



### 关于Manager（消息管理者）

该服务内置三个定时任务，分别如下：

1. 将消息从waiting queue或者retry queue移动到waiting queue；
2. 将pending的消息移动到retry queue；
3. 清理stream中已经消费完（已经ack）的数据。已经消费完的数据，redis是不会自动帮忙清理的，所以需要手动清理。



### 如何使用

为了方便上手，三个项目各有一个`demo`用于演示如何使用。感兴趣的同学可以跑一下demo

当服务端项目作为producer使用时，引入依赖如下：

```xml
<dependency>
    <groupId>org.lisa.stream</groupId>
    <artifactId>lisa-delay-queue-producer-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```



当服务端作为consumer使用时，引入依赖如下：

```XML
<dependency>
    <groupId>org.lisa.stream</groupId>
    <artifactId>lisa-delay-queue-consumer-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```



当服务端作为manager使用时，引入依赖如下：

```XML
<dependency>
    <groupId>org.lisa.stream</groupId>
    <artifactId>lisa-delay-queue-manager-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

