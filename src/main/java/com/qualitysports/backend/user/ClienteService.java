package com.qualitysports.backend.user;

import com.qualitysports.backend.user.dto.PasswordChangeRequest;
import com.qualitysports.backend.user.dto.PerfilResponse;
import com.qualitysports.backend.user.dto.PerfilUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public PerfilResponse getPerfil(Cliente cliente) {
        return toResponse(cliente);
    }

    @Transactional
    public PerfilResponse updatePerfil(Cliente cliente, PerfilUpdateRequest req) {
        cliente.setNombre(req.nombre());
        cliente.setTelefono(req.telefono());
        cliente.setDireccionEnvio(req.direccionEnvio());
        clienteRepository.save(cliente);
        return toResponse(cliente);
    }

    @Transactional
    public void changePassword(Cliente cliente, PasswordChangeRequest req) {
        if (!passwordEncoder.matches(req.passwordActual(), cliente.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta");
        }
        cliente.setPassword(passwordEncoder.encode(req.passwordNueva()));
        clienteRepository.save(cliente);
    }

    private PerfilResponse toResponse(Cliente c) {
        return new PerfilResponse(c.getId(), c.getNombre(), c.getEmail(), c.getTelefono(), c.getDireccionEnvio());
    }
}
