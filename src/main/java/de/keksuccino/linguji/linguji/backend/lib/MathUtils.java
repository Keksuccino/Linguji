package de.keksuccino.linguji.linguji.backend.lib;

import java.util.Random;

public class MathUtils {

	public static boolean isIntegerOrDouble(String value) {
    	try {
    		if (value.contains(".")) {
    			Double.parseDouble(value);
    		} else {
    			Integer.parseInt(value);
    		}
    		return true;
    	} catch (Exception ignored) {}
    	return false;
    }
	
	public static boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
    		return true;
    	} catch (Exception ignored) {}
    	return false;
	}
	
	public static boolean isDouble(String value) {
		try {
			Double.parseDouble(value);
    		return true;
    	} catch (Exception ignored) {}
    	return false;
	}
	
	public static boolean isLong(String value) {
		try {
			Long.parseLong(value);
    		return true;
    	} catch (Exception ignored) {}
    	return false;
	}
	
	public static boolean isFloat(String value) {
		try {
			Float.parseFloat(value);
    		return true;
    	} catch (Exception ignored) {}
    	return false;
	}
	
	public static int getRandomNumberInRange(int min, int max) {
		if (min >= max) {
			return min;
		}
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
	/**
	 * Returns the calculated value.
	 */
	public static double calculateFromString(final String in) {
		
		if (MathUtils.isDouble(in)) {
			return Double.parseDouble(in);
		}
		
	    try {
	    	return new Object() {
		        int pos = -1, ch;

		        void nextChar() {
		            ch = (++pos < in.length()) ? in.charAt(pos) : -1;
		        }

		        boolean eat(int charToEat) {
		            while (ch == ' ') nextChar();
		            if (ch == charToEat) {
		                nextChar();
		                return true;
		            }
		            return false;
		        }

		        double parse() {
		            nextChar();
		            double x = parseExpression();
		            if (pos < in.length()) throw new RuntimeException("Unexpected: " + (char)ch);
		            return x;
		        }

		        double parseExpression() {
		            double x = parseTerm();
		            for (;;) {
		                if      (eat('+')) x += parseTerm();
		                else if (eat('-')) x -= parseTerm();
		                else return x;
		            }
		        }

		        double parseTerm() {
		            double x = parseFactor();
		            for (;;) {
		                if      (eat('*')) x *= parseFactor();
		                else if (eat('/')) x /= parseFactor();
		                else return x;
		            }
		        }

		        double parseFactor() {
		            if (eat('+')) return parseFactor();
		            if (eat('-')) return -parseFactor();

		            double x;
		            int startPos = this.pos;
		            if (eat('(')) {
		                x = parseExpression();
		                eat(')');
		            } else if ((ch >= '0' && ch <= '9') || ch == '.') {
		                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
		                x = Double.parseDouble(in.substring(startPos, this.pos));
		            } else if (ch >= 'a' && ch <= 'z') {
		                while (ch >= 'a' && ch <= 'z') nextChar();
		                String func = in.substring(startPos, this.pos);
		                x = parseFactor();
		                if (func.equals("sqrt")) x = Math.sqrt(x);
		                else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
		                else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
		                else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
		                else throw new RuntimeException("Unknown function: " + func);
		            } else {
		                throw new RuntimeException("Unexpected: " + (char)ch);
		            }

		            if (eat('^')) x = Math.pow(x, parseFactor());

		            return x;
		        }
		    }.parse();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	    return 0.0D;
	}

}
