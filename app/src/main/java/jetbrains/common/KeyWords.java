package jetbrains.common;

public enum KeyWords {
    IF("if"),
    THEN("then"),
    ELSE("else"),
    WHILE("while"),
    DO("do"),
    FUN("fun"),
    RETURN("return");

    private String value;

    private KeyWords(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
