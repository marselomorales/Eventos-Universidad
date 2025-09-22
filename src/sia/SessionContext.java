package sia;

import java.util.Optional;

public final class SessionContext {
    private static SessionContext instance;
    private Usuario usuarioActual;
    private Persona personaVinculada;

    private SessionContext() {}

    public static SessionContext get() {
        if (instance == null) {
            instance = new SessionContext();
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return usuarioActual != null;
    }

    public Usuario getUsuario() {
        return usuarioActual;
    }

    public Persona getPersona() {
        return personaVinculada;
    }

    public void login(Usuario usuario, Persona persona) {
        this.usuarioActual = usuario;
        this.personaVinculada = persona;
    }

    public void logout() {
        this.usuarioActual = null;
        this.personaVinculada = null;
    }

    public Optional<String> getRol() {
        return isLoggedIn() ? Optional.of(usuarioActual.getRol()) : Optional.empty();
    }
}