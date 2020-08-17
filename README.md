### TBDS-Monitor简单的实现逻辑

TBDS的监控指标方案

1. application通过flume实现的http:7777端口请求数据，
2. flume数据处理/验证后,生产数据到kafka 
3. tcollector通过kafka消费数据，并整理数据
4. tcollector通过put请求存储到openTSDB中

```flow
st=>start: Application(HTTP请求7777端口)
io=>inputoutput: Flume(HTTP接受处理JSON)
op=>operation: KAFKA(生产/消费)
cond=>condition: 数据是否JSON格式
sub=>subroutine: drop
e=>end: TColltor

st->io->cond
cond(yes)->op->e
cond(no)->sub
```

### TBDS中的TCollector与openTSDB数据格式关系

1、openTSDB添加数据语法
```shell
put <metric> <timestamp> <value> <tagk_1>=<tagv_1>[ <tagk_n>=<tagv_n>]
```

2、tcollector从kafka消费得来的metric data
```json
{
    "metrics": [
        {
            "appid": "mking",
            "hostname": "tbds-172-16-0-4",
            "metricname": "network",
            "metrics": {
                "1597634942159": "406885354231"
            },
            "tags": {
                "instance_id": "lo",
                "type": "bytes_sent"
            }
        }
    ]
}
```

3、openTSDB与tcollector字段整合

- metric: 由AppID+metricname构成的fullMetricsName
    + appid: metric的前缀
    + metricname: metric的子项
- timestamp: 取metrics.metrics.key
    + 会对metrics下的kv进行提取，以key为openTSDB的timestamp
- value: 即metrics数据。取metrics.metrics.value
    + 会对metrics下的kv进行提取，以value为openTSDB的values
- tags: 主要由如下项目组成
    + hostname: 需要匹配集群的hostname命名规则     
    + type: 如果存在，自动填充到tags
    + instanceid: 如果存在，自动填充到tags
    + tags: 如果存在自定义tags数据，会自动填充到tags


### 自定义功能实现
> 通过重写flume拦截器实现数据格式转换，并生产到kafka。由tcollector消费并完成后续工作

**相关代码功能**

- Interceptor.MetricsInterceptor
    + flume拦截器
    + 调用Builder内部静态类启动
    + 调用时需要实例化相关的LogData对象。并传参配置项
    
- Constract.LogData
    + 一个简单的抽象类
    + 用于拦截器调用方法
    + 用于实例相关的子对象

- DataFrame.JsonFrame
    + 一个简单的LogData子类。负责处理和提取flume.source数据
    + 提取并处理json内部数据
    + 实现多级键值提取。


**JsonFrame 拦截器配置示例**

JsonFrame主要是通过key找对应的值value。通过对应的value组装新的kv


```properity
# demo: {"key1":{"key2":123},"ip":"127.0.0.1"}
# iterceptors
agent.sources.s1.interceptors = hermes regfilt   # 拦截器名

# 采集AppID的值。如若没有，则反会key
agent.sources.s1.interceptors.hermes.appid = appIds

# 采集metricname的值，如若没有，则返回key
agent.sources.s1.interceptors.hermes.metricname = network # metricName

# 采集hostname的值。如没有，会报错
agent.sources.s1.interceptors.hermes.hostname = ip  

# 采集metric的kv值。timestamp和value是两个key，
agent.sources.s1.interceptors.hermes.items = timestamp=value

# 采集tags的值。
agent.sources.s1.interceptors.hermes.tags = instance_id=instance_id,type=type

# 配置启动项
agent.sources.s1.interceptors.hermes.type = Interceptor.MetricsInterceptor$Builder
```


