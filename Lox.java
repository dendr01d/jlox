package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
	
	// don't run code that has errors
	static boolean hadError = false;


	public static void main(String[] args) throws IOException {	
		if (args.length > 1) {
			System.out.println("Usage: jlox [scriptName]");
		}
		else if (args.length == 1) {
			// run the specified script
			runFile(args[0]);
		}
		else {
			// run the REPL
			runPrompt();
		}
	}

	// just run the script
	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));

		// if there was an error then tell the commandline as much
		if (hadError) {
			System.exit(1);
		}
	}
	
	// run an interactive prompt
	private static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		System.out.print("Use Control-C to exit\n");

		for (;;) {
			System.out.print("> ");
			run(reader.readLine());

			// reset error flag on line-by-line basis
			hadError = false;
		}
	}

	// the actual run function for which the latter two are shells
	private static void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();

		// For now we'll just print the tokens back out
		for (Token token : tokens) {
			System.out.println(token);
		}
	}

	static void error(int line, String message) {
		report(line, "", message);
	}
	
	private static void report(int line, String where, String message) {
		System.err.println("[Line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}
	

}
