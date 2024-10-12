package com.prog.image.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ResponseObject {
    int id;
    String fileName;
    String status = "FAIL";
    String fmt = "";

    public ResponseObject(int id, String fileName, String status, String fmt) {
        this.id = id;
        this.fileName = fileName + "." + fmt;
        this.status = status;
        this.fmt = fmt;
    }
}
