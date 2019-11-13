package converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Node {
    public String path = "";
    public String elementKey = "";
    public String elementValue = "";
    public LinkedHashMap<String, String> attributesOrKeyValuePairs = new LinkedHashMap<>();
    public ArrayList<Node> children = new ArrayList<>();

    public ArrayList<Node> getChildren() {
        return children;
    }

    public LinkedHashMap<String, String> getAttributesOrKeyValuePairs() {
        return attributesOrKeyValuePairs;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getElementKey() {
        return elementKey;
    }

    public void setElementKey(String elementKey) {
        this.elementKey = elementKey;
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
        this.attributesOrKeyValuePairs.put(key, value);
    }

    public void addChild(Node node){
        this.children.add(node);
    }
}
