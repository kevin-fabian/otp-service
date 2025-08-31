package com.fabiankevin.app.services.otp.commands;

import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import lombok.Builder;

@Builder(toBuilder = true)
public record GenerateOtpCommand(
        String recipient,
        OtpPurpose purpose,
        DeliveryMethod deliveryMethod,
        String metadata
) {
}
