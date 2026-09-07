package com.BUSY.learnWithUs.Dto.Progress;

import com.BUSY.learnWithUs.Entity.ProgressStatus;

public record ProgressRequest(
        ProgressStatus status
) {
}
