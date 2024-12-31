import org.springframework.security.crypto.bcrypt.BCrypt;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        String plainPassword = "adminubuntu";
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        System.out.println("hashed password: " + hashedPassword);
    }
}