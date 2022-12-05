package com.chiknas.swancloud.api.apiservices.user;

import java.time.LocalDateTime;

public class CurrentUserDetails {
    private String lastUploadedFileDate;

    public LocalDateTime getLastUploadedFileDate() {
        return LocalDateTime.parse(lastUploadedFileDate);
    }
}
