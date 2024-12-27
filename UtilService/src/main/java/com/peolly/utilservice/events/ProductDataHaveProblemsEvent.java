package com.peolly.utilservice.events;

import java.io.Serializable;
import java.util.List;

public record ProductDataHaveProblemsEvent(
        List<String> invalidFields)
implements Serializable {
}
