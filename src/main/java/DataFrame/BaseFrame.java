package DataFrame;


import org.apache.flume.Event;
import org.json.JSONObject;

abstract public class BaseFrame
{

    abstract public void init(Event event);

    abstract public String getAppId();

    abstract public String getHostName();

    abstract public String getMetricName();

    abstract public JSONObject getTags();

    abstract public JSONObject getMetric() ;

    abstract public String getType();
}
