package com.prog.image.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "file_map")
public class FileMap {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "path", columnDefinition = "text")
    private String path;
    @Column(name = "fname", columnDefinition = "text")
    private String fname;
    @Column(name = "fmt", length = 10)
    private String fmt;

    public FileMap(String path, String fname, String fmt) {
        this.path = path;
        this.fname = fname;
        this.fmt = fmt;
    }
}
