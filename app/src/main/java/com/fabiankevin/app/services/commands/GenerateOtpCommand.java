package com.fabiankevin.app.services.commands;

import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import lombok.Builder;

@Builder(toBuilder = true)
public record GenerateOtpCommand(
        String userIdentifier,
        OtpPurpose purpose,
        OtpStatus status,
        DeliveryMethod deliveryMethod,
        String metadata
) {
}
