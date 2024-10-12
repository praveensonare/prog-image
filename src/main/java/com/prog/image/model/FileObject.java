package com.prog.image.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.opencv.core.Mat;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class FileObject {
    String fileName;
    byte[] data;
    String fmt = "";

    public FileObject(String fileName, byte[] data, String fmt) {
        this.fileName = fileName;
        this.data = data;
        this.fmt = fmt;
    }
}
