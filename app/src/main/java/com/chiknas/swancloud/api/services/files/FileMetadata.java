package com.chiknas.swancloud.api.services.files;

import java.time.LocalDate;

public class FileMetadata {

    private Integer id;
    private String fileName;
    private String createdDate;

    public Integer getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public LocalDate getCreatedDate() {
        return LocalDate.parse(createdDate);
    }
}
