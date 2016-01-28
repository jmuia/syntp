/**
 * Created by jmuia on 2016-01-28.
 */
public class SyntpError extends Exception {
    int statusCode;

    public SyntpError(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        System.out.println(message);
    }

}

