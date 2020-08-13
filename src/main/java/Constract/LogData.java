package Constract;

import DataFrame.JsonFrame;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.json.JSONObject;

abstract public class LogData {

    protected Event event = null;

    // 获取相关实例名称
    final public static LogData getInstance(String name, Context context){
        if (name.equals("Json")) {
            return new JsonFrame(context);
        }
        return null;
    }

    public void initlize(Event event){
        this.event = event;
    }

    // 获取AppID
    abstract public String getAppId();

    // 获取metric name
    abstract public String getMetricName();

    // 获取hostname
    abstract public String getHostname();

    // metrics
    abstract public JSONObject getMetrics();

    // tags
    abstract public JSONObject getTags();

    // 条件判断
    abstract public boolean judge();

}
