package com.peolly.utilservice.events;

import java.io.Serializable;

public record GetCompanyByIdEvent(
        Long companyId)
implements Serializable {
}
