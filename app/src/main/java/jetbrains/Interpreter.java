package jetbrains;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jetbrains.common.*;

public class Interpreter {

    static Set<String> globalVariables = new HashSet<>();

    public static void main(String[] args) {
    }

    public static void findVariables(String input) throws Exception {

        String[] splittedInput = input.split("\\s+");

        int braceDepth = 0;

        for (int i = 0; i < splittedInput.length; i++) {
            String h = splittedInput[i];

            if (h.equals("{"))
                braceDepth++;
            else if (h.equals("}"))
                braceDepth--;

            if (braceDepth == 0) {
                if (h.equals("=")) {
                    if (i == 0)
                        throw new Exception("Incorrect syntax");

                    globalVariables.add(splittedInput[i - 1]);
                }
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
        return "let " + statement;
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
            return res + handleLine(subString) + "}";
        }

        pointer += 4;
        res += handleLine(subString) + "} else";
        // Code after else
        subString = "";
        while (pointer < statementSize) {
            subString += statement.charAt(pointer++);
        }
        res += "{" + handleLine(subString) + "}";
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

        return res + handleLine(subString) + "}";
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
        return res + handleLine(subString) + "}";
    }

    public static String handleLine(String codeLine) throws Exception {
        String word = "";
        int lineSize = codeLine.length();
        int pointer = 0;

        List<String> statements = new ArrayList();

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

        List<String> results = new ArrayList<>(counter + 1);

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

            if (word.equals(KeyWords.IF.getValue())) {
                results.set(i, handleIf(codeLine, pointer));
            } else if (word.equals(KeyWords.WHILE.getValue())) {
                results.set(i, handleWhile(codeLine, pointer));

            } else if (word.equals(KeyWords.FUN.getValue())) {
                results.set(i, handleFun(codeLine, pointer));

            } else if (word.equals(KeyWords.RETURN.getValue())) {
                results.set(i, codeLine);

            } else {
                results.set(i, handleVariables(codeLine, pointer, word));
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
                .replace("(", " ( ").replace(")", " ) ");
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
