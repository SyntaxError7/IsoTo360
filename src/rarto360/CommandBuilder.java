package rarto360;

/**
 * Builds a string array suitable for Runtime.exec
 *
 * @author Jesenko Mehmedbasic
 *         created 23-11-11, 20:27
 */
public class CommandBuilder {
    private String parameter;

    public CommandBuilder(Iterable<String> parameters) {

        StringBuilder builder = new StringBuilder();
        for (String p : parameters) {
            builder.append(p);
            builder.append(" ");
        }
        this.parameter = builder.toString();

    }

    public String[] build() {
        String[] result = new String[3];
        result[0] = "cmd";
        result[1] = "/C";
        result[2] = parameter;
        return result;
    }
}
