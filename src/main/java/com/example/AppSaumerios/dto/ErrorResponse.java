package com.example.AppSaumerios.dto;



import java.time.LocalDateTime;

public class ErrorResponse {
    private int status;
    private String error;
    private String mensaje;
    private LocalDateTime timestamp;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String mensaje) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setStatus(int status) {
        this.status = status;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Para logs/debug
    @Override
    public String toString() {
        return "ErrorResponse{" +
                "status=" + status +
                ", error='" + error + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
