package com.BUSY.learnWithUs.Dto.Bulk;

import java.util.List;

public record BulkEnrollmentResponse(
        List<BulkResult> results
) {
}