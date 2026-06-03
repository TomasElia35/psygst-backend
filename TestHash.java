import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // El hash bcrypt no se puede "desencriptar". 
        // Para recuperar el acceso, debes crear un nuevo hash y actualizar la base de datos.
        String nuevaContrasena = "admin123";
        String nuevoHash = encoder.encode(nuevaContrasena);
        
        System.out.println("Tu nueva contraseña es: " + nuevaContrasena);
        System.out.println("Copia este hash y pégalo en la base de datos: " + nuevoHash);
    }
}
