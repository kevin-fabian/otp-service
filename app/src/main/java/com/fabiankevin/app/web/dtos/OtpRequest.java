package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import lombok.Builder;

@Builder(toBuilder = true)
public record OtpRequest(
        String userIdentifier,
        OtpPurpose purpose,
        DeliveryMethod deliveryMethod,
        String metadata
) {
    public GenerateOtpCommand toCommand() {
        return GenerateOtpCommand.builder()
                .userIdentifier(this.userIdentifier)
                .purpose(this.purpose)
                .deliveryMethod(this.deliveryMethod)
                .metadata(this.metadata)
                .build();
    }
}