public class Test {
    public static void main(String[] args) {
        byte[] cafebabe = new byte[]{-54,-2,-70,-66};
        int result = toInt(cafebabe);
        System.out.println(result);
       // System.out.println(Integer.toHexString(result));
    }

    private static int toInt(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result <<= 8;
            result |= bytes[i] & 0xFF;
        }
        return result;
    }
}