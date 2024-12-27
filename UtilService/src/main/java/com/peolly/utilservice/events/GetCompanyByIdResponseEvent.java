package com.peolly.utilservice.events;

import java.io.Serializable;

public record GetCompanyByIdResponseEvent(
        Long companyId,
        boolean isFound,
        String companyName)
implements Serializable {
}
