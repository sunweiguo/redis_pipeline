package com.swg.httpserver.result;

import lombok.Data;

@Data
public class Response {
    private String code;
    private String[] data;
}
