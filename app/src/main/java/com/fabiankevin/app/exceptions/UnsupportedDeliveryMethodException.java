package com.fabiankevin.app.exceptions;

import com.fabiankevin.app.models.enums.DeliveryMethod;

public class UnsupportedDeliveryMethodException extends RuntimeException {
    public UnsupportedDeliveryMethodException(DeliveryMethod deliveryMethod) {
        super("Unsupported delivery method: "+deliveryMethod);
    }
}
