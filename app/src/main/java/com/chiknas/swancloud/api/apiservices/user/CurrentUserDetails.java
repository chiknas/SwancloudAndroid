package com.chiknas.swancloud.api.apiservices.user;

import java.time.LocalDate;

public class CurrentUserDetails {
    private String lastUploadedFileDate;

    public LocalDate getLastUploadedFileDate() {
        return LocalDate.parse(lastUploadedFileDate);
    }
}
