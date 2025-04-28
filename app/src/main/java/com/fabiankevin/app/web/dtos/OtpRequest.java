package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import lombok.Builder;

@Builder(toBuilder = true)
public record OtpRequest(
        String userIdentifier,
        OtpPurpose purpose,
        OtpStatus status,
        DeliveryMethod deliveryMethod,
        String metadata
) {
    public GenerateOtpCommand toCommand() {
        return GenerateOtpCommand.builder()
                .userIdentifier(this.userIdentifier)
                .purpose(this.purpose)
                .status(this.status)
                .deliveryMethod(this.deliveryMethod)
                .metadata(this.metadata)
                .build();
    }
}