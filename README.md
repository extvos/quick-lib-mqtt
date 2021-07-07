# quick-lib-mqtt

模块根据配置自动创建MQTT Client，通过注解的方式支持消息订阅和消息体以及Topic的分段获取。 支持配置一个默认的Client以及多个自定义名称Client，订阅和发送消息可以指定Client。

## 配置方式

```yaml
spring:
	mqtt:
		disable: false  # 是否禁用
		### <-- 一下为默认客户端配置
		client-id: xxxx  # 客户端ID
		brokers: tcp://localhost:1883   # MQTT服务器地址, 必填, 可以配置多个.
		username: xxxx  # 用户名.
		password: xzxz   # 密码.
		enable-shared-subscription: false # 是否启用共享订阅,对于不同的Broker,共享订阅可能无效(EMQ已测可用).
		default-publish-qos: 0  # 发布消息默认使用的QOS, 默认 0.
		max-reconnect-delay: 5 # 最大重连时间，默认5秒
		keep-alive-interval: 5 # KeepAlive 周期(秒).
		connection-timeout: 5 # 连接超时时间(秒).
		executor-service-timeout: 5 # 发送超时时间(秒).
		clean-session: false # 是否清除会话.
		automatic-reconnect: false # 断开是否重新连接.
		will: # 遗愿相关配置.
		    topic: xxxx # 遗愿主题
		    payload: xxxx  # 遗愿消息内容
		    qos: 0  # 遗愿消息QOS
		    retained: false  # 遗愿消息是否保留
		#### -->
		#### <-- 多个Client配置：
		clients:
			{CLIENT_ID_1}:  # 此次为ClientId，其余跟上面的默认Client配置相同
				...
			{CLIENT_ID_2}:  # 此次为ClientId，其余跟上面的默认Client配置相同
				...

```

## 消息订阅 `@TopicSubscripbe`

通过注解`@TopicSubscripbe`方法添加消息订阅，通过`@TopicVariable`和`@Payload`来指定入参。

如：

```java
@Component
public class TestTask {
	@TopicSubscribe("test/#")
    public void mqttTestTopics(String topic, @Payload Map<String,Object> data){
        log.debug("mqttTestTopics:> {}, {}", topic, data);
    }

    @TopicSubscribe("$SYS/brokers/{node}/clients/{device}/connected")
    public void onConnect(String topic, @TopicVariable("node") String node, @TopicVariable("device") String device, @Payload Map<String,Object> data){
        log.info("onConnect1:> {} {} ", node, device);
        log.info("onConnect2:> {} {} ", topic, data);
    }

    @TopicSubscribe("$SYS/brokers/{node}/clients/{device}/disconnected")
    public void onDisconnect(String topic, @TopicVariable("node") String node, @TopicVariable("device") String device, @Payload Map<String,Object> data){
        log.info("onDisconnect1:> {} {} ", node, device);
        log.info("onDisconnect2:> {} {} ", topic, data);
    }
}

```

## 消息发送

通过实例化`MqttPublisher`来发送消息。 如：
```java

	MqttPublisher mqttPublisher = new MqttPublisher;
	mqttPublisher.send("Test/a/b/c",byte[]{0,1,2,3,4,5,6,7,8,9});
```