package com.swg.httpclinet.test;

import com.swg.httpclinet.util.OkHttpUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("test")
public class TestController {
    @RequestMapping("1")
    public String test(){
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("categoryId","00001");
        OkHttpUtil.get("http://localhost:8888/test2",paramMap);

        return "success";
    }
}
