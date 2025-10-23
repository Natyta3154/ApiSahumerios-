package com.example.AppSaumerios.util;

import com.example.AppSaumerios.dto.ProductoDTO;
import com.example.AppSaumerios.dto.ProductoDTO.OfertaSimpleDTO;
import com.example.AppSaumerios.dto.OfertaDTO;
import com.example.AppSaumerios.dto.ProductoUpdateDTO;
import com.example.AppSaumerios.entity.Fragancia;
import com.example.AppSaumerios.entity.ProductoAtributo;
import com.example.AppSaumerios.entity.Productos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ProductoMapper {

    // =========================
    // ENTITY -> DTO
    // =========================
    public static ProductoDTO toDTO(Productos producto, List<OfertaDTO> todasLasOfertas) {
        ProductoDTO dto = new ProductoDTO();

        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setPrecioMayorista(producto.getPrecioMayorista());
        dto.setStock(producto.getStock());
        dto.setTotalIngresado(producto.getTotalIngresado());
        dto.setImagenUrl(producto.getImagenUrl());
        dto.setActivo(producto.getActivo());
        dto.setCategoriaNombre(
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null
        );
        dto.setDestacado(producto.getDestacado());

        // ✅ Atributos como List
        List<ProductoDTO.ProductoAtributoDTO> atributos = producto.getProductoAtributos().stream()
                .map(pa -> new ProductoDTO.ProductoAtributoDTO(
                        pa.getAtributo().getNombre(),
                        pa.getValor()
                ))
                .collect(Collectors.toList());
        dto.setAtributos(atributos);

        // ✅ Fragancias como List
        List<String> fragancias = producto.getFragancias().stream()
                .map(Fragancia::getNombre)
                .collect(Collectors.toList());
        dto.setFragancias(fragancias);

        // ✅ Ofertas activas
        List<OfertaSimpleDTO> ofertas = todasLasOfertas != null ?
                todasLasOfertas.stream()
                        .filter(of -> of.getProductoId() != null && of.getProductoId().equals(producto.getId()))
                        .filter(ProductoMapper::ofertaActiva)
                        .map(of -> {
                            OfertaSimpleDTO oDto = new OfertaSimpleDTO();
                            oDto.setIdOferta(of.getIdOferta());
                            oDto.setValorDescuento(of.getValorDescuento());
                            oDto.setTipoDescuento(of.getTipoDescuento());
                            oDto.setFechaInicio(of.getFechaInicio());
                            oDto.setFechaFin(of.getFechaFin());
                            oDto.setEstado(of.getEstado());
                            oDto.setPrecio(calcularPrecioConDescuento(producto.getPrecio(), of));
                            return oDto;
                        })
                        .collect(Collectors.toList())
                : List.of();
        dto.setOfertas(ofertas);

        // ✅ Actualizar precio principal si hay ofertas activas
        ofertas.stream().findFirst().ifPresent(o -> dto.setPrecio(o.getPrecio()));

        return dto;
    }

    // =========================
    // DTO -> ENTITY
    // =========================
    public static Productos toEntity(ProductoDTO dto) {
        Productos producto = new Productos();

        producto.setId(dto.getId());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setPrecioMayorista(dto.getPrecioMayorista());
        producto.setStock(dto.getStock());
        producto.setTotalIngresado(dto.getTotalIngresado());
        producto.setImagenUrl(dto.getImagenUrl());
        producto.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return producto;
    }

    public static void updateEntityFromDTO(ProductoDTO dto, Productos producto) {
        if (dto.getNombre() != null) producto.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) producto.setDescripcion(dto.getDescripcion());
        if (dto.getPrecio() != null) producto.setPrecio(dto.getPrecio());
        if (dto.getPrecioMayorista() != null) producto.setPrecioMayorista(dto.getPrecioMayorista());
        if (dto.getStock() != null) producto.setStock(dto.getStock());
        if (dto.getTotalIngresado() != null) producto.setTotalIngresado(dto.getTotalIngresado());
        if (dto.getImagenUrl() != null) producto.setImagenUrl(dto.getImagenUrl());
        if (dto.getActivo() != null) producto.setActivo(dto.getActivo());
    }

    // =========================
    // Métodos auxiliares
    // =========================
    private static boolean ofertaActiva(OfertaDTO of) {
        LocalDate hoy = LocalDate.now();
        return of.getEstado() != null && of.getEstado()
                && (of.getFechaInicio() == null || !hoy.isBefore(of.getFechaInicio()))
                && (of.getFechaFin() == null || !hoy.isAfter(of.getFechaFin()));
    }

    private static BigDecimal calcularPrecioConDescuento(BigDecimal precio, OfertaDTO of) {
        if (precio == null || of.getValorDescuento() == null) return precio;

        switch (of.getTipoDescuento()) {
            case "PORCENTAJE":
                return precio.subtract(precio.multiply(of.getValorDescuento().divide(BigDecimal.valueOf(100)))).max(BigDecimal.ZERO);
            case "MONTO":
                return precio.subtract(of.getValorDescuento()).max(BigDecimal.ZERO);
            default:
                return precio;
        }
    }

    // metodo para hacer la conversion de ProductoUpdateDTO a ProductoDTO
    // método para hacer la conversión de ProductoUpdateDTO a ProductoDTO
    public static ProductoDTO dtoFromUpdate(ProductoUpdateDTO updateDTO) {
        ProductoDTO dto = new ProductoDTO();

        dto.setNombre(updateDTO.getNombre());
        dto.setDescripcion(updateDTO.getDescripcion());
        dto.setPrecio(updateDTO.getPrecio());
        dto.setPrecioMayorista(updateDTO.getPrecioMayorista());
        dto.setStock(updateDTO.getStock());
        dto.setImagenUrl(updateDTO.getImagenUrl());
        dto.setActivo(updateDTO.getActivo());
        dto.setDestacado(updateDTO.getDestacado());

        return dto;

    }

}
