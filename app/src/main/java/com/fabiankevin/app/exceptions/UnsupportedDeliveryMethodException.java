package com.fabiankevin.app.exceptions;

import com.fabiankevin.app.models.enums.DeliveryMethod;

public class UnsupportedDeliveryMethodException extends AppException {
    public UnsupportedDeliveryMethodException(DeliveryMethod deliveryMethod) {
        super(400, "Unsupported delivery method: "+deliveryMethod);
    }
}
