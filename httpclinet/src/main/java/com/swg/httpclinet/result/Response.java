package com.swg.httpclinet.result;

import lombok.Data;

@Data
public class Response {
    private String code;
    private String[] data;
}
