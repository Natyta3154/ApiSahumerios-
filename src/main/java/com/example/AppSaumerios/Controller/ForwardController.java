package com.example.AppSaumerios.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ForwardController {

    /**
     * Redirige todas las rutas no-API al index.html del frontend,
     * para que el enrutamiento del lado del cliente (React/Vite) funcione correctamente.
     */
    @GetMapping("/{path:^(?!api|usuarios|productos|ofertas|static|css|js|images).*$}/**")
    public String redirect() {
        return "forward:/index.html";
    }

    // 💡 Alternativa aún más segura (sin regex):
    // Si te vuelve a dar error por el parser de path, usá este método en su lugar:
    /*
    @GetMapping("/{path}/**")
    public String redirect(@PathVariable String path) {
        if (path.equals("api") || path.equals("usuarios") ||
            path.equals("productos") || path.equals("ofertas")) {
            // Deja pasar las rutas del backend
            return "forward:/" + path;
        }
        // Redirige todo lo demás al frontend (React)
        return "forward:/index.html";
    }
    */
}


