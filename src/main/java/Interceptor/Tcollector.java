package Interceptor;


import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import DataFrame.*;
import org.apache.flume.sink.AbstractSink;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.List;


public class Tcollector implements Interceptor {

    // 日志打印
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSink.class);

    // @var: BaseFrame对象
    private BaseFrame frame;

    // 构造器，传入一个BaseFrame对象
    public Tcollector(BaseFrame frame) {
        this.frame = frame;
    }

    // 初始化数据
    public void initialize() {}

    // 单项拦截
    public Event intercept(Event event)
    {
        this.frame.init(event);

        JSONObject eventBody = new JSONObject();
        if (
                frame.getHostName().equals("") || frame.getAppId().equals("") || frame.getMetricName().equals("")
        ){
            event.setBody(new byte[]{});
        }else {
            eventBody.put("appid", frame.getAppId());
            eventBody.put("hostname", frame.getHostName());
            eventBody.put("metricname", frame.getMetricName());
            eventBody.put("tags", frame.getTags());
            eventBody.put("metrics", frame.getMetric());
            // 写入event.body
            try {
                byte[] bytes = eventBody.toString().getBytes("utf-8");
                event.setBody(bytes);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            LOG.info(eventBody.toString());
        }
        return event;
    }

    // 多项拦截
    public List<Event> intercept(List<Event> list) {
        for (Event event : list) {
            intercept(event);
        }
        return list;
    }

    public void close() {

    }

    // 实现静态类
    public static class Builder implements Interceptor.Builder
    {
        private String appId;
        private String metricName;

        public Interceptor build() {
            return new Tcollector(new BaseFrame()
            {
                // event的body转json
                JSONObject eventBody = new JSONObject();

                // 多数组形式json
                private JSONObject metrics;

                // 单数组形式json
                private JSONObject tags;


                // 把EVENT BODY转JSON
                public void init(Event event)
                {
                    eventBody = new JSONObject(new String(event.getBody()));
                }

                // 获取type。
                public String getType(){
                    if (eventBody.has("type")) {
                        return eventBody.getString("type");
                    }
                    return "tbds" ;
                }


                // 获取第一个metric标识
                public String getAppId() {
                    return appId;
                }


                // 返回剩下的metric标识
                public String getMetricName() {
                    return metricName;
                }

                // 返回带tbds标识的hostname
                public String getHostName()
                {
                    String hostname ;

                    if ( eventBody.has("ip")) {
                        hostname =  eventBody.getString("ip");
                    }else {
                        hostname = "0.0.0.0";
                    }

                    if (hostname.startsWith("tbds")) {
                        return hostname;
                    }
                    return "tbds-"+hostname.replaceAll("\\.","-");
                }

                // 获取需要保存的数据
                public JSONObject getMetric() {
                    return new JSONObject()
                            .put(eventBody.getString("time"), eventBody.getString("count"));
                }

                // 获取标签
                public JSONObject getTags()
                {
                    return new JSONObject()
                            .put("instance_id", eventBody.get("tablename"))
                            .put("service","hermes");
                }
            });
        }

        // 拦截器相关配置文件
        public void configure(Context context) {
            appId = context.getString("appid","t_demo");
            metricName = context.getString("metricname","metricname");
        }
    }
}
