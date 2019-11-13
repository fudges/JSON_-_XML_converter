package converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Node {
    public String path = "";
    public String element = "";
    public String elementValue = "";
    public LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
    public ArrayList<Node> children = new ArrayList<>();

    public ArrayList<Node> getChildren() {
        return children;
    }

    public LinkedHashMap<String, String> getAttributes() {
        return attributes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public String getElementValue() {
        return elementValue;
    }

    public void setElementValue(String elementValue) {
        this.elementValue = elementValue;
    }

    public Node() {

    }

    public void addAddtribute(String key, String value){
        this.attributes.put(key, value);
    }

    public void addChild(Node node){
        this.children.add(node);
    }
}
