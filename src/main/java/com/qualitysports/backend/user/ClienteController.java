package com.qualitysports.backend.user;

import com.qualitysports.backend.user.dto.PasswordChangeRequest;
import com.qualitysports.backend.user.dto.PerfilResponse;
import com.qualitysports.backend.user.dto.PerfilUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cliente")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENTE')")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping("/perfil")
    public PerfilResponse getPerfil(@AuthenticationPrincipal User usuario) {
        return clienteService.getPerfil(toCliente(usuario));
    }

    @PutMapping("/perfil")
    public PerfilResponse updatePerfil(
            @AuthenticationPrincipal User usuario,
            @Valid @RequestBody PerfilUpdateRequest req
    ) {
        return clienteService.updatePerfil(toCliente(usuario), req);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal User usuario,
            @Valid @RequestBody PasswordChangeRequest req
    ) {
        clienteService.changePassword(toCliente(usuario), req);
    }

    private Cliente toCliente(User usuario) {
        if (!(usuario instanceof Cliente c)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo clientes pueden acceder a este recurso");
        }
        return c;
    }
}
