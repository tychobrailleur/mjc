package mjc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import mjc.jasmin.JasminGenerator;
import mjc.jasmin.JasminHandler;
import mjc.lexer.Lexer;
import mjc.lexer.LexerException;
import mjc.parser.Parser;
import mjc.parser.ParserException;
import mjc.symbol.SymbolTable;
import mjc.node.InvalidToken;
import mjc.node.Node;
import mjc.analysis.ASTGraphPrinter;
import mjc.analysis.ASTPrinter;
import mjc.analysis.SymbolTableBuilder;
import mjc.analysis.TypeChecker;
import mjc.error.MiniJavaError;
import static mjc.error.MiniJavaErrorType.LEXER_ERROR;
import static mjc.error.MiniJavaErrorType.PARSER_ERROR;

public class JVMMain {
    private final static CommandLineParser commandLineParser = new GnuParser();
    private final static HelpFormatter helpFormatter = new HelpFormatter();
    private final static Options options = new Options();

    private final ASTPrinter astPrinter = new ASTPrinter();
    private final ASTGraphPrinter graphPrinter = new ASTGraphPrinter();

    public JVMMain() {
        options.addOption("S", false, "only output Jasmin assembly code");
        options.addOption("p", false, "print abstract syntax tree");
        options.addOption("g", false, "print abstract syntax tree in GraphViz format");
        options.addOption("h", false, "show help message");

        helpFormatter.setOptionComparator(new OptionComparator<Option>());
    }

    public static void main(String[] args) {
        JVMMain main = new JVMMain();

        try {
            // Run the compiler.
            if (!main.run(args)) {
                System.exit(1);
            }
        } catch (ParseException e) {
            printHelp();
            System.exit(1);
        }
    }

    /**
     * Run compiler with the given command line arguments.
     *
     * @param args Command line arguments.
     * @return true if compilation succeeded, otherwise false.
     * @throws ParseException if parsing of command line arguments failed.
     */
    private boolean run(String[] args) throws ParseException {

        final CommandLine commandLine = commandLineParser.parse(options, args);

        if (commandLine.hasOption("h")) {
            printHelp();
            return true;
        }

        if (commandLine.getArgs().length != 1) {
            printHelp();
            return false;
        }

        /****************************************
         * Stage 1: Lexical Analysis / Parsing. *
         ****************************************/

        final Node ast;
        try {
            final String fileName = commandLine.getArgs()[0];
            final PushbackReader reader = new PushbackReader(new FileReader(fileName));
            final Parser parser = new Parser(new Lexer(reader));
            ast = parser.parse();
        } catch (LexerException e) {
            final InvalidToken token = e.getToken();
            final int line = token.getLine();
            final int column = token.getPos();
            System.err.println(LEXER_ERROR.on(line, column, token.getText()));
            return false;
        } catch (ParserException e) {
            System.err.println(PARSER_ERROR.on(e.getLine(), e.getPos(), e.getError()));
            return false;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }

        if (commandLine.hasOption("p"))
            astPrinter.print(ast);

        if (commandLine.hasOption("g"))
            graphPrinter.print(ast);

        /*******************************
         * Stage 2: Semantic Analysis. *
         *******************************/

        // Build symbol table.
        final SymbolTableBuilder builder = new SymbolTableBuilder();
        final SymbolTable symbolTable = builder.build(ast);
        if (builder.hasErrors()) {
            for (MiniJavaError error : builder.getErrors()) {
                System.err.println(error);
            }
        }

        // Run type-check.
        final TypeChecker typeChecker = new TypeChecker();
        if (!typeChecker.check(ast, symbolTable)) {
            for (MiniJavaError error : typeChecker.getErrors()) {
                System.err.println(error);
            }
        }

        if (builder.hasErrors() || typeChecker.hasErrors()) {
            return false; // Abort compilation.
        }

        /****************************
         * Stage 3: Code Generation *
         ****************************/

        final JasminGenerator generator = new JasminGenerator(new JasminHandler() {
            public void handle(String className, StringBuilder code) {
                // Write to .j file.
                final Path path = Paths.get(className + ".j");
                final Charset charset = StandardCharsets.UTF_8;
                try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
                    writer.append(code);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!commandLine.hasOption("S")) {
                    // Invoke Jasmin to assemble the file.
                    jasmin.Main.main(new String[] { className + ".j" });
                }
            }
        });
        generator.generate(ast, symbolTable, typeChecker.getTypes());

        return true;
    }

    /**
     * Prints a help message to standard output.
     */
    private static void printHelp() {
        helpFormatter.printHelp("mjc <infile> [options]", options);
    }

    // Comparator for Options, to get them in the order we want in help output.
    class OptionComparator<T extends Option> implements Comparator<T> {
        private static final String ORDER = "Spgh";

        @Override
        public int compare(T option1, T option2) {
            return ORDER.indexOf(option1.getOpt()) - ORDER.indexOf(option2.getOpt());
        }
    }
}
