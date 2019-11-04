package converter;

import java.util.ArrayList;
import java.util.HashMap;

public class Element {
    private String key = "";
    private String value = "";
    private HashMap<String, String> attributes = new HashMap<>();
    private ArrayList<Element> childElements = new ArrayList<>();

    public ArrayList<Element> getChildElements() {
        return childElements;
    }

    public void setChildElements(ArrayList<Element> childElements) {
        this.childElements = childElements;
    }

    public Element() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }
}
