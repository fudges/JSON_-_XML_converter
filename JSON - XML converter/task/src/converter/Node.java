package converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Node
{
    public String path;
    public String elementKey;
    public String elementValue;
    public LinkedHashMap<String, String> attributesOrKeyValuePairs;
    public ArrayList<Node> children;

    public ArrayList<Node> getChildren() {
        return this.children;
    }

    public LinkedHashMap<String, String> getAttributesOrKeyValuePairs() {
        return this.attributesOrKeyValuePairs;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getElementKey() {
        return this.elementKey;
    }

    public void setElementKey(final String elementKey) {
        this.elementKey = elementKey;
    }

    public String getElementValue() {
        return this.elementValue;
    }

    public void setElementValue(final String elementValue) {
        this.elementValue = elementValue;
    }

    public Node() {
        this.path = "";
        this.elementKey = "";
        this.elementValue = "";
        this.attributesOrKeyValuePairs = new LinkedHashMap<String, String>();
        this.children = new ArrayList<Node>();
    }

    public void addAddtribute(final String key, final String value) {
        this.attributesOrKeyValuePairs.put(key, value);
    }

    public void addChild(final Node node) {
        this.children.add(node);
    }
}