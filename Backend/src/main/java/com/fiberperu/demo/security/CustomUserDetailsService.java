package com.fiberperu.demo.security;

import com.fiberperu.demo.entity.Usuario;
import com.fiberperu.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado con el correo: " + correo
                        )
                );

        SimpleGrantedAuthority autoridad =
                new SimpleGrantedAuthority(
                        usuario.getRol().getNombre().name()
                );

        return new org.springframework.security.core.userdetails.User(
                usuario.getCorreo(),
                usuario.getContrasenaHash(),
                Boolean.TRUE.equals(usuario.getEstado()),
                true,
                true,
                true,
                List.of(autoridad)
        );
    }
}
