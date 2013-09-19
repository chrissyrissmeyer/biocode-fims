package renderers;

import digester.Rule;

/**
 * Handle messaging for
 */
public class Message {
    private String message;
    private java.util.List list;

    private Rule r;
    private Integer row;

    private Integer level;

    public static final Integer WARNING = 0;
    public static final Integer ERROR = 1;

    public Message(String message, Integer level) {
        this(message, level, null);
    }

    public Message(String message, Integer level, Integer row) {
        this(message, null, level, row);
    }

    public Message(String message, java.util.List list, Integer level, Integer row) {
        this.message = message;
        this.row = row;
        this.list = list;
        this.level = level;
    }

    /**
     * @return Message for this line
     */
    public String print() {

        // Check that there is stuff in this list
        String listString = "";
        if (list != null)
            listString = list.toString();
        String msg = "";
        if (this.row != null) {
            Integer msgRow = this.row + 1;
            return getLevelAsString() + ": Row " + (msgRow).toString() + ": " + message + listString;
        }
        return getLevelAsString() + ": " + message + listString;
    }

    public String getLevelAsString() {
        if (level == 0) return "Warning";
        else return "Error";
    }

    public Integer getLevel() {
        return level;
    }
}

