package Interceptor;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.apache.flume.sink.AbstractSink;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class HermesInterceptor implements Interceptor {

    /**
     * 日志记录
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSink.class);

    /**
     * 可以自定义的metric.appid
     */
    private String appid ;

    /**
     * 构造函数
     * @param appid
     */
    private HermesInterceptor(String appid)
    {
        this.appid = appid;
    }


    public void initialize() {}

    /**
     * 单事件拦截
     * @param event
     * @return
     */
    public Event intercept(Event event)
    {

        // 自定义Hermes ParserCounter
        String eventData = parserCounter(event);
        if (eventData.length() == 0) {
            event.setBody(new byte[]{});
        }else {
            try {
                byte[] bytes = eventData.getBytes("utf-8");
                event.setBody(bytes);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return event;
    }

    /**
     * 批量时间拦截
     * @param list
     * @return
     */
    public List<Event> intercept(List<Event> list) {
        for (Event event : list) {
            intercept(event);
        }
        return list;
    }


    /**
     * 整合Hermes ParserCount字段
     * @param event
     * @return
     */
    protected String parserCounter(Event event)
    {
        // {"count":18,"node":"worker","time":"26602082","type":"parserCount","tablename":"UBL_EVT","ip":"175.168.1.12"}
        // {"metrics":{"hostname":"175.168.1.12","instance_id":"UBL_CDR","appid":"triott","metricname":"hermes.parserCount","timestamp":"26602269"}}
        JSONObject eventNewBody  = new JSONObject();

        try {
            JSONObject eventBodyJson = new JSONObject(new String(event.getBody()));

            if (eventBodyJson.getString("type").trim().equals("parserCount"))
            {
                String hostname= "tbds-"+eventBodyJson.getString("ip").replaceAll("\\.","-");
                eventNewBody.put("hostname", hostname);
                eventNewBody.put("instance_id", eventBodyJson.get("tablename"));
                eventNewBody.put("appid", appid);
                eventNewBody.put("metricname","hermes.parserCount");
                eventNewBody.put("timestamp",System.currentTimeMillis());
                eventNewBody.put("metrics", new JSONObject().put(System.currentTimeMillis()+"", eventBodyJson.get("count")));
                LOG.info("success："+eventNewBody.toString());
            }else {
                LOG.debug("error data:"+eventBodyJson.toString());
            }
        } catch (org.json.JSONException e) {
            //LOG.debug(e.getMessage());
            LOG.debug("error exception:"+new String(event.getBody()));
        }finally {
            if (!eventNewBody.has("appid")) {
                LOG.debug("error exception:"+new String(event.getBody()));
            }
        }
        return eventNewBody.has("appid") ? new JSONObject().append("metrics", eventNewBody).toString() :"";
    }

    public void close() {}



    public static class Builder implements Interceptor.Builder
    {
        private String _appid;

        public Interceptor build() {
            return new HermesInterceptor(this._appid);
        }

        // 拦截器相关配置文件
        public void configure(Context context) {
            _appid = context.getString("appid","tbds-ww");
        }
    }


}
