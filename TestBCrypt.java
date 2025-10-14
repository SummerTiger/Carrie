import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "admin123";
        String hash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi";

        System.out.println("Testing BCrypt hash validation:");
        System.out.println("Plain password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("Matches: " + encoder.matches(password, hash));

        // Generate a fresh hash to compare
        String newHash = encoder.encode(password);
        System.out.println("\nGenerated new hash: " + newHash);
        System.out.println("New hash matches: " + encoder.matches(password, newHash));
    }
}
