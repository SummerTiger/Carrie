import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String encoded = encoder.encode(password);
        System.out.println("Plain: " + password);
        System.out.println("Encoded: " + encoded);
        System.out.println("\nVerify test: " + encoder.matches(password, encoded));
    }
}
