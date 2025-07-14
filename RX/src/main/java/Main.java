import interpreter.Interpreter;
import repl.Repl;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        //TODO: Add more args/flags such as verbose mode, debug mode, repl(?) etc...
        //TODO: Metaprogramming -> Rules can have Rules as parameters -> OOP?
        //https://www.researchgate.net/scientific-contributions/Tim-Reichert-69812364

        //TODO: Errorhandling

        if(args.length < 1) {
            System.err.println("Usage: rx [-i <file> | -r]");
            System.exit(1);
        }

        String mode = args[0];

        switch (mode) {
            case "-i":
                if (args.length < 2) {
                    System.err.println("Usage: rx -i <file> [-d] [-h]");
                    System.exit(1);
                }

                String fileArg = args[1];

                boolean debug = false;
                boolean highlight = false;

                for (int i = 2; i < args.length; i++) {
                    switch (args[i]) {
                        case "-d" -> debug = true;
                        case "-h" -> highlight = true;
                        default -> {
                            System.err.println("Unknown option: " + args[i]);
                            System.exit(1);
                        }
                    }
                }

                if (highlight && !debug) {
                    System.err.println("Error: -h (highlight) requires -d (debug) mode.");
                    System.exit(1);
                }

                Interpreter interpreter = new Interpreter(debug, highlight);
                interpreter.interpret(Path.of(fileArg));
                break;

            case "-r":
                if (args.length > 1) {
                    System.err.println("Usage: rx -r");
                    System.exit(1);
                }
                Repl repl = new Repl();
                repl.start();
                break;
            default:
                System.err.println("Usage: rx [-i <file> | -r]");
                System.exit(1);
        }
    }
}
