class ArrayAssignStatementTest {
    public static void main(String[] args) {
        int i;
        int[] ia;
        long l;
        long[] la;
        boolean b;
        A cA;
        B cB;

        ia[ia] = i;   // WRONG_INDEX_TYPE
        ia[l] = i;    // WRONG_INDEX_TYPE
        ia[la] = i;   // WRONG_INDEX_TYPE
        ia[b] = i;    // WRONG_INDEX_TYPE
        ia[cA] = i;   // WRONG_INDEX_TYPE

        la[ia] = l;  // WRONG_INDEX_TYPE
        la[l] = l;   // WRONG_INDEX_TYPE
        la[la] = l;  // WRONG_INDEX_TYPE
        la[b] = l;   // WRONG_INDEX_TYPE
        la[cA] = l;  // WRONG_INDEX_TYPE

        A[i] = i; // EXPECTED_VARIABLE_GOT_CLASS
        U[i] = i; // UNDECLARED_IDENTIFIER

        ia[i] = ia; // INVALID_ASSIGNMENT
        ia[i] = l;  // INVALID_ASSIGNMENT
        ia[i] = la; // INVALID_ASSIGNMENT
        ia[i] = b;  // INVALID_ASSIGNMENT
        ia[i] = cA; // INVALID_ASSIGNMENT

        la[i] = ia; // INVALID_ASSIGNMENT
        la[i] = la; // INVALID_ASSIGNMENT
        la[i] = b;  // INVALID_ASSIGNMENT
        la[i] = cA; // INVALID_ASSIGNMENT

        i[i] = i;  // NOT_ARRAY_TYPE
        l[i] = i;  // NOT_ARRAY_TYPE
        b[i] = i;  // NOT_ARRAY_TYPE
        cA[i] = i; // NOT_ARRAY_TYPE
    }
}
class A {}
class B {}