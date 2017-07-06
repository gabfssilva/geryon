package org.geryon;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
public class RequestIdentifier {
    private String method;
    private String path;

    public RequestIdentifier(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestIdentifier)) return false;

        RequestIdentifier that = (RequestIdentifier) o;

        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        return path != null ? path.equals(that.path) : that.path == null;
    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}
