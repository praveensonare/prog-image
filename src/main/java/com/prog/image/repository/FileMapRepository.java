package com.prog.image.repository;

import com.prog.image.model.FileMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileMapRepository extends JpaRepository<FileMap, Integer> {

}
