package com.ies21.gestionproductos.domain.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record InventorySummary(int total, long available, long lowStock, long outOfStock,
        Map<String, Long> byCategory, Map<String, Long> lowStockByCategory) {
    public InventorySummary {
        byCategory = Collections.unmodifiableMap(new LinkedHashMap<>(byCategory));
        lowStockByCategory = Collections.unmodifiableMap(new LinkedHashMap<>(lowStockByCategory));
    }
}
