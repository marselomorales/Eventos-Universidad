package sia;

public final class Usuario {
    private String id;
    private String username;
    private String rol;
    private String personaId;
    private String salt;
    private String passwordHash;

    public Usuario() {}

    public Usuario(String id, String username, String rol, String personaId, String salt, String passwordHash) {
        this.id = id;
        this.username = username;
        this.rol = rol;
        this.personaId = personaId;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getPersonaId() { return personaId; }
    public void setPersonaId(String personaId) { this.personaId = personaId; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}