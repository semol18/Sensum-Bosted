/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sensum_bosted;

/**
 *
 * @author jakob
 */
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordHashing {
    
    private static final String delimiter = "%02x";
    private String password = "";
    private byte[] salt;

    MessageDigest md;

    public PasswordHashing() {
        // Generate the random salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        this.salt = salt;
    }

    public PasswordHashing(byte[] salt) {
        this.salt = salt;
    }

    public String hash(String password) {
        try {
            // Select the message digest for the hash computation -> MD5
            md = MessageDigest.getInstance("MD5");

            // Passing the salt to the digest for the computation
            md.update(salt);

            // Generate the salted hash
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format(delimiter, b));
            }

            System.out.println(sb);

            password = sb.toString();
            return password;
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return password;
    }
    
    public boolean compare(String password, String hash) {
        return hash.equals(hash(password));
    }
    
    public static byte[] extractSalt(String hash) {      
        
        String[] temp = hash.split(delimiter);
        return temp[1].getBytes(); 
        
    }
}
