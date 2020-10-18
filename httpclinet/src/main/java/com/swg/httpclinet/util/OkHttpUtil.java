package com.swg.httpclinet.util;

import okhttp3.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by qhong on 2018/7/3 16:55
 **/
@Component
public class OkHttpUtil{
    private static final int THREAD_COUNT = 5;

    private static final int PIPELINE_COUNT = 1000;

    private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);

    private static final Logger logger = LoggerFactory.getLogger(OkHttpUtil.class);

    /**
     * 根据map获取get请求参数
     * @param queries
     * @return
     */
    public static StringBuffer getQueryString(String url,Map<String,String> queries){
        StringBuffer sb = new StringBuffer(url);
        if (queries != null && queries.keySet().size() > 0) {
            boolean firstFlag = true;
            Iterator iterator = queries.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry<String, String>) iterator.next();
                if (firstFlag) {
                    sb.append("?" + entry.getKey() + "=" + entry.getValue());
                    firstFlag = false;
                } else {
                    sb.append("&" + entry.getKey() + "=" + entry.getValue());
                }
            }
        }
        return sb;
    }

    /**
     * 调用okhttp的newCall方法
     * @param request
     * @return
     */
    private static String execNewCall(Request request){
        Response response = null;
        try {
            OkHttpClient okHttpClient = SpringUtils.getBean(OkHttpClient.class);
            System.out.println("开始请求");
//            开始异步请求
            Call call = okHttpClient.newCall(request);//3.使用client去请求
            System.out.println("请求完毕，等待数据异步返回");
            call.enqueue(new Callback() {//4.回调方法
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("发生错误了！");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();//获得数据
                    com.swg.httpclinet.result.Response res = JsonUtils.jsonToPojo(result, com.swg.httpclinet.result.Response.class);

                    String[]  data = res.getData();


//pipeline分批来塞值，一次1000
                    long currtime = System.currentTimeMillis();
                    System.out.println("开始插入redis");
                    Jedis jedis = new Jedis("127.0.0.1",6379);
                    Pipeline pipeline = jedis.pipelined();
                    //如果数据量少于1000，则直接塞到一个pipleline中执行即可
                    if(data.length <= PIPELINE_COUNT){
                        for(String str : data){
                            pipeline.sadd("pipeline",str);
                        }
                        pipeline.sync();
                    }else {
                        int segment = data.length / PIPELINE_COUNT + 1;
                        int index = 1;
                        while(index <= segment){
                            for(int i=index*PIPELINE_COUNT;i<index*PIPELINE_COUNT+PIPELINE_COUNT&&i<data.length;i++){
                                pipeline.sadd("pipeline",data[i]);
                            }
                            pipeline.sync();
                            index++;
                        }
                    }
                    jedis.close();
                    System.out.println("插入redis结束");
                    long lasttime = System.currentTimeMillis();
                    System.out.println((lasttime-currtime)/1000+"秒！");
                    System.out.println("收到数据，请求结束");


//                    pipeline一次性把所有的值都塞进去发送给redis
//                    long currtime = System.currentTimeMillis();
//                    System.out.println("开始插入redis");
//                    Jedis jedis = new Jedis("127.0.0.1",6379);
//                    Pipeline pipeline = jedis.pipelined();
//                    for(String str : data){
//                        pipeline.sadd("pipeline",str);
//                    }
//                    pipeline.sync();
//                    jedis.close();
//                    System.out.println("插入redis结束");
//                    long lasttime = System.currentTimeMillis();
//                    System.out.println((lasttime-currtime)/1000+"秒！");
//                    System.out.println("收到数据，请求结束");


//                    普通一条一条塞
//                    long currtime = System.currentTimeMillis();
//                    System.out.println("开始插入redis");
//                    Jedis jedis = new Jedis("127.0.0.1",6379);
//
//                    for(String str : data){
//                        jedis.sadd("common-insert",str);
//                    }
//                    jedis.close();
//                    System.out.println("插入redis结束");
//                    long lasttime = System.currentTimeMillis();
//                    System.out.println((lasttime-currtime)/1000+"秒！");
//                    //开始入库
//                    System.out.println("收到数据，请求结束");

                }
            });
//            同步请求的写法
//            response = okHttpClient.newCall(request).execute();
//            if (response.isSuccessful()) {
//                return response.body().string();
//            }
        } catch (Exception e) {
            logger.error("okhttp3 put error >> ex = {}", ExceptionUtils.getStackTrace(e));
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return "";
    }

    private static void insertDataToRedis(com.swg.httpclinet.result.Response res) {
        for(int i=0; i<50; i++){
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
//                    String[]  data = res.getData();
//                    long currtime = System.currentTimeMillis();
//                    System.out.println("开始插入redis");
//                    RedisTemplate redisTemplate = SpringUtils.getBean("redisTemplate",RedisTemplate.class);
//
//                    for(String str : data){
//                        redisTemplate.opsForSet().add("test6",str);
//                    }
//                    System.out.println("插入redis结束");
//                    long lasttime = System.currentTimeMillis();
//                    System.out.println((lasttime-currtime)/1000+"秒！");
//                    //开始入库
//                    System.out.println("收到数据，请求结束");
                }
            });
        }
        threadPool.shutdown();
    }

    /**
     * get
     * @param url     请求的url
     * @param queries 请求的参数，在浏览器？后面的数据，没有可以传null
     * @return
     */
    public static String get(String url, Map<String, String> queries) {
        StringBuffer sb = getQueryString(url,queries);
        Request request = new Request.Builder()
                .url(sb.toString())
                .build();
        return execNewCall(request);
    }

    /**
     * post
     *
     * @param url    请求的url
     * @param params post form 提交的参数
     * @return
     */
    public static String postFormParams(String url, Map<String, String> params) {
        FormBody.Builder builder = new FormBody.Builder();
        //添加参数
        if (params != null && params.keySet().size() > 0) {
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
        }
        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
        return execNewCall(request);
    }


    /**
     * Post请求发送JSON数据....{"name":"zhangsan","pwd":"123456"}
     * 参数一：请求Url
     * 参数二：请求的JSON
     * 参数三：请求回调
     */
    public static String postJsonParams(String url, String jsonParams) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return execNewCall(request);
    }

    /**
     * Post请求发送xml数据....
     * 参数一：请求Url
     * 参数二：请求的xmlString
     * 参数三：请求回调
     */
    public static String postXmlParams(String url, String xml) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), xml);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return execNewCall(request);
    }
}