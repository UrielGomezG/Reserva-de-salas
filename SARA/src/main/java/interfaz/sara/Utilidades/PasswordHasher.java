package interfaz.sara.Utilidades;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad para hash y verificación de contraseñas usando SHA-256
 * TODO: Considerar BCrypt o Argon2 para producción
 */
public class PasswordHasher {
    
    /**
     * Genera hash SHA-256 de la contraseña usando UTF-8
     * @return Hash hexadecimal de 64 caracteres, o null si hay error
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error al generar hash: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Verifica si una contraseña coincide con un hash
     */
    public static boolean verificarPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        
        String hashCalculado = hashPassword(password);
        return hashCalculado != null && hashCalculado.equals(hash);
    }
}
