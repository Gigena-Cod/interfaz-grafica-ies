package com.ies21.gestionproductos.domain.model;

import java.math.BigDecimal;

public record Product(String id, String name, String category, BigDecimal price, int stock, boolean available) {
}
