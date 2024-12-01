package com.peolly.utilservice.events;

import java.io.Serializable;
import java.util.List;

public record SendProductDataHaveProblemsEvent(
        List<String> invalidFields)
implements Serializable {
}
