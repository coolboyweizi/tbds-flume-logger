// 测试用。
import org.json.JSONObject;

import java.util.List;

public class demo {

    public static void main(String[] args) {
        String s= "{\"count\":18,\"node\":\"worker\",\"time\":\"26602082\",\"type\":\"parserCount\",\"tablename\":\"UBL_EVT\",\"ip\":\"175.168.1.12\"}";
        JSONObject eventBodyJson = new JSONObject(s);

         /*
        JSONObject eventNewBody  = new JSONObject();
        JSONObject metricJson = new JSONObject();
        String hostname= "tbds-"+eventBodyJson.getString("ip").replaceAll("\\.","-");


        eventNewBody.put("hostname", hostname);
        eventNewBody.put("instance_id", eventBodyJson.get("tablename"));
        eventNewBody.put("appid", "mkings");
        eventNewBody.put("metricname","hermes.parserCount");
        eventNewBody.put("timestamp",System.currentTimeMillis());
        eventNewBody.put("metrics", new JSONObject().put(System.currentTimeMillis()+"", eventBodyJson.get("count")));

        metricJson.append("metrics", eventNewBody);
        //System.out.printf(metricJson.toString());*/
        String d = "{\"ip\": \"tbds-172-16-0-4\", \"type\": \"used\", \"value\": 11270078464, \"timestamp\": 1596793261005, \"instance_id\": \"/\"}";

        JSONObject j = new JSONObject(d);

        String[] c = "".split(",");

        System.out.println(conditionor(j, c) + "");
    }

    private static String getDataFromJson(JSONObject jsonObject, String fields)
    {
        String[] field = fields.split("\\.");
        Object data = null;
        for (int i = field.length; i > 0 ; i--) {
            String key = field[field.length-i];
            if (i > 1) {
                jsonObject = jsonObject.getJSONObject(key);
            }else {
                data = jsonObject.get(key);
            }
        }
        return data.toString();
    }

    private static Boolean conditionor(JSONObject eventBody, String[] field)
    {
        boolean condition = true;

        for (String item: field
        ) {
            if (item.trim().equals("")){
                continue;
            }
            System.out.println(item);
            condition = false;
            if (item.indexOf("=") > 0)  // 判断是否相等
            {

                String[] kv = item.split("=");

                if ( eventBody.getString(kv[0]).equals(kv[1])){
                    condition=true;
                    break;
                }
            } else {
               // 判断是否存在
                if (eventBody.has(item)) {
                    condition=true;
                    break;
                }
            }
        }
        return condition;
    }
}
