package converter;

public class Path {
    private String prevElementName = "";
    private String pathToElement = "";

    public Path() {
    }

    public String getPrevElementName() {
        return prevElementName;
    }

    public void setPrevElementName(String prevElementName) {
        this.prevElementName = prevElementName;
    }

    public String getPathToElement() {
        return pathToElement;
    }

    public void setPathToElement(String pathToElement) {
        this.pathToElement = pathToElement;
    }
}
