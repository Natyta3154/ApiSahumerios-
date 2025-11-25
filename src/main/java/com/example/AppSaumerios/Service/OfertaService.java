package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.OfertaDTO;
import com.example.AppSaumerios.dto.ProductoOfertaDTO;
import com.example.AppSaumerios.entity.Ofertas;
import com.example.AppSaumerios.entity.Productos;
import com.example.AppSaumerios.repository.OfertaRepository;
import com.example.AppSaumerios.repository.ProductoRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OfertaService {

    private final OfertaRepository ofertaRepository;
    private final ProductoRepository productoRepository;

    public OfertaService(OfertaRepository ofertaRepository, ProductoRepository productoRepository) {
        this.ofertaRepository = ofertaRepository;
        this.productoRepository = productoRepository;
    }
    // =========================
// Productos para carrusel: solo ofertas activas
// =========================
    public List<ProductoOfertaDTO> obtenerProductosCarrusel(int limite) {
        // Obtener todas las ofertas activas y filtrar las que tengan descuento
        List<ProductoOfertaDTO> productosConOferta = listarOfertasConPrecioFinal().stream()
                .filter(p -> p.getPrecioConDescuento().compareTo(p.getPrecioOriginal()) != 0) // tiene descuento
                .sorted((p1, p2) -> p2.getPrecioOriginal().compareTo(p1.getPrecioOriginal())) // ordenar por precio descendente
                .limit(limite) // limitar a los top N
                .collect(Collectors.toList());

        return productosConOferta;
    }




    // =========================
    // Crear o actualizar oferta
    // =========================
    @CacheEvict(value = "ofertasTop", allEntries = true)
    public Ofertas crearOferta(OfertaDTO dto) {
        if (dto.getProductoId() == null) {
            throw new RuntimeException("El producto es obligatorio");
        }

        Productos producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Ofertas oferta = new Ofertas();
        oferta.setProducto(producto);
        oferta.setValorDescuento(dto.getValorDescuento());
        oferta.setTipoDescuento(dto.getTipoDescuento() != null ? dto.getTipoDescuento() : "PORCENTAJE");
        oferta.setFechaInicio(dto.getFechaInicio());
        oferta.setFechaFin(dto.getFechaFin());
        oferta.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
        oferta.setNombreProducto(producto.getNombre());
        oferta.setPrecio(producto.getPrecio());

        return ofertaRepository.save(oferta);
    }

    // =========================
    // Crear o actualizar oferta (devuelve DTO)
    // =========================
    public OfertaDTO crearOfertaDTO(OfertaDTO dto) {
        Ofertas oferta = crearOferta(dto);
        return mapOfertaToDTO(oferta);
    }

    // =========================
    // Listar todas las ofertas
    // =========================
    public List<Ofertas> listarOfertas() {
        return ofertaRepository.findAll();
    }

    // =========================
    // Listar todas las ofertas en DTO
    // =========================
    public List<OfertaDTO> listarOfertasDTO() {
        return listarOfertas().stream()
                .map(this::mapOfertaToDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // Listar productos con precio final aplicado
    // =========================
    @Cacheable("ofertasTop")
    public List<ProductoOfertaDTO> listarOfertasConPrecioFinal() {
        return ofertaRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // =========================
// Listar todas las ofertas como OfertaDTO
// =========================
    public List<OfertaDTO> obtenerTodasLasOfertasDTO() {
        return ofertaRepository.findAll().stream()
                .map(this::mapOfertaToDTO)
                .collect(Collectors.toList());
    }


    // =========================
    // Buscar oferta por ID
    // =========================
    public Ofertas buscarPorId(Long id) {
        return ofertaRepository.findById(id).orElse(null);
    }

    // =========================
    // Buscar oferta por ID en DTO
    // =========================
    public OfertaDTO buscarPorIdDTO(Long id) {
        Ofertas oferta = buscarPorId(id);
        return oferta != null ? mapOfertaToDTO(oferta) : null;
    }

    // =========================
    // Eliminar oferta
    // =========================
    @CacheEvict(value = "ofertasTop", allEntries = true)
    public void eliminar(Long id) {
        ofertaRepository.deleteById(id);
    }

    // =========================
    // Mapear entidad a DTO (ahora público)
    // =========================
    public OfertaDTO mapOfertaToDTO(Ofertas oferta) {
        OfertaDTO dto = new OfertaDTO();
        dto.setIdOferta(oferta.getIdOferta());
        dto.setProductoId(oferta.getProducto().getId());
        dto.setNombreProducto(oferta.getNombreProducto());
        dto.setDescripcion(oferta.getDescripcion());
        dto.setPrecio(oferta.getPrecio());
        dto.setTipoDescuento(oferta.getTipoDescuento());
        dto.setValorDescuento(oferta.getValorDescuento());
        dto.setFechaInicio(oferta.getFechaInicio());
        dto.setFechaFin(oferta.getFechaFin());
        dto.setEstado(oferta.getEstado());
        return dto;
    }

    // =========================
    // Mapeo interno de entidad a ProductoOfertaDTO
    // =========================
    private ProductoOfertaDTO mapToDTO(Ofertas oferta) {
        BigDecimal precioOriginal = oferta.getProducto().getPrecio();
        BigDecimal precioConDescuento = precioOriginal;

        boolean activa = oferta.getEstado()
                && LocalDate.now().isAfter(oferta.getFechaInicio().minusDays(1))
                && LocalDate.now().isBefore(oferta.getFechaFin().plusDays(1));

        if (activa) {
            if ("PORCENTAJE".equalsIgnoreCase(oferta.getTipoDescuento())) {
                BigDecimal descuento = precioOriginal
                        .multiply(oferta.getValorDescuento())
                        .divide(BigDecimal.valueOf(100));
                precioConDescuento = precioOriginal.subtract(descuento);
            } else if ("MONTO".equalsIgnoreCase(oferta.getTipoDescuento())) {
                precioConDescuento = precioOriginal.subtract(oferta.getValorDescuento());
            }

            if (precioConDescuento.compareTo(BigDecimal.ZERO) < 0) {
                precioConDescuento = BigDecimal.ZERO;
            }
        }

        ProductoOfertaDTO dto = new ProductoOfertaDTO(
                oferta.getProducto().getId(),
                oferta.getProducto().getNombre(),
                precioOriginal,
                precioConDescuento,
                oferta.getProducto().getImagenUrl()
        );


        // <-- Asignar fechas aquí
        dto.setFechaInicio(oferta.getFechaInicio());
        dto.setFechaFin(oferta.getFechaFin());

        return dto;
    }


}
