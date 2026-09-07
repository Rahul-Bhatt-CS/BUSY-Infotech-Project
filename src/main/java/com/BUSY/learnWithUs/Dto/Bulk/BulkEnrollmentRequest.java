package com.BUSY.learnWithUs.Dto.Bulk;

import java.util.List;

public record BulkEnrollmentRequest(
        List<String> emails
) {
}