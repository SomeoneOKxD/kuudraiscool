package someoneok.kic.utils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionCompat {
    private static final Pattern SIMPLE_WILDCARD = Pattern.compile("^\\d+(?:\\.\\d+)?(?:\\.x)?$");
    private static final Pattern MAJOR_WILDCARD  = Pattern.compile("^\\d+\\.x$");

    private static final Pattern EXACT_VERSION   = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");

    private static final Pattern COMPARATOR      = Pattern.compile("^(<=|>=|<|>|=|==)?\\s*(\\d+(?:\\.\\d+){0,2})$");

    public static boolean isCompatible(String hostApi, String constraint) {
        Version host = Version.parse(hostApi);
        constraint = constraint.trim();

        if (SIMPLE_WILDCARD.matcher(constraint).matches() || MAJOR_WILDCARD.matcher(constraint).matches()) {
            Version target = Version.parse(constraint.replaceAll("\\.x", ""));
            if (countDots(constraint) <= 1 && !constraint.endsWith(".x")) return host.major == target.major;
            if (countDots(constraint) == 1 || constraint.endsWith(".x")) return host.major == target.major && host.minor == target.minor;
        }

        if (EXACT_VERSION.matcher(constraint).matches()) return host.equals(Version.parse(constraint));

        if (constraint.startsWith("^")) {
            Version base = Version.parse(constraint.substring(1));
            Version upper;
            if (base.major > 0) upper = new Version(base.major + 1, 0, 0);
            else if (base.minor > 0) upper = new Version(0, base.minor + 1, 0);
            else upper = new Version(0, 0, base.patch + 1);
            return gte(host, base) && lt(host, upper);
        }

        if (constraint.startsWith("~")) {
            Version base = Version.parse(constraint.substring(1));
            Version upper = new Version(base.major, base.minor + 1, 0);
            return gte(host, base.normalizeForTilde()) && lt(host, upper);
        }

        String[] parts = constraint.split("\\s+");
        boolean any = false;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            Matcher m = COMPARATOR.matcher(part);
            if (!m.find()) return false;
            any = true;
            String op = m.group(1);
            String ver = m.group(2);
            Version v = Version.parse(ver);

            int cmp = host.compareTo(v);
            boolean ok;
            if (op == null || op.equals("=") || op.equals("==")) ok = cmp == 0;
            else if (op.equals("<"))  ok = cmp < 0;
            else if (op.equals("<=")) ok = cmp <= 0;
            else if (op.equals(">"))  ok = cmp > 0;
            else if (op.equals(">=")) ok = cmp >= 0;
            else ok = false;

            if (!ok) return false;
        }
        return any;
    }

    private static boolean gte(Version a, Version b) { return a.compareTo(b) >= 0; }
    private static boolean lt(Version a, Version b)  { return a.compareTo(b) <  0; }
    private static int countDots(String s) { int c = 0; for (char ch : s.toCharArray()) if (ch == '.') c++; return c; }

    private static final class Version implements Comparable<Version> {
        final int major, minor, patch;

        Version(int M, int m, int p) { this.major = M; this.minor = m; this.patch = p; }

        static Version parse(String s) {
            String[] t = s.split("\\.");
            int M = t.length > 0 ? parseInt(t[0]) : 0;
            int m = t.length > 1 ? parseInt(t[1]) : 0;
            int p = t.length > 2 ? parseInt(t[2]) : 0;
            return new Version(M, m, p);
        }

        Version normalizeForTilde() {
            return this;
        }

        static int parseInt(String x) {
            if (x == null || x.isEmpty()) return 0;
            if (x.equalsIgnoreCase("x")) return 0;
            return Integer.parseInt(x);
        }

        @Override public int compareTo(Version o) {
            if (major != o.major) return Integer.compare(major, o.major);
            if (minor != o.minor) return Integer.compare(minor, o.minor);
            return Integer.compare(patch, o.patch);
        }

        @Override public boolean equals(Object o) {
            if (!(o instanceof Version)) return false;
            Version v = (Version)o;
            return major == v.major && minor == v.minor && patch == v.patch;
        }

        @Override public int hashCode() { return Objects.hash(major, minor, patch); }

        @Override public String toString() { return major + "." + minor + "." + patch; }
    }
}
