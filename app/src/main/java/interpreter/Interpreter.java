package interpreter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import interpreter.common.*;

public class Interpreter {

    static Set<String> globalVariables = new HashSet<>();
    static Set<String> funcParameters = new HashSet<>();

    public static void main(String[] args) {
    }

    public static void findVariables(String input) throws Exception {

        String[] splittedInput = input.split("\\s+");

        int braceDepth = 0;

        for (int i = 0; i < splittedInput.length; i++) {
            String h = splittedInput[i];

            if (braceDepth != 0 && !h.equals(")") && !h.equals(",")) {
                funcParameters.add(h);
            }

            if (h.equals("("))
                braceDepth++;
            else if (h.equals(")"))
                braceDepth--;
            if (h.equals("=")) {
                if (i == 0)
                    throw new Exception("Incorrect syntax");

                if (!funcParameters.contains(h))
                    globalVariables.add(splittedInput[i - 1]);
            }

        }
    }

    // Returns the line without \n
    public static ProgramSlice cutLine(String sourceProgram, int oldPointer, int programSize) {
        String line = "";
        int newPointer = oldPointer;
        while (newPointer < programSize && sourceProgram.charAt(newPointer) != '\n') {
            line += sourceProgram.charAt(newPointer++);
        }

        // Met \n
        return newPointer + 1 < programSize ? new ProgramSlice(line, newPointer + 1, true)
                : new ProgramSlice(line, newPointer, false);
    }

    public static String handleVariables(String statement, int pointer, String variableName) {
        int statementSize = statement.length();

        while (pointer < statementSize && statement.charAt(pointer) != ' ' && statement.charAt(pointer) != '=')
            variableName += statement.charAt(pointer++);
        if (globalVariables.contains(variableName)) {
            return statement;
        }
        return statement;
    }

    public static String handleIf(String statement, int pointer) throws Exception {
        String res = "if (";
        int statementSize = statement.length();

        // if-head
        while (pointer < statementSize && !"then"
                .equals(statement.substring(pointer, statementSize - pointer > 4 ? pointer + 4 : statementSize - 1))) {
            res += statement.charAt(pointer++);
        }
        if (pointer >= statementSize)
            throw new Exception("Incorrect if starement");
        res += ") {";
        pointer += 4;
        String subString = "";
        // Then
        while (pointer < statementSize && !"else"
                .equals(statement.substring(pointer, statementSize - pointer > 4 ? pointer + 4 : statementSize - 1))) {
            subString += statement.charAt(pointer++);
        }
        if (pointer >= statementSize) {
            return res + handleStatement(subString) + "}";
        }

        pointer += 4;
        res += handleStatement(subString) + "} else";
        // Code after else
        subString = "";
        while (pointer < statementSize) {
            subString += statement.charAt(pointer++);
        }
        res += "{" + handleStatement(subString) + "}";
        return res;
    }

    public static String handleWhile(String statement, int pointer) throws Exception {
        String res = "while (";
        int statementSize = statement.length();
        // iter till do
        while (pointer < statementSize && !"do"
                .equals(statement.substring(pointer, statementSize - pointer > 2 ? pointer + 2 : statementSize - 1)))
            res += statement.charAt(pointer++);
        res += ")";
        if (pointer >= statementSize) {
            return res + ";";
        }
        res += "{";
        pointer += 2;
        String subString = "";
        while (pointer < statementSize)
            subString += statement.charAt(pointer++);

        return res + handleStatement(subString) + "}";
    }

    public static String handleFun(String statement, int pointer) throws Exception {
        String res = "function ";
        int statementSize = statement.length();
        while (pointer < statementSize && statement.charAt(pointer) != ')')
            res += statement.charAt(pointer++);
        if (pointer >= statementSize)
            throw new Exception("Incorrect function definition");
        res += statement.charAt(pointer++);
        while (pointer < statementSize && statement.charAt(pointer) == ' ')
            pointer++;
        if (pointer >= statementSize)
            return res + ";";
        if (statement.charAt(pointer++) != '{')
            throw new Exception("Incorrect function definition");
        res += "{";
        String subString = "";
        while (pointer < statementSize && statement.charAt(pointer) != '}')
            subString += statement.charAt(pointer++);
        return res + handleStatement(subString) + "}";
    }

    public static String handleLine(String codeLine) throws Exception {
        String word = "";
        int lineSize = codeLine.length();
        int pointer = 0;

        String res = "";

        while (pointer < lineSize && codeLine.charAt(pointer) == ' ')
            pointer++;
        while (pointer < lineSize && codeLine.charAt(pointer) >= 65 && codeLine.charAt(pointer) <= 90
                || codeLine.charAt(pointer) >= 97 && codeLine.charAt(pointer) <= 122)
            word += codeLine.charAt(pointer++);

        res = switch (word) {
            case "if" -> handleIf(codeLine, pointer);
            case "while" -> handleWhile(codeLine, pointer);
            case "fun" -> handleFun(codeLine, pointer);
            case "return" -> codeLine;
            default -> handleVariables(codeLine, pointer, word);
        };

        return res;
    }

    public static String handleStatement(String codeLine) throws Exception {
        String word = "";
        int lineSize = codeLine.length();
        int pointer = 0;

        List<String> statements = new ArrayList<>();

        statements.add("");
        int counter = 0;
        while (pointer < lineSize) {
            if (codeLine.charAt(pointer) == ',') {
                counter++;
                statements.add("");

            } else {
                statements.set(counter, statements.get(counter) + codeLine.charAt(pointer));
            }
            pointer++;
        }

        List<String> results = new LinkedList<>();

        for (int i = 0; i < counter + 1; i++) {
            word = "";
            pointer = 0;
            lineSize = statements.get(i).length();
            codeLine = statements.get(i);
            while (pointer < lineSize && codeLine.charAt(pointer) == ' ')
                pointer++;
            while (pointer < lineSize && codeLine.charAt(pointer) >= 65 && codeLine.charAt(pointer) <= 90
                    || codeLine.charAt(pointer) >= 97 && codeLine.charAt(pointer) <= 122)
                word += codeLine.charAt(pointer++);

            switch (word) {
                case "if" -> results.add(handleIf(codeLine, pointer));
                case "while" -> results.add(handleWhile(codeLine, pointer));
                case "fun" -> results.add(handleFun(codeLine, pointer));
                case "return" -> results.add(codeLine);
                default -> results.add(handleVariables(codeLine, pointer, word));
            }

        }

        String res = "";
        for (int i = 0; i < counter + 1; i++) {
            res += results.get(i) + ";";
        }

        return res;
    }

    public static String interpret(String sourceProgram) throws Exception {
        sourceProgram = sourceProgram.replace("{", " { ").replace("}", " } ")
                .replace("(", " ( ").replace(")", " ) ").replace(",", " , ");
        String res = "";
        int programSize = sourceProgram.length();

        findVariables(sourceProgram);
        for (String v : globalVariables) {
            res += "let " + v + ";\n";
        }
        // res = let...; let...;

        int pointer = 0;
        String codeLine = "";
        boolean run = true;
        ProgramSlice programSlice;

        while (run) {
            programSlice = cutLine(sourceProgram, pointer, programSize);
            run = programSlice.notLastLine();
            // codeLine without \n
            codeLine = programSlice.codeLine();
            pointer = programSlice.newPointer();

            res += handleLine(codeLine);
            res += '\n';
        }

        for (String v : globalVariables) {
            res += "console.log(\"" + v + ": \" + " + v + ")";
            res += "\n";
        }

        return res;
    }
}
