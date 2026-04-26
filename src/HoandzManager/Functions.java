
package HoandzManager;

/**
 *
 * @author Bùi Công Hoan
 */
import java.util.Random;
import java.util.regex.Pattern;
import player.Player;

public class Functions {
    private static final String REGEX = "\\b(dkm|\u0111km|\u0111brr|\u0111\u1ecbt|\u0111\u0129|\u0111\u1ef9|cm|cmm|l\u1ed3n|bu\u1ed3i|cc|\u00f4m cl|m\u1eb9 m\u00e0y|c\u1eb7c|\u0111\u1ee5|fuck|damn|clmm|dcmm|cl|tml|\u0111*t|c*c|dit|d*t|c.a.c|l.o.n|c.\u1eb7.c|l.\u1ed3.n|b.u.\u1ed3.i|bu*i|\u0111\u1eb7c c\u1ea7u|\u0111\u1ed3n l\u1ea7u|b\u00fa cu|buscu|\u0111m|cc|\u0111b|db|lol|nhu lon|nhu cac|vc|vl|v\u00e3i|\u0111\u00e9o|\u0111\u1edd m\u1edd|\u0111\u1edd c\u1edd m\u1edd|clgt|dell|m\u1eb9|c\u1ee9t|shit|idiot|kh\u1ed1n|xi\u00ean ch\u1ebft|c\u1ee5|giao ph\u1ed1i|thi\u1ec3u n\u0103ng|ng\u00e1o|ch\u00f3|dog|\u0111cmm|vcl|vkl|\u0111!t|d!t|\u0111\u1ef5t|dyt|ngu|\u00f3c|.com|.net|.online|.vn|.pw|.pro|.org|.info|.ml|.ga|.gq|.cf|.fun|.xyz|.io|.club)\\b";
    private static Pattern pattern = Pattern.compile("\\b(dkm|\u0111km|\u0111brr|\u0111\u1ecbt|\u0111\u0129|\u0111\u1ef9|cm|cmm|l\u1ed3n|bu\u1ed3i|cc|\u00f4m cl|m\u1eb9 m\u00e0y|c\u1eb7c|\u0111\u1ee5|fuck|damn|clmm|dcmm|cl|tml|\u0111*t|c*c|dit|d*t|c.a.c|l.o.n|c.\u1eb7.c|l.\u1ed3.n|b.u.\u1ed3.i|bu*i|\u0111\u1eb7c c\u1ea7u|\u0111\u1ed3n l\u1ea7u|b\u00fa cu|buscu|\u0111m|cc|\u0111b|db|lol|nhu lon|nhu cac|vc|vl|v\u00e3i|\u0111\u00e9o|\u0111\u1edd m\u1edd|\u0111\u1edd c\u1edd m\u1edd|clgt|dell|m\u1eb9|c\u1ee9t|shit|idiot|kh\u1ed1n|xi\u00ean ch\u1ebft|c\u1ee5|giao ph\u1ed1i|thi\u1ec3u n\u0103ng|ng\u00e1o|ch\u00f3|dog|\u0111cmm|vcl|vkl|\u0111!t|d!t|\u0111\u1ef5t|dyt|ngu|\u00f3c|.com|.net|.online|.vn|.pw|.pro|.org|.info|.ml|.ga|.gq|.cf|.fun|.xyz|.io|.club)\\b", 2);

    public static boolean checkspam(String text) {
        return pattern.matcher(text.toLowerCase()).find();
    }

    public static boolean checkspam(Player player, String text) {
        return pattern.matcher(text.toLowerCase()).find() && !player.name.equals("Hoandz");
    }

    public static int maxint(long n) {
        return (int)(n > Integer.MAX_VALUE ? Integer.MAX_VALUE : n);
    }

    public static String generateRandomCharacters(int quantity) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < quantity; ++i) {
            int type = random.nextInt(2);
            char generatedChar = type == 0 ? (char)(random.nextInt(10) + 48) : (char)(random.nextInt(26) + 65);
            sb.append(generatedChar);
        }
        return sb.toString();
    }

    public static void sleep(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    public static void sleep1Milis() {
        try {
            Thread.sleep(1L);
        } catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }
}
