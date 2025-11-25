package com.example.AppSaumerios.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoUpdateDTO {

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    private String descripcion;

    // Aseguramos que el precio no sea negativo si se envía
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
    private BigDecimal precio;

    // Aseguramos que el precio mayorista no sea negativo si se envía
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio mayorista debe ser cero o mayor")
    private BigDecimal precioMayorista;

    // Aseguramos que el stock no sea negativo si se envía
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    // El total ingresado puede ser nulo en una actualización, pero si se envía, debe ser >= 0
    @Min(value = 0, message = "El total ingresado no puede ser negativo")
    private Integer totalIngresado;

    private Boolean activo;
    private String imagenUrl;
    private Boolean destacado;
}
