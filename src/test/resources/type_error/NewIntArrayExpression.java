class NewIntArrayExpressionTest {
    public static void main(String[] args) {
        int i;
        int[] ia;
        long l;
        long[] la;
        boolean b;
        A cA;

        //ia = new int[i]; // OK!
        ia = new int[ia]; // INVALID_SIZE_TYPE
        ia = new int[l]; // INVALID_SIZE_TYPE
        ia = new int[la]; // INVALID_SIZE_TYPE
        ia = new int[b]; // INVALID_SIZE_TYPE
        ia = new int[cA]; // INVALID_SIZE_TYPE
    }
}
class A {}
