package com.peolly.storageservice.external;

import java.util.List;
import java.util.UUID;

public record ReserveItemsRequest(
        List<Long> itemIds,
        UUID userId
) {
}