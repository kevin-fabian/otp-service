package com.fabiankevin.app.exceptions;

import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.github.fabiankevin.microwebspringbootstarter.exceptions.ApiException;

public class UnsupportedDeliveryMethodException extends ApiException {
    public UnsupportedDeliveryMethodException(DeliveryMethod deliveryMethod) {
        super("Unsupported delivery method: %s".formatted(deliveryMethod), 400);
    }
}
