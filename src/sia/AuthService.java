package sia;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

public final class AuthService {
    private final Map<String, Usuario> byUsername = new HashMap<>();
    private final Map<String, Usuario> byId = new HashMap<>();

    public Optional<Usuario> login(String username, char[] password) {
        Usuario usuario = byUsername.get(username.toLowerCase());
        if (usuario == null) {
            return Optional.empty();
        }

        String attemptedHash = hash(password, usuario.getSalt());
        if (attemptedHash.equals(usuario.getPasswordHash())) {
            return Optional.of(usuario);
        }
        return Optional.empty();
    }

    public Usuario register(String username, char[] password, String rol, String personaId) {
        String salt = newSalt();
        String passwordHash = hash(password, salt);
        String id = "U" + (byId.size() + 1);

        Usuario usuario = new Usuario(id, username, rol, personaId, salt, passwordHash);
        byUsername.put(username.toLowerCase(), usuario);
        byId.put(id, usuario);
        return usuario;
    }

    public Optional<Usuario> findByUsername(String username) {
        return Optional.ofNullable(byUsername.get(username.toLowerCase()));
    }

    public void loadUsers(List<Usuario> usuarios) {
        byUsername.clear();
        byId.clear();
        for (Usuario u : usuarios) {
            byUsername.put(u.getUsername().toLowerCase(), u);
            byId.put(u.getId(), u);
        }
    }

    public List<Usuario> getAll() {
        return new ArrayList<>(byId.values());
    }

    public static String hash(char[] password, String saltHex) {
        try {
            byte[] salt = hexToBytes(saltHex);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] pw = new String(password).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] out = md.digest(pw);
            Arrays.fill(pw, (byte) 0);
            return bytesToHex(out);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String newSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return bytesToHex(salt);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}