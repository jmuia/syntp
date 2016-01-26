import java.util.StringTokenizer;

/**
 * Created by jmuia on 2016-01-21.
 */

public class SyntpVersion implements Comparable<SyntpVersion> {
    final static String SYNTP = "SYNTP";
    int major;
    int minor;
    int patch;

    public SyntpVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public SyntpVersion(String version) throws Exception {
        StringTokenizer tokens = new StringTokenizer(version, "/");
        if (tokens.countTokens() != 2) {
            throw new Exception("Semantic version must be of the form SYNTP/MAJOR.MINOR.PATCH");
        }
        String syntp = tokens.nextToken();
        if (!syntp.equals(SYNTP)) {
            throw new Exception("Semantic version must be of the form SYNTP/MAJOR.MINOR.PATCH");
        }

        tokens = new StringTokenizer(tokens.nextToken(), ".");
        if (tokens.countTokens() != 3) {
            throw new Exception("Semantic version must be of the form MAJOR.MINOR.PATCH");
        }

        this.major = Integer.parseInt(tokens.nextToken());
        this.minor = Integer.parseInt(tokens.nextToken());
        this.patch = Integer.parseInt(tokens.nextToken());
    }

    @Override
    public int compareTo(SyntpVersion that) {
        if (this.major > that.major) {
            return 1;
        } else if (this.major < that.major) {
            return -1;
        }

        if (this.minor > that.minor) {
            return 1;
        } else if (this.minor < that.minor) {
            return -1;
        }

        if (this.patch > that.patch) {
            return 1;
        } else if (this.patch < that.patch) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return SYNTP + "/" + this.major + "." + this.minor + "." + this.patch;
    }
}
