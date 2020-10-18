package com.swg.httpserver.api;

import com.swg.httpserver.result.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

@RestController
public class TestController {

    // 使用ArrayList来存储每行读取到的字符串
    ArrayList<String> arrayList = new ArrayList<>();

    @RequestMapping("/test2")
    public Response test(@RequestParam(name = "categoryId") String categoryId) {
        System.out.println("接收到的参数为："+categoryId);
        Response response = initData();
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return  response;
    }

    private Response initData() {
        String[] arr = toArrayByFileReader1("C:\\Users\\fossi\\Desktop\\test\\zongyi_idlist.txt");
        Response response = new Response();
        response.setCode("C00000");
        response.setData(arr);
        return response;
    }


    private String[] toArrayByFileReader1(String name) {
        arrayList = new ArrayList<>();
        try {
            FileReader fr = new FileReader(name);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            // 按行读取字符串
            while ((str = bf.readLine()) != null) {
                arrayList.add(str);
            }
            bf.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对ArrayList中存储的字符串进行处理
        int length = arrayList.size();
        String[] array = new String[length];
        for (int i = 0; i < length; i++) {
            array[i] = arrayList.get(i);
        }
        System.out.println("收到的数据长度为"+array.length);
        // 返回数组
        return array;
    }
}
