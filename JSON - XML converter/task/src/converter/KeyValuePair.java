package converter;

import java.util.ArrayList;

public class KeyValuePair
{
    private String key;
    private String value;
    private ArrayList<KeyValuePair> valueArray;
    private Node valueNode;
    private boolean invalid;

    public boolean isInvalid() {
        return this.invalid;
    }

    public void setInvalid(final boolean invalid) {
        this.invalid = invalid;
    }

    public void addToValueArray(final KeyValuePair keyValuePair) {
        this.valueArray.add(keyValuePair);
    }

    public ArrayList<KeyValuePair> getValueArray() {
        return this.valueArray;
    }

    public void setValueArray(final ArrayList<KeyValuePair> valueArray) {
        this.valueArray = valueArray;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public Node getValueNode() {
        return this.valueNode;
    }

    public void setValueNode(final Node valueNode) {
        this.valueNode = valueNode;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public KeyValuePair() {
        this.key = "";
        this.value = "";
        this.valueArray = new ArrayList<KeyValuePair>();
        this.valueNode = new Node();
        this.invalid = false;
    }
}
