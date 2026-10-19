package com.ies21.gestionproductos.infrastructure.features.reports;

import com.ies21.gestionproductos.domain.service.InventoryReportService;
import com.ies21.gestionproductos.domain.model.ProductStatus;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Presentación del reporte: el dominio entrega datos, no HTML ni estilos. */
public final class InventoryReportRenderer {
    /**
     * Evita interpretar nombres y categorías del usuario como etiquetas HTML.
     * @param value texto que se incluirá en el documento
     * @return texto con caracteres especiales escapados
     */
    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
    private String statusLabel(ProductStatus status) {
        return switch (status) {
            case ALL -> "Todos";
            case AVAILABLE -> "Disponibles";
            case UNAVAILABLE -> "No disponibles";
            case LOW_STOCK -> "Stock bajo";
            case OUT_OF_STOCK -> "Agotados";
        };
    }
    /**
     * Construye la tabla y el resumen que muestra el JEditorPane de reportes.
     * No consulta MongoDB: utiliza exclusivamente los datos de la instantánea recibida.
     * @param data resultados y fecha generados por el servicio
     * @return documento HTML local con importes en formato argentino
     */
    public String renderHtml(InventoryReportService.Report data) {
        var snapshot = data.products();
        var category = data.category();
        var status = data.status();
        var currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
        var value = data.value();
        String date = data.generatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss z"));
        StringBuilder html = new StringBuilder("<html><body style='font-family:Segoe UI;color:#141D30;background:white;margin:14px;font-size:11px'>");
        html.append("<h2 style='margin:0'>INVENTORYMANAGER</h2><h3 style='margin:6px 0'>Reporte de catálogo e inventario</h3>");
        html.append("<p>Fecha de generación: ").append(date).append("<br>Datos del inventario persistido<br>");
        html.append("Categoría: ").append(escape(category == null ? "Todas" : category));
        html.append(" · Estado: ").append(escape(statusLabel(status))).append("</p>");
        html.append("<table width='100%' cellspacing='0' cellpadding='5'>");
        html.append("<tr bgcolor='#144F80' style='color:white'><th align='left'>ID / CÓDIGO</th><th align='left'>NOMBRE DEL PRODUCTO</th><th align='left'>CATEGORÍA</th><th>PRECIO</th><th>STOCK</th><th>DISPONIBLE</th></tr>");
        int row = 0;
        for (var p : snapshot) {
            html.append("<tr bgcolor='").append(row++ % 2 == 0 ? "#FFFFFF" : "#F2F6FC").append("'><td>").append(escape(p.id()))
                    .append("</td><td>").append(escape(p.name())).append("</td><td>").append(escape(p.category()))
                    .append("</td><td align='right'>").append(currency.format(p.price())).append("</td><td align='right'>")
                    .append(p.stock()).append(" un.</td><td style='color:").append(p.available() ? "#00996F" : "#FF385C")
                    .append("'>").append(p.available() ? "Sí" : "No").append("</td></tr>");
        }
        html.append("</table>");
        if (snapshot.isEmpty()) html.append("<p>No hay productos registrados para estos filtros.</p>");
        html.append("<hr><p><b>Total de productos: ").append(snapshot.size()).append(" · Valuación: ")
                .append(currency.format(value)).append(" ARS</b></p><p>Valuación calculada como precio × stock.</p></body></html>");
        return html.toString();
    }
    /**
     * Presenta la misma información como texto plano, sin marcas HTML.
     * @param data instantánea que se desea representar
     * @return reporte legible con los campos de cada producto
     */
    public String renderText(InventoryReportService.Report data) {
        var snapshot = data.products();
        var currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
        StringBuilder report = new StringBuilder("INVENTORYMANAGER\nREPORTE DE INVENTARIO\n");
        report.append("Fecha de generación: ").append(data.generatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss z")));
        report.append("\nTotal de productos: ").append(snapshot.size()).append("\nDatos del inventario persistido\n");
        report.append("─".repeat(64)).append('\n');
        if (snapshot.isEmpty()) report.append("No hay productos registrados.\n");
        for (var product : snapshot) {
            report.append("ID: ").append(product.id()).append('\n');
            report.append("Nombre: ").append(product.name()).append('\n');
            report.append("Categoría: ").append(product.category()).append('\n');
            report.append("Precio: ").append(currency.format(product.price())).append(" ARS\n");
            report.append("Stock: ").append(product.stock()).append(" unidades\n");
            report.append("Disponible: ").append(product.available() ? "Sí" : "No").append('\n');
            report.append("─".repeat(64)).append('\n');
        }
        return report.toString();
    }

}
