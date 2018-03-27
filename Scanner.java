package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jlox.TokenType.*;

class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	
	private int start = 0; // the start of the lexeme we're currently looking at
	private int current = 0; // the current character within said lexeme
	private int line = 1; // the line we're on

	private static final Map<String, TokenType> keywords;

	static {
		keywords = new HashMap<>();
		keywords.put("and", AND);
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("for", FOR);
		keywords.put("fun", FUN);
		keywords.put("if", IF);
		keywords.put("nil", NIL);
		keywords.put("or", OR);
		keywords.put("print", PRINT);
		keywords.put("return", RETURN);
		keywords.put("super", SUPER);
		keywords.put("this", THIS);
		keywords.put("true", TRUE);
		keywords.put("var", VAR);
		keywords.put("while", WHILE);
	}



	// constructor
	Scanner(String source) {
		this.source = source;
	}

	// works through the source, returning a list of the tokens it finds
	List<Token> scanTokens() {
		while (!isAtEnd()) {
			// beginning of the next lexeme
			start = current;
			scanToken();
		}
		
		// once we've scanned them all, add an EOF for good measure
		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	// scans a single token
	private void scanToken() {
		char c = advance();
		switch (c) {
			case '(': addToken(LEFT_PAREN); break;
			case ')': addToken(RIGHT_PAREN); break;
			case '{': addToken(LEFT_BRACE); break;
			case '}': addToken(RIGHT_BRACE); break;
			case ',': addToken(COMMA); break;
			case '.': addToken(DOT); break;
			case '-': addToken(MINUS); break;
			case '+': addToken(PLUS); break;
			case ';': addToken(SEMICOLON); break;
			case '*': addToken(STAR); break;
			
			// need to check for double-char operators for these
			case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
			case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
			case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
			case '>': addToken(match('=') ? GREATER_EQUAL: GREATER); break;

			// need to check for comments for /
			case '/':
				if (match('/')) {
					// if it IS a comment, consume the rest of the line
					while (peek() != '\n' && !isAtEnd()) { advance(); }
				}
				else if (match('*')) {
					// if it's a block comment, call a method to deal with it
					blockComment();
				}
				else {
					addToken(SLASH);
				}
				break;
			
			// if we encounter a ", we know it's a string
			case '"': string(); break;

			// ignore whitespace
			case ' ':
			case '\r':
			case '\t':
				break;

			// increment lines though
			case '\n':
				line++;
				break;

			// everything else
			default:
				if (isDigit(c)) {
					number();
				}
				else if (isAlpha(c)) {
					identifier();
				}
				else {
					Lox.error(line, "Unexpected character.");
					break;
				}
		}
	
	}

	// parsing block comments
	// I decided to allow nesting, but this means you MUST close them
	private void blockComment() {
		// munch characters, mostly ignoring them
		while (!isAtEnd()) {
			char c = advance();
			switch (c) {
				case '\n':
					line++;
					break;

				// check if the block is being closed
				case '*':
					if (match('/')) { return; }
					break; // ?

				// check if a new block is being opened
				// if so, recurse to handle it
				case '/':
					if (match('*')) { blockComment(); }
					break;
			}
		}
		// if we've made it out of that while loop without returning
		// then we must have reached the end of the file without all of
		// our blocks closing
		// which I'm arbitrarily deciding is a syntax error

		Lox.error(line, "Unterminated block comment.");
		return;
	}


	// strings are complicated
	// enough so to stick their logic in a separate function
	private void string() {
		// consume the string until you reach an endquote
		// or the end of the source
		// multi-line strings are allowed, so we must increment line when necessary
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') {
				line++;
			}
			advance();
		}

		// if you've reached the end of the file
		if (isAtEnd()) {
			Lox.error(line, "Unterminated string.");
			return;
		}

		// eat up the endquote
		advance();

		// trim the surrounding quotes and add the final string as a token
		String value = source.substring(start + 1, current - 1);
		addToken(STRING, value);
	}

	
	// ditto for numbers
	private void number() {
		while (isDigit(peek())) { advance(); }

		// check for fractional part
		// if there is one, consume it
		if (peek() == '.' && isDigit(peek2())) {
			// consume the dot
			advance();

			while (isDigit(peek())) { advance(); }
		}

		addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
	}
	
	// and why not identifiers
	private void identifier() {
		while (isAlphaNumeric(peek())) { advance(); }

		// check if it's a reserved word
		String text = source.substring(start, current);

		TokenType type = keywords.get(text);
		if (type == null) { type = IDENTIFIER; }
		addToken(type);
	}


	// helpers
	// ---------------------------------------------------------------

	// checks if we've finished working through the source
	private boolean isAtEnd() {
		return current >= source.length();
	}

	// return the next character, consuming the old one
	private char advance() {
		current++;
		return source.charAt(current - 1);
	}
	
	// return the next character WITHOUT consuming the old one
	private char peek() {
		if (isAtEnd()) return '\0';
		return source.charAt(current);
	}

	// return the character 2 characters ahead without consuming either
	private char peek2() {
		if (current + 1 >= source.length()) { return '\0'; }
		return source.charAt(current + 1);
	}

	// sort of a conditional advance()
	// only consumes the next character if it's what we expect
	// used to check for multi-char operators (eg "<" vs "<=")
	private boolean match(char expected) {
		if (isAtEnd()) return false;
		if (source.charAt(current) != expected) return false;

		current++;
		return true;
	}


	// add a token with no other details (note overloaded)
	private void addToken(TokenType type) {
		addToken(type, null);
	}

	// add a token WITH details (note overloaded)
	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}

	// checks if a char is a digit from 0 to 9
	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	// checks if a char is a letter of the alphabet
	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	//check if a character is alpahnumeric
	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}
}
