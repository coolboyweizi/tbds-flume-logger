package Interceptor;

import Constract.LogData;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.apache.flume.sink.AbstractSink;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

/**
 * Class For Metrics
 */
public class MetricsInterceptor implements Interceptor
{
    // openTSDB 入库语法:  put metricName timestamp values tags

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSink.class);

    private LogData logData ;

    public MetricsInterceptor(LogData logData)
    {
        this.logData = logData;
    }

    public void initialize() {}

    public void close() {}

    // 单项拦截
    public Event intercept(Event event)
    {
        // 格式化数据。如果不是json，就为空

        try {
            LOG.info("init");
            logData.initlize(event);
            JSONObject metricBodyJson = new JSONObject();

            // 条件判断
            if ( logData.judge() ) {
                metricBodyJson.put("appid", logData.getAppId());
                metricBodyJson.put("metricname", logData.getMetricName());

                metricBodyJson.put("tags", logData.getTags());
                metricBodyJson.put("metrics", logData.getMetrics());
                metricBodyJson.put("hostname", logData.getHostname());

                byte[]  bytes = new JSONObject()
                        .append("metrics", metricBodyJson)
                        .toString()
                        .getBytes("utf-8");

                event.setBody(bytes);

            }else {
                event.setBody(new byte[]{});
            }
        }catch (Exception e){
            event.setBody(new byte[]{});
            mError(e.getMessage()+e.toString());
        }
        return event;
    }

    // 多项拦截
    public List<Event> intercept(List<Event> list)
    {
        for (Event event : list) {
            intercept(event);
        }
        return list;
    }


    // LOG LEVEL
    private void mInfo(String message){
        if (LOG.isInfoEnabled()) {
            LOG.info(message);
        }
    }

    private void mWarn(String message){
        if (LOG.isWarnEnabled()) {
            LOG.warn(message);
        }
    }

    private void mError(String message){
        if (LOG.isErrorEnabled()) {
            LOG.error(message);
        }
    }

    // 内部静态启动
    public static class Builder implements Interceptor.Builder
    {
        private Context context;
        public Interceptor build()
        {
            return new MetricsInterceptor(
                    LogData.getInstance("Json", context)
            );
        }
        public void configure(Context context)
        {
            this.context = context;
        }
    }
}
