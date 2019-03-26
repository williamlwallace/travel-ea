package util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.algorithms.Algorithm;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.security.AlgorithmConstraints;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Random;


/**
 *  Hashes and salts passwords and handles password verification as well as securely generating authentication tokens
 */
public class CryptoManager {

    /**
     * Check if a password attempt matches the existing salted hash
     * @param passwordToCheck New password being attempted
     * @param salt Salt data associated with user account (In string format)
     * @param existingHashedPassword Existing password that has already been salted and hashed
     * @return True if passwords match, false otherwise
     */
    public static boolean checkPasswordMatch(String passwordToCheck, String salt, String existingHashedPassword){
        return checkPasswordMatch(passwordToCheck, Base64.getDecoder().decode(salt), existingHashedPassword);
    }

    /**
     * Check if a password attempt matches the existing salted hash
     * @param passwordToCheck New password being attempted
     * @param salt Salt data associated with user account
     * @param existingHashedPassword Existing password that has already been salted and hashed
     * @return True if passwords match, false otherwise
     */
    private static boolean checkPasswordMatch(String passwordToCheck, byte[] salt, String existingHashedPassword) {
        // First hash the given password with salt
        String newHashedPassword = hashPassword(passwordToCheck, salt);

        // Now return if this matches the existing hashed password
        return existingHashedPassword.equals(newHashedPassword);
    }

    /**
     * Hash a password with a given salt
     * @param password Password to hash
     * @param salt Salt for password
     * @return Salted and hashed password
     */
    public static String hashPassword(String password, byte[] salt) {
        // Define iterations and key length
        Integer iterations = 10000;
        Integer keyLength = 256;

        // Now salt and hash the password
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            SecretKey key = skf.generateSecret(spec);
            byte[] res = key.getEncoded();
            return Base64.getEncoder().encodeToString(res);

        }
        catch( NoSuchAlgorithmException | InvalidKeySpecException e ) {
            throw new RuntimeException( e );
        }
    }

    /**
     * Generates a new random base64 string to use as a password salt
     * @return
     */
    public static String generateNewSalt() {
        return Base64.getEncoder().encodeToString(generateNewSaltBytes());
    }

    /**
     * Generates a 32 byte salt to use when salting a new password
     * MAKE SURE TO SAVE THE SALT
     * @return 32 byte salt
     */
    private static byte[] generateNewSaltBytes() {
        // Generate salt (32 bytes at minimum)
        byte[] salt = new byte[32];
        Random rand = new SecureRandom();
        rand.nextBytes(salt);

        // Return salt
        return salt;
    }

    /**
     * Generate JSON web token
     * @return JWT
     */
    public static String createToken(Long userId, String secret) {
        Algorithm algorithm;
        try {
            algorithm = Algorithm.HMAC256(secret);
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return JWT.create()
                .withIssuer("TravelEA")
                .withClaim("userId", userId)
                .sign(algorithm);
    }

    public static Long veryifyToken(String token, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("TravelEA")
                .build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("userId").asLong();
        } 
        catch (JWTVerificationException | java.io.UnsupportedEncodingException e) {
            return null;
        }
    }
}
