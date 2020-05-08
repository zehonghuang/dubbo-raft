# dubbo-raft

### 基于Dubbo框架的raft算法库

- ✅ ~~目前在开发rpc层~~
- ✅ ~~完成Dubbo-Raft客户端~~
- ✅ ~~基本完成rpc层，开始写选举~~
- 完成选举闭环，且冒烟通过，还需更多的测试
- 日志复制
    - ✅ ~~完成Leader日志落地~~
    - ✅ ~~基本完成状态机功能~~
    - ✅ ~~完成Pipeline优化~~
- 增加计时器示例
- 增加disruptor处理client发送的任务
    - ⁉️调研百度的bthread，看是否能用java实现，替代disruptor，原因是后者无法做event之间通信调度
- 增加RocksDB做为日志存储
- 🚩计划实现SegmentLog，分段存储