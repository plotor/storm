### 总体架构与代码结构

__Storm 中涉及到的术语__：

1. stream: 被处理的数据
2. spout: 数据源
3. bolt: 封装数据的处理逻辑
4. executor: 工作线程，执行 spout 和 bolt 的业务逻辑
5. worker: 工作进程，一个 JVM 对应一个工作进程，一个工作进程可以包含一个或多个工作线程 executor
6. task: storm 中最小的处理单元，一个 executor 可以包含一个或多个 task，消息的分发都是从一个 task 到另外一个 task 进行的
7. grouping: 消息分发策略，定义 bolt 结点以何种方式接收数据
8. topology: 以消息分组方式连接起来的 spout 和 bolt 节点网络，定义了运算处理的拓扑结构，处理的是不断流动的消息

__Storm 存储在 ZK 上的元数据__：

```text
+ /storm
| ---- + /workerbeats
| ---- | ---- + /<topology-id>
| ---- | ---- | ---- + /node-port
| ---- | ---- | ---- + /node-port
| ---- + /storms
| ---- | ---- + /<topology-id>
| ---- + /assignments
| ---- | ---- + /<topology-id>
| ---- + /supervivors
| ---- | ---- + /<supervivor-id>
| ---- + /errors
| ---- | ---- + /<topology-id>
| ---- | ---- | ---- + /<component-id>
```

- /storm/workerbeats/<topology-id>/node-port

存储由 node 和 port 指定的 worker 的运行状态和一些统计信息，包括 topology-id，当前 worker 上所有 executor 的统计信息（发送的消息数目，接收的消息数目等），当前 workder 的启动时间以及最后一次更新这些信息的时间。

- /storm/storms/<topology-id>

存储 topology 本身的信息，包括名字、启动时间、运行状态、使用的 worker 数目，以及每个组件的并行度设置，其内容在运行期间是不变的。

- /storm/assignments/<topology-id>

存储 nimbus 为每个 topology 分配的任务信息，包括该 topology 在 nimbus 机器本地的存储目录、被分配到的 supervivor 机器到主机名的映射关系、每个 executor 运行在哪个 worker 上及其启动时间等，该节点在运行过程中会被更新。

- /storm/supervivors/<supervivor-id>

存储 supervivor 的运行统计信息，包括最后一次更新的时间、主机名、supervivor-id、已经使用的端口列表、所有的端口列表，以及运行时间等，该节点在运行过程中会被更新。

- /storm/errors/<topology-id>/<topology-id>/e/<sequential-id>

存储运行过程中每个组件上的错误信息，sequential-id 是一个递增序列号，每个组件最多只会保存最近的 10 条错误信息。

### 编程基础

几种基本的 Bolt 接口总结：

- IRichBolt

最常用来定义 topology 组件的接口，用户可以通过其实现各种控制逻辑，并且能够控制何时进行 ack、fail，以及 anchor 操作。

- IBasicBolt

提供了定义简单逻辑的 topology 组件接口，对于此类 bolt，storm 内置实现了 ack、fail，以及 anchor 机制。用户基于此类实现自己的 bolt 也比较简单，但是使用上存在限制，基于收到的某条消息衍生出来的所有消息必须在一次 execute 中发出去（或者需要对消息进行缓存并编号），否则内置的 ack 机制不能保证 bolt 的正常工作。所以用户应该避免使用该类型的 bolt 来做诸如聚集或者连接的操作。

- IBatchBolt

用来处理批量数据的接口，目前主要用于事务 topology。

