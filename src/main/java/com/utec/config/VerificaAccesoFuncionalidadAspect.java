package com.utec.config;


import com.utec.config.RequiereFuncionalidad;
import com.utec.model.Usuario;
import com.utec.repository.UsuarioRepository;
import com.utec.repository.PerfilRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class VerificaAccesoFuncionalidadAspect {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Around("@annotation(requiereFuncionalidad)")
    public Object verificarAcceso(ProceedingJoinPoint joinPoint, RequiereFuncionalidad requiereFuncionalidad) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        String username = auth.getName();

        Usuario usuario = usuarioRepository.findByCorreo(username)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));

        Integer perfilId = usuario.getPerfil().getIdPerfil();
        String funcionalidad = requiereFuncionalidad.valor();

        boolean tieneAcceso = perfilRepository.existeFuncionalidadParaPerfil(perfilId, funcionalidad);

        if (!tieneAcceso) {
            throw new AccessDeniedException("No tiene permiso para la funcionalidad: " + funcionalidad);
        }

        return joinPoint.proceed();
    }

}
