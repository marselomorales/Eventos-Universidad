package sia.persistence;

import sia.Usuario;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class AuthStorage {
    private static final String SEP = ";";
    
    private AuthStorage() {}
    
    public static List<Usuario> loadUsers() throws IOException {
        List<Usuario> usuarios = new ArrayList<>();
        Path file = Paths.get("data/usuarios.csv");
        
        if (!Files.exists(file)) {
            return usuarios;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) return usuarios;
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(SEP, -1);
                if (parts.length < 6) continue;
                
                Usuario usuario = new Usuario(
                    parts[0].trim(), 
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    parts[4].trim(),
                    parts[5].trim()
                );
                usuarios.add(usuario);
            }
        }
        return usuarios;
    }
    
    public static void saveUsers(List<Usuario> usuarios) throws IOException {
        Path dir = Paths.get("data");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        
        Path file = dir.resolve("usuarios.csv");
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("id;username;rol;personaId;salt;passwordHash\n");
            
            for (Usuario usuario : usuarios) {
                writer.write(String.join(SEP,
                    usuario.getId(),
                    usuario.getUsername(),
                    usuario.getRol(),
                    usuario.getPersonaId() != null ? usuario.getPersonaId() : "",
                    usuario.getSalt(),
                    usuario.getPasswordHash()
                ));
                writer.write("\n");
            }
        }
    }
}