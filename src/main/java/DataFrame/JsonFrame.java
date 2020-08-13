package DataFrame;

import Constract.LogData;
import org.apache.flume.Context;
import org.apache.flume.sink.AbstractSink;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonFrame extends LogData
{
    // 日志
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSink.class);

    // 必须项
    private String appId, metricName, hostName ;

    // 组合项
    private String[] metrics, tags, condition;

    // 构造函数
    public JsonFrame(Context context) {
        // 配置文件
        this.appId = context.getString("appid","tbds-mk");
        this.metricName = context.getString("metricname","metricname");
        this.hostName = context.getString("hostname","hostname");

        // 组合项
        this.metrics= context.getString("items").split(",");
        this.tags = context.getString("tags").split(",");
        this.condition = context.getString("condition","").split(",");
    }

    // 获取AppID。如果不存在json中，则返回configure的配置值
    public String getAppId() {
        return  this.getDataFromJson(this.appId);
    }

    // 获取metric name
    public String getMetricName() {
        return  this.getDataFromJson(this.metricName);
    }

    // 替换TBDS的集群Host
    public String getHostname() {
        String host = this.getDataFromJson(this.hostName);
        LOG.info("host");
        if (host.startsWith("tbds-")) {
            return host;
        }
        return "tbds-"+host.replaceAll("\\.","-");
    }

    // 获取metric
    public JSONObject getMetrics()
    {
        JSONObject metricData = new JSONObject();
        for (String item: this.metrics
             ) {

            String mKey = item.split("=")[0];
            String vKey = item.split("=")[1];
            metricData.put(this.getDataFromJson(mKey), this.getDataFromJson(vKey));
        }
        return metricData;
    }

    // 获取tags
    public JSONObject getTags() {
        JSONObject tagsData = new JSONObject();
        for (String item: this.tags
             ) {

            String mKey = item.split("=")[0];
            String vKey = item.split("=")[1];

            tagsData.put(mKey, this.getDataFromJson(vKey));
        }
        return tagsData;
    }

    // 条件过滤。
    public boolean judge()
    {
        // 默认不过滤
        boolean result = true ;

        for (String item: this.condition) {

            // 如果没有相关过滤条件，直接略过
            if (item.trim().equals("")){
                continue;
            }

            result = false;
            if (item.indexOf("=") > 0)  // 判断是否相等
            {
                String[] kv = item.split("=");

                if (this.getDataFromJson(kv[0]).equals(kv[1]))
                {
                    result=true;
                    break;
                }
            } else {
                // 判断是否存在。如果存在，则key和value不一样。 此处有个小bug
                if ( ! this.getDataFromJson(item).equals(item)) {
                    result=true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 从JSON数据中取出数据
     * @param fields 多级的keys。如: a.b.c => {"a":["b":{"c":value}]}
     * @return String 返回String值。也包括单string或者json的string.如若没有，则返回key
     */
    private String getDataFromJson(String fields)
    {
        JSONObject  jsonObject = new JSONObject( new String(event.getBody()));
        String[]    field = fields.split("\\.");
        Object      data = fields;

        // 倒序取值
        for (int i = field.length; i > 0 ; i--)
        {
            String key = field[field.length-i];

            if ( !jsonObject.has(key)) {
                return data.toString();
            }
            if (i > 1) {
                jsonObject = jsonObject.getJSONObject(key);
            }else {
                data = jsonObject.get(key);
            }

        }
        return data.toString();
    }
}


