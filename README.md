# dubbo-raft

### 基于Dubbo框架的raft算法库

这是个人试验性项目(参考百度BRAFT与蚂蚁SOFA-JRAFT)，功能追求完整，但性能无法保证，会持续更新。

- ✅ ~~目前在开发rpc层~~
- ✅ ~~完成Dubbo-Raft客户端~~
- ✅ ~~基本完成rpc层，开始写选举~~
- ❗️ 已支持multi-raft，「分布式KV储存」待开发哈
- 完成选举闭环，且冒烟通过，还需更多的测试
- 日志复制
    - ✅ ~~完成Leader日志落地~~
    - ✅ ~~基本完成状态机功能~~
    - ✅ ~~完成Pipeline优化~~
    - ✅️ ~~完成Read Index~~
    - ✅ 完成日志复制功能，但未完成全部测试(包括以下未开发的功能)
        - ⭕️ 配置变更，待开发
        - ☑️ 日志压缩(快照)开发中，这个模块组件有点多
            - ✅ 完成本地快照save & load
- 增加计数器示例
- 增加disruptor处理client发送的任务
    - ⁉️调研百度的bthread，看是否能用java实现，替代disruptor，原因是后者无法做event之间通信调度
- 增加RocksDB做为日志存储

- 🚩🚩🚩🚩立个flag，看到底会不会去实现哈哈哈🚩🚩🚩🚩
    - 计划实现SegmentLog，分段存储
    - KV存储动态分片
    - 无状态机版Raft
        - 基于AMQP的分布式消息队列
        
- 相关文献 & 其他拓展

    《[CONSENSUS: BRIDGING THEORY AND PRACTICE](https://web.stanford.edu/~ouster/cgi-bin/papers/OngaroPhD.pdf)》
    
    《[In Search of an Understandable Consensus Algorithm](https://raft.github.io/raft.pdf)》
    
    《[PacificA: Replication in Log-Based Distributed Storage Systems](https://www.microsoft.com/en-us/research/publication/pacifica-replication-in-log-based-distributed-storage-systems/)》
    
    