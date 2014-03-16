MiniJava Programs with Type Errors
==================================

These are MiniJava with type errors. There is one test case for each language
construct. Each test case consists of a `.java` file with the input program,
and a corresponding `.expected` file containing the list of expected type errors.

The error codes listed in the `.expected` files refer to the `MiniJavaErrorType`
enum values. There should be one occurrance for each expected type error.

The test cases are used as input for the `TypeChecker` tests. The order in
which the expected errors are listed is not significant. As long as the
type-checker finds all listed errors (and only those), the test case will
pass.