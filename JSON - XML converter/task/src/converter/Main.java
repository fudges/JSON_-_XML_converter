package converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/*
 * Exception performing whole class analysis ignored.
 */
public class Main {
    public static void main(String[] args) {
//        File file = new File("C:\\Users\\Michael\\Desktop\\JavaProjects\\JSON - XML converter1\\JSON - XML converter\\task\\src" +
//                "\\test11.txt");
        File file = new File("C:\\Users\\mcarner\\Documents\\GitHub\\JSON_-_XML_converter\\JSON - XML converter\\task\\src" +
                "\\test11.txt");

//        File file = new File("test.txt");
        String input = "";
        try {
            try (Scanner scanner = new Scanner(file);){
                while (scanner.hasNext()) {
                    input = input + scanner.nextLine();
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //Clean up extra spaces
        input = (input).replaceAll("\\s+", " ");

//        String tempString = detectJsonElements(input);
        //Testing array brackets
//        KeyValuePair temp = parseJsonKeyValuePair(input,false);
        //Check if Json or Xml
        boolean pause = true;
        String format = isJsonOrXml(input);
        String intermediaryFormat;
        String output = "";

        if (format.equalsIgnoreCase("XML")){
            //Convert XML -> JSON
//            intermediaryFormat = convertJSONToIntermediaryFormat(parseJson(input), 0, "");
            //Remove any headers that may exist
            input = input.replaceAll("<\\?[^\\?]*\\?>","");

            Node tempNode = processXml(input);
            output = printNodeAsJson(tempNode,0,false);
//            output = convertXMLToIntermediaryFormat(tempNode,0,"");
//            System.out.println(output);
//            output = printAsJSON(output);
            System.out.println(output);
        } else if (format.equalsIgnoreCase("JSON")){
            //Convert JSON -> XML
            String startingPath = "";
            Pattern rootDetectingPattern = Pattern.compile("^\\s*\\{.*}\\s*$");
            Matcher rootDetectingMatcher = rootDetectingPattern.matcher(input);
            if (rootDetectingMatcher.find()){
                startingPath = "root";
            }
            KeyValuePair tempKV = parseJson(input);
//            String tempOutput = convertJSONToIntermediaryFormat(tempKV, 0, startingPath);
            String tempOutput2 = printKeyValuePairAsXML(tempKV,0);
//            KeyValuePair tempKV = parseJson(input);
            System.out.println(tempOutput2);
//            output = printAsXML(tempOutput);
//            System.out.println(output);

        } else {
            //Could not detect, handle error. EXCEPTION??
            System.out.println("Could not detect format, exiting");
        }
    }

    public static boolean fullOfNullsCheck(KeyValuePair nullCheckKVP){
        boolean fullOfNulls = true;
        for (KeyValuePair valueArrayKVP :
                nullCheckKVP.getValueArray()) {
            if (!valueArrayKVP.getValue().equalsIgnoreCase("null")){
                fullOfNulls = false;
            }
            if (valueArrayKVP.getValueArray().size() > 0){
                if(!fullOfNullsCheck(nullCheckKVP)){
                    fullOfNulls = false;
                }
            }
        }
        return fullOfNulls;
    }

    public static String printKeyValuePairAsXML(KeyValuePair inputKeyValuePair, int depth){
        String output = "";


        String key = inputKeyValuePair.getKey();
        String value = inputKeyValuePair.getValue();
        boolean loneTag = false;
        boolean attributesExist = false;


        //Check for attributes and values in valueArray
        ArrayList<KeyValuePair> attribArray = new ArrayList<>();
        if (inputKeyValuePair.getValueArray().size() > 0){

            for (KeyValuePair arrayKeyValuePair :
                    inputKeyValuePair.getValueArray()) {
                String tempKey = arrayKeyValuePair.getKey();
                String tempValue = arrayKeyValuePair.getValue();


                if (tempKey.contains("@")){
                    attribArray.add(arrayKeyValuePair);
                    attributesExist = true;
                } else if (tempKey.contains("#") && arrayKeyValuePair.getValueArray().size() == 0){
                    //It's a value with nothing in the ValueArray
                    value = tempValue;
                } else if (tempKey.contains("#") && arrayKeyValuePair.getValueArray().size() > 0){
                    //Catches when a value has content stored in the ValueArray such as:
                    // "#element": {
                    //                    "deep": {
                    //                        "@deepattr": "deepvalue",
                    //                        "#deep": [
                    //                            1, 2, 3
                    //                        ]
                    //                    }
                    //                }
                    for (KeyValuePair valueArrayKeyValuePair :
                            arrayKeyValuePair.getValueArray()) {
                        depth++;
                        value += printKeyValuePairAsXML(valueArrayKeyValuePair,depth);
                        depth--;
                        boolean pause = true;
                    }
                    value += "\n" + getIndents(depth);

                    //NEED ADDITIONAL TESTS. it's now printing the #value element with its own tags
                }

            }
        }

        //Determine if sole tag
//        if (value.equalsIgnoreCase("null") && inputKeyValuePair.getValueArray().size() <= 1){
        if (value.equalsIgnoreCase("null")){
            loneTag = true;
        }
        //Additional loneTag check for valueArrays > 1 but contain only nulls
//        if (fullOfNullsCheck(inputKeyValuePair)){
//            loneTag = true;
//        }
        //********************
        //***Start printing***
        //********************
        //Add first part of the opening tag

        output += "\n" + getIndents(depth) + "<" + key;

        //Assemble attributes string
        String attributeString = "";

        if (attributesExist) {
            attributeString += " ";
            int attributesCounter = 0;
            for (KeyValuePair attributeKeyValuePair :
                    attribArray) {
                attributesCounter++;
                String attributeValue = "\"" + attributeKeyValuePair.getValue() + "\"";
                attributeValue = attributeValue.replaceAll("^\"\"(?!$)|(?!^)\"\"$","\"");
                attributeValue = attributeValue.replaceAll("^\"?null\"?$","\"\"");

                attributeString += attributeKeyValuePair.getKey().replaceAll("@","") + "=" + attributeValue;

                if(attributesCounter < attribArray.size()){
                    attributeString += " ";
                }
            }
        }

        //Add attributes string
        output += attributeString;

        //If loneTag, add closing portion and RETURN OUTPUT
        if (loneTag){
            output += " />";
            return output;
        }

        //If there's a value, close the first tag, add value and then close second tag and RETURN OUTPUT
        if (!value.equalsIgnoreCase("null") && inputKeyValuePair.getValueArray().size() == 0
            && !value.matches("<|>")){
            //Remove the quotations
            value = value.replaceAll("\"","");
            output += ">" + value + "</" + key + ">";
            return output;
        }

        //If it has child KeyValuePairs, process those recursively
        if (inputKeyValuePair.getValueArray().size() > 0) {
            output += ">";

            for (KeyValuePair valueArrayKeyValuePair :
                    inputKeyValuePair.getValueArray()) {
                String tempKey = valueArrayKeyValuePair.getKey();
                //If key does not indicate it is:
                //  an attribute
                //  a parent element value
                //  an invalid element (these are skipped)
                if (!tempKey.matches("@.*|#.*|&&&INVALID&&&")){
                    depth++;
                    output += printKeyValuePairAsXML(valueArrayKeyValuePair,depth);
                    depth--;

                }
            }
            //THIS NEEDS FIXING
            //Add closing tag
            if (!value.equalsIgnoreCase("")){
                if (!value.contains("<")){
                    value = value.replaceAll("\"","");
                }
                output += value + "</" + key + ">";
            } else {
                output += "\n" + getIndents(depth) + "</" + key + ">";
            }

            //output += "\n" + getIndents(depth) + "</" + key + ">";
        }

        //Final test to remove root tags if needed
        if (depth == 0 && key.equalsIgnoreCase("root") && inputKeyValuePair.getValueArray().size() <= 1) {
            output = output.replaceAll("\n?</?root>\n?","");
            //Remove 1 of each tab
            String tabReduceRegex = "";
            String tabReplaceString = "";

            tabReduceRegex = "([^\t\\n]?)(\t)(?=[^\t])";
            output = output.replaceAll(tabReduceRegex,"");
        }


        return output;
    }

    public static String printNodeAsJson(Node inputNode, int depth, boolean isArrayElement){

        String output = "";
        //Opening curly brackets
        if (depth == 0) {
            output += "{";
        }


//        if (depth == 0) {
            depth++;
            output += "\n" + getIndents(depth);
//        }

        String key = inputNode.getElementKey();
        String value = inputNode.getElementValue();

        if(!isArrayElement){
            //Print key
            output += "\"" + key + "\" : ";

            //If there is a value, there will be no children
            if(!value.equalsIgnoreCase("")){
                if (value.equalsIgnoreCase("null")){
                    output += value;
                } else {
                    output += "\"" + value + "\"";
                }
                return output;
            }
        }
        //If there are children, print them.
        if(value.equalsIgnoreCase("") && inputNode.getChildren().size() > 0){

            //Test if it's an array
            String prevKey = "";
            int[] isPartOfAnArray = new int[inputNode.getChildren().size()];
            int counter = 0;

            for (Node childNode :
                    inputNode.getChildren()) {

                if (childNode.getElementKey().equalsIgnoreCase(prevKey)){
                    isPartOfAnArray[counter-1] = 1;
                    isPartOfAnArray[counter] = 1;
                }


                prevKey = childNode.getElementKey();
                counter++;

            }

            boolean pause = true;

            //Test if it's an array and print bracket if needed

            //Test if they are all part of array
            int sum = IntStream.of(isPartOfAnArray).sum();
            int childIndexCounter = 0;

            if (isPartOfAnArray.length == sum){
                //All children are part of array
//                depth++;
                output += "[";//\n" + getIndents(depth);


                for (Node childNode :
                        inputNode.getChildren()) {
                    childIndexCounter++;
                    output += printNodeAsJson(childNode, depth, true);

                    if(childIndexCounter < inputNode.getChildren().size()){
                        output += ",";
                    }
                }

                //Closing array brackets
//                depth--;
                output += "\n" + getIndents(depth) + "]";
                depth--;

            } else if (sum == 0) {
                //None are part of array

                output += "{";
//                output += "{\n" + getIndents(depth);

                for (Node childNode :
                        inputNode.getChildren()) {
                    childIndexCounter++;
                    output += printNodeAsJson(childNode, depth, false);

                    if(childIndexCounter < inputNode.getChildren().size()){
                        output += ",";
                    }

                }
            }
             pause = true;

        }


        //Closing curly brackets
        output += "\n" + getIndents(depth) + "}";
        return output;
    }


    //TODO:
    //Looks like the json printing part is working
    //Test it and make sure!!
    //



    //<editor-fold desc="Json to XML portion">
    public static String isJsonOrXml(String input) {
        String[] inputArray = (input).split("");
        String inputType = "";
        if (inputArray[0].equalsIgnoreCase("<")) {
            inputType = "XML";
        } else if (inputArray[0].equalsIgnoreCase("{")) {
            inputType = "JSON";
        } else {
            inputType = "ERROR: Could not determine type";
        }
        return inputType;
    }

    public static KeyValuePair parseJson(String input) {

        Pattern rootPattern = Pattern.compile("^\\{.*}$");
        Matcher rootMatcher = rootPattern.matcher(input);
        if (rootMatcher.find()){
            KeyValuePair rootPair = new KeyValuePair();

            rootPair.setKey("root");
            for (KeyValuePair keyValuePair :
                    parseJsonBrackets(input).getValueArray()) {
                rootPair.addToValueArray(keyValuePair);
            }
            return rootPair;
        } else {
            return parseJsonBrackets(input);
        }

    }

    public static String detectJsonElements(String input) {
        //This finds the very first keyValuePair and sends the rest back as leftovers



        String keyValueString = "";
        String leftovers = "";
        String returnString = "";
        int bracketCounter = 0;
        int arrayBracketCounter = 0;
        boolean colonFound = false;
        boolean bracketFound = false;
        boolean arrayBracketFound = false;
        boolean nextFound = false;
        Character[] inputCharArray = (Character[])input.chars().mapToObj(c -> Character.valueOf((char)c)).toArray(x$0 -> new Character[x$0]);
        boolean endFound = false;
        for (Character inputChar : inputCharArray) {
            if (!endFound) {
                if (inputChar.toString().equalsIgnoreCase("{")) {
                    bracketFound = true;
                    ++bracketCounter;
                } else if (inputChar.toString().equalsIgnoreCase("}")) {
                    --bracketCounter;
                }
                if (inputChar.toString().equalsIgnoreCase("[")) {
                    arrayBracketFound = true;
                    ++arrayBracketCounter;
                } else if (inputChar.toString().equalsIgnoreCase("]")) {
                    --arrayBracketCounter;
                }
                if (inputChar.toString().equalsIgnoreCase(":")) {
                    colonFound = true;
                }
                keyValueString = keyValueString + inputChar.toString();
                //Split up keyValuePairs here
                //If the end of the keyValuePair is found, flip endFound to true and spit the rest out to leftovers
                if (!arrayBracketFound && bracketCounter == 0 && inputChar.toString().equalsIgnoreCase(",")){
                    endFound = true;
                }
                if (arrayBracketFound && arrayBracketCounter == 0 && bracketCounter == 0 && inputChar.toString().equalsIgnoreCase(",")) {
                    endFound = true;
                }
//                if (bracketCounter != 0 || !inputChar.toString().equalsIgnoreCase(",")) continue;
//                endFound = true;
//                continue;
            }else {
                leftovers = leftovers + inputChar.toString();
            }

        }
        //colonFound was interfering with parsing arrayBrackets. Is it even necessary?
//        if (!colonFound || (leftovers).length() == 0) {
        if (leftovers.length() == 0) {
            leftovers = ">>>END<<<";
        }
        returnString = keyValueString + "~~split~~" + leftovers;
        return returnString;
    }


    public static KeyValuePair parseJsonKeyValuePair(String input, boolean invalid) {
        Matcher invalidKeyMatcher;
        Pattern invalidKeyPattern;
        KeyValuePair keyValuePair = new KeyValuePair();
        if (invalid) {
            keyValuePair.setInvalid(true);
        }





        //Detect if KeyValuePair or arrayBracket element
        Pattern arrayBracketValueTestPattern = Pattern.compile("^([^:,]*),?$");
        Matcher arrayBracketValueTestMatcher = arrayBracketValueTestPattern.matcher(input);
        //Blank brackets are handled further down
        //But also handling it here to prevent reworking a bunch of code
        boolean elementIsEmpty = input.matches("(^\\[\\s*]|^\\{\\s*}),");
        if (arrayBracketValueTestMatcher.find()) {
            if (elementIsEmpty) {
                keyValuePair.setKey("element");
                keyValuePair.setValue("");
                return keyValuePair;
            }
            //Is just a single value
            //Set KEY to "element" and set VALUE to the found value
            keyValuePair.setKey("element");
            keyValuePair.setValue(arrayBracketValueTestMatcher.group(1));
            return keyValuePair;
        }

        boolean noNameElement = false;
        Pattern keyPattern = Pattern.compile("^\\s*\"([^\"]*)\"\\s*:\\s*\\{?");
        Matcher keyMatcher = keyPattern.matcher(input);
        String key = "";
        if (keyMatcher.find()) {
            key = keyMatcher.group(1);
        } else {
            key = "element";
            //Set noNameElement to true, so you can parse it in case it has element attributes/values
            noNameElement = true;
        }
        if ((invalidKeyMatcher = (invalidKeyPattern = Pattern.compile("^@$|^#$")).matcher(key)).find() || key.length() == 0) {
            keyValuePair.setKey("&&&INVALID&&&");
            return keyValuePair;
        }
        if (invalid) {
            key = key.replaceFirst("^#", "");
        }
        keyValuePair.setKey(key);

        //Detect and parse arrayBrackets
//        Pattern arrayBracketPattern = Pattern.compile("(\\{.*})");
//        Matcher arrayBracketMatcher = arrayBracketPattern.matcher(input);
//        if (arrayBracketMatcher.find()) {
//            KeyValuePair temp = new KeyValuePair();
//        }


        String value = "";
        //Detect section of either curly or array brackets
        Pattern valueBracketPattern = Pattern.compile("((\\[|\\{).*(]|}))");
        Matcher valueBracketMatcher = valueBracketPattern.matcher(input);
        Pattern valuePattern = Pattern.compile("\"[^\"]*\"\\s*:\\s*([^{,]*)");
        Matcher valueMatcher = valuePattern.matcher(input);
        if (valueBracketMatcher.find()) {
            //Parse the values found inside the bracket
            //Returns an ArrayList of KeyValuePairs containing the elements inside
            ArrayList<KeyValuePair> tempArray = parseJsonBrackets(valueBracketMatcher.group(1)).getValueArray();
            //Only add as blank "element" if it's array.
            //If there's just one, set value to ""
            boolean containsValueElement = false;
            try {
                if (tempArray.get(0).getKey().equalsIgnoreCase("#element")){
                    containsValueElement = true;
                } else if (tempArray.size() == 1 && (tempArray.get(0).getValueArray().size() > 0 || !tempArray.get(0).getValue().equalsIgnoreCase(""))){
                    containsValueElement = true;
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }
            //TODO:
            //inner2 is discarding it here
            //WHY?
            //Can I change the else statement??
            if (tempArray.size() > 1 || key.matches("^#.*$") || containsValueElement){
                for (Object parsedKeyValuePairs : tempArray) {
                    keyValuePair.addToValueArray((KeyValuePair)parsedKeyValuePairs);
                }
            } else {
                keyValuePair.setValue("");
            }
            boolean pause = true;
            boolean invalidFlag = false;
            boolean attrCheck = false;
            boolean valueCheck = false;
            int attribCount = 0;
            int elementValueCount = 0;
            for (int j = 0; j < keyValuePair.getValueArray().size(); ++j) {
                KeyValuePair tempChild = keyValuePair.getValueArray().get(j);
                String tempChildKey = tempChild.getKey();
                String tempParentKey = keyValuePair.getKey();
                if (tempChildKey.contains("@invalid_attr")) {
                    pause = true;
                }
                if (tempChildKey.contains("@")) {
                    ++attribCount;
                    attrCheck = true;
                    if (tempChild.getValueArray().size() > 0) {
                        invalidFlag = true;
                    }
                }
                if (!tempChildKey.contains("#")) continue;
                ++elementValueCount;
                valueCheck = true;
                if ((tempChildKey = tempChildKey.replaceFirst("#", "")).equalsIgnoreCase(tempParentKey)) continue;
                invalidFlag = true;
            }
            if (!(!attrCheck || attrCheck && valueCheck)) {
                invalidFlag = true;
            }
            if (attribCount > 0 && elementValueCount > 0 && keyValuePair.getValueArray().size() > attribCount + elementValueCount) {
                invalidFlag = true;
            }
            ArrayList<Integer> discardIndexArray = new ArrayList<Integer>();
            if (invalidFlag) {
                for (int j = 0; j < keyValuePair.getValueArray().size(); ++j) {
                    String tempKey = (keyValuePair.getValueArray().get(j)).getKey();
                    if (tempKey.contains("@") || tempKey.contains("#")) {
                        tempKey = tempKey.replaceAll("^#|^@", "");
                        boolean discard = false;
                        for (int i = 0; i < keyValuePair.getValueArray().size(); ++i) {
                            String keyToCompare = (keyValuePair.getValueArray().get(i)).getKey();
                            if (!keyToCompare.equals(tempKey)) continue;
                            discardIndexArray.add(j);
                        }
                    }
                    if (discardIndexArray.contains(j)) continue;
                    (keyValuePair.getValueArray().get(j)).setKey(tempKey);
                }
                for (int i = keyValuePair.getValueArray().size() - 1; i >= 0; --i) {
                    if (!discardIndexArray.contains(i)) continue;
                    keyValuePair.getValueArray().remove(i);
                }
            }
        } else if (valueMatcher.find()) {
            value = valueMatcher.group(1);
            if (!(value).contains("null") && !(value).trim().matches("^\".*\"$")) {
                value = "\"" + value + "\"";
            }
            keyValuePair.setValue(value);
        }
        return keyValuePair;
    }



    public static KeyValuePair parseJsonBrackets(String input) {
        KeyValuePair keyValuePair = new KeyValuePair();

        //Parse root brackets here?
        String keyValue = "";
        String inputDebug = input;
        Pattern emptyBracketsPattern = Pattern.compile("^\\{\\s*}$");
        Matcher emptyBracketsMatcher = emptyBracketsPattern.matcher(input);
        if (emptyBracketsMatcher.find()) {
            keyValuePair.setValue("");
            return keyValuePair;
        }

        //Strip off surrounding brackets
        input = input.strip().replaceAll("^\\{|^\\[", "").replaceAll("}$|]$", "");
        input = input.replaceAll("\\s+", " ");

        ArrayList<String> keyValueArray = new ArrayList<String>();
        String stop = "";
        String firstDetectedKeyValuePairString = "";
        do {
            firstDetectedKeyValuePairString = detectJsonElements(input);
            String[] firstDetectedKeyValuePairStringSplit = firstDetectedKeyValuePairString.split("~~split~~");
            keyValueArray.add(firstDetectedKeyValuePairStringSplit[0]);
            if (firstDetectedKeyValuePairStringSplit.length <= 1) continue;
            input = firstDetectedKeyValuePairStringSplit[1];
        } while (!firstDetectedKeyValuePairString.contains(">>>END<<<"));
        boolean invalidFlag = false;
        Iterator i = keyValueArray.iterator();
        while (i.hasNext()) {
            String kvString = ((String)i.next()).trim();
            KeyValuePair keyValuePairOutput = new KeyValuePair();
            if (!invalidFlag) {
                keyValuePairOutput = parseJsonKeyValuePair(kvString, false);
                if (keyValuePairOutput.getKey().equalsIgnoreCase("&&&INVALID&&&")) {
                    invalidFlag = true;
                }
            } else if (invalidFlag) {
                keyValuePairOutput = parseJsonKeyValuePair(kvString, true);
            }
            if (keyValuePairOutput.getKey().equalsIgnoreCase("&&&INVALID&&&")) continue;
            keyValuePair.addToValueArray(keyValuePairOutput);
        }
        if (invalidFlag) {
            for (int j = 0; j < keyValuePair.getValueArray().size(); ++j) {
                String tempKey = (keyValuePair.getValueArray().get(j)).getKey();
                tempKey = tempKey.replaceAll("^#|^@", "");
                (keyValuePair.getValueArray().get(j)).setKey(tempKey);
            }
        }
        return keyValuePair;
    }





    public static String getIndents(int indentLevel){
        String indents = "";
        for (int i = 0; i < indentLevel; i++) {
            indents += "\t";
        }
        return indents;
    }
    //</editor-fold>


    public static KeyValuePair xmlGetNextChild(String input){
        //This method iterates through the String input, remembers the first tag and looks for it's closing tag
        //It uses counters so it doesn't accidentally grab a closing tag of the same name

        String output = "";
        String leftovers = "";
        KeyValuePair outputValues = new KeyValuePair();

        int tagCounter = 0;
//        int closeTagCounter = 0;
        String currentTag = "";
        boolean done = false;
        for (String chunk :
                input.split("(?=<)|(?<=>)")) {
            //Catch lone tags
            if (chunk.matches("^<[^>/]*/>$") && currentTag.equalsIgnoreCase("")){
                output += chunk;
                done = true;
                continue;
            }

            //Grab first tag, increment counter, print to output and CONTINUE
            if (currentTag.equalsIgnoreCase("")){
                currentTag = chunk.replaceAll("<|\\s[^>]*>|>","");

                tagCounter++;
                output += chunk;
                continue;
            }
            //Counts opening tags that have same name
            String openingRegex = "^<" + currentTag + "[^>]*>$";
            String closingRegex = "^</" + currentTag + "[^>]*>$";
            if (chunk.matches(openingRegex)){
                tagCounter++;
            }
            //Counts closing tags that have same name
            if (chunk.matches(closingRegex)){
                tagCounter--;
            }

            if (!done){
                output += chunk;
            } else {
                leftovers += chunk;
            }

            if (tagCounter == 0 && chunk.equalsIgnoreCase("</" + currentTag + ">")){
                done = true;

            }

        }

        outputValues.setKey(output);
        outputValues.setValue(leftovers);
        return outputValues;
    }

    public static Node processXml(String input) {
        String output = "";
        Node xmlNode = new Node();
        Pattern pTag = Pattern.compile("(<([^/]*?)/?>)");
        Matcher mTag = pTag.matcher(input);
        String elementValue = "";
        boolean loneTag = false;
        if (mTag.find()) {
            Matcher elementValueMatcher;
            Pattern elementValuePattern;
            String fullTag = mTag.group(2);
            String totalTag = mTag.group(1);
            String element = fullTag.split(" ")[0];
            output = output + "{ \"" + element + "\" : ";
            Pattern pLoneTag = Pattern.compile("<.+?/>");
            Matcher mLoneTag = pLoneTag.matcher(totalTag);
            if (mLoneTag.find()) {
                loneTag = true;
                elementValue = "null";
            }
            if (!loneTag && (elementValueMatcher = (elementValuePattern = Pattern.compile(element + "[^>]*>\\s*(.*?)\\s*<")).matcher(input)).find()) {
                elementValue = elementValueMatcher.group(1);
            }
            xmlNode.setElementKey(element);
            xmlNode.setElementValue(elementValue);
            String attributeCheck = fullTag.replaceFirst(element + "\\s*", "").stripTrailing();
            if (attributeCheck.length() > 0) {
                output = output + "{ ";
//                Pattern pattern = Pattern.compile("(\\w+).*?\"(\\w+)\""); ORIGINAL
//                Pattern pattern = Pattern.compile("(\\w+).*?(?:\"|')(\\w+)(?:\"|')"); ORIGINAL FIXED
                //Revert to commented out section if this new regex breaks stuff
                Pattern pattern = Pattern.compile("(\\w+)\\s?=\\s?(?:\"|')(\\w+)(?:\"|')");
                Matcher matcher = pattern.matcher(attributeCheck);
                int lastFindPos = 0;
                boolean attributesFound = false;
                while (matcher.find()) {
                    attributesFound = true;
                    Node tempNode = new Node();
                    String attributeKey = "@" + matcher.group(1);
                    String attributeValue = matcher.group(2);
                    tempNode.setElementKey(attributeKey);
                    tempNode.setElementValue(attributeValue);
                    xmlNode.addChild(tempNode);
//                    xmlNode.addAddtribute(attributeKey, attributeValue);
                    output = output + "\"@" + matcher.group(1) + "\" : \"" + matcher.group(2) + "\"";
                    lastFindPos = matcher.end();
                    if (lastFindPos == attributeCheck.length() && elementValue == "" && !loneTag) continue;
                    output = output + ", ";
                }
                //Add value as child element if attributes exist
                if (attributesFound) {
                    Node tempNode = new Node();
                    String valueKey = "#" + element;

                    tempNode.setElementKey(valueKey);
                    tempNode.setElementValue(elementValue);

                    xmlNode.addChild(tempNode);
                    xmlNode.setElementValue("");
                }
                if (elementValue != "" && !loneTag) {
                    output = output + "\"#" + element + "\" : \"" + elementValue + "\"";
                } else if (loneTag) {
                    output = output + "\"#" + element + "\" : " + elementValue;
                }
                output = output + "} ";
                output = output + "} ";
            }
            if (!loneTag) {
                String recursiveInput = input;
                String tempRegex = "^<" + element + "[^>]*>(?:(?!<).)*" + "|" + "</" + element + "[^>]?>(?:(?!<).)*$";
                recursiveInput = recursiveInput.replaceAll(tempRegex, "").trim();
                KeyValuePair childElementKVP = new KeyValuePair();

                while (recursiveInput.length() > 0) {
                    Matcher newLoneTagCheckMatcher;
                    Pattern newLoneTagCheckPattern;
                    Pattern firstTagPattern = Pattern.compile("<(.*?)/?>");
                    Matcher firstTagMatcher = firstTagPattern.matcher(recursiveInput);
                    String firstTag = "";
                    if (firstTagMatcher.find()) {
                        firstTag = firstTagMatcher.group(1).split(" ")[0];
                    }
                    loneTag = (newLoneTagCheckMatcher = (newLoneTagCheckPattern = Pattern.compile(" ?^<" + firstTag + "[^>]*/>")).matcher(recursiveInput)).find();
                    String getNextChildRegex = "";
                    //Get the next child here
                    childElementKVP = xmlGetNextChild(recursiveInput);
                    String child = childElementKVP.getKey().trim();
                    String leftovers = childElementKVP.getValue().trim();

//                    getNextChildRegex = !loneTag ? getNextChildRegex + "<" + firstTag + ".*?</" + firstTag + ">" : getNextChildRegex + "<" + firstTag + ".*?/>";
//                    Pattern childPattern = Pattern.compile(getNextChildRegex);
//                    Matcher childMatcher = childPattern.matcher(recursiveInput);
//                    if (!childMatcher.find()) continue;
                    xmlNode.addChild(processXml(child));
                    recursiveInput = leftovers;
                    if (recursiveInput.matches("\\s*")){
                        recursiveInput = "";
                    }

//                    recursiveInput = recursiveInput.replaceAll(child + "\\s*", "");
                }
            } else {
                return xmlNode;
            }
            return xmlNode;
        }
        System.out.println("ERROR: No tag found");
        System.exit(1);
        return xmlNode;
    }

    //<editor-fold desc="Unused">
    public static String printAsJSON(String input){

        String output = "";
        //Convert into single line, remove all extra spaces tabs and whatnot
        input = input.replaceAll("\n\n","\n");


        //Patterns
        Pattern elementPattern = Pattern.compile("Element:");
        Pattern pathPattern = Pattern.compile("path = (.*)");
        Pattern valuePattern = Pattern.compile("value = (\"?[^\"]*\"?)");
        Pattern attributesPattern = Pattern.compile("attributes:");
        Pattern attributeParsePattern = Pattern.compile("([^\\s]*)\\s=\\s(.*)");
        Pattern childrenPattern = Pattern.compile("children = (.*)");

        //Flags
        boolean elementStart = false;
        boolean valueFound = false;
        boolean attributesFound = false;
        boolean isChildElement = false;
        boolean finishElement = false;
        boolean firstElementFound = false;
        boolean surroundingBracketsPrinted = false;
        boolean closeBrackets = false;
//        boolean closeParentTag = false;
        boolean childElementEnd = false;
        boolean childStartFound = false;
        boolean childrenFound = false;


        //Counters
        int bracketCounter = 0;
        int indentLevel = 0;
        int index = 0;


        //Recurring variables
        String prevPath = "";
        String curPath = "";
        String curElementKey = "";
        String prevParentKey = "";
        String curValue = "";
        String attribKey = "";
        String attribValue = "";
        String curParentPath = "path = ";
        String prevFullPath = "";
        String curFullPath = "";
        String prevKey = "";


        //Lists
        ArrayList<String> parentTags = new ArrayList<>();
        ArrayList<Path> pathList = new ArrayList<>();
        ArrayList<KeyValuePair> tempAttributeArray = new ArrayList<>();


        //Iterate through each line of input
        for (String line :
                input.split("\n")) {
            Matcher lineElement = elementPattern.matcher(line);
            Matcher linePath = pathPattern.matcher(line);
            Matcher lineValue = valuePattern.matcher(line);
            Matcher lineAttributes = attributesPattern.matcher(line);
            Matcher lineAttributesParse = attributeParsePattern.matcher(line);
            Matcher childrenMatcher = childrenPattern.matcher(line);

            boolean pause = true;
            boolean elementHeaderFound = false;



            //Print Surrounding brackets
            if (!surroundingBracketsPrinted){
                surroundingBracketsPrinted = true;
                indentLevel++;
                bracketCounter++;
                output += "{\n";
            }



            //If "Element:" found, process previous element
            if (lineElement.find() && !prevPath.equalsIgnoreCase("")) {
                //If it's at the same level of the previous element, print comma
                //If not, print closing brackets

                output += getIndents(indentLevel) + "\"" + curElementKey + "\"" + ": ";
                if (!curValue.equalsIgnoreCase("") && tempAttributeArray.size() == 0){

                    output += curValue;
                    if (isChildElement){
                        output += ",\n";
                    }


                } else if (tempAttributeArray.size() > 0){
                    output += "{\n";
                    //Increment indent level before printing attributes/values
                    indentLevel++;
                    bracketCounter++;

                    int tempCounter = 0;
                    //Iterate through tempKeyValueArray, output attributes
                    for (KeyValuePair tempAttribKeyValuePair :
                            tempAttributeArray) {
                        String tempAttribKey = tempAttribKeyValuePair.getKey();
                        String tempAttribValue = tempAttribKeyValuePair.getValue();
                        output += getIndents(indentLevel) + "\"@" + tempAttribKey + "\" : " + tempAttribValue;
                        //Do I need quotations here? I honestly have no clue.

                        if(tempCounter != tempAttributeArray.size()-1 || !curValue.equalsIgnoreCase("")){
                            output += ",";
                        }

                        //Add final line break
                        output += "\n";
                        tempCounter++;
                    }
                    if(tempCounter != tempAttributeArray.size()-1 || !curValue.equalsIgnoreCase("")){
                        output = output.replaceFirst("\n?$",",\n");
//                        output += ",";
                    }
                    //Turn off attributesFound here so that it works right for the next element
                    attributesFound = false;
                    //Clear tempAttributeArray
                    tempAttributeArray.clear();

                    //Add value if it exists
                    if (!curValue.equalsIgnoreCase("")){
                        output += getIndents(indentLevel) + "\"#" + curElementKey + "\": " + curValue + "\n";
                    }


                    //Print value for children elements
                    if (childrenFound){
                        output += getIndents(indentLevel) + "\"#" + curElementKey + "\": ";
//                        indentLevel++;
                    } else {
                        //Close up the brackets at the end, decrease counters
                        //Add flag or counter for closing brackets for closing a brackets
                        indentLevel--;
                        output += getIndents(indentLevel) + "}";
                    }





                }
                //Set curValue to blank, so it isn't carried on to another element on accident
                curValue = "";

                //Reset childrenFound to false;
                childrenFound = false;

            }




            //Start processing things on "path = ..."
            if (linePath.find()){

//                curFullPath = line;
                curPath = linePath.group(1);

                //Get current elementName / key
                String[] pathArray = curPath.replaceAll("\\s","").split(",");
                curElementKey = pathArray[pathArray.length-1];


                if (!prevPath.equalsIgnoreCase("")){
                    //Check if this element is a child of previous element
                    //if curpath still contains previous elementKey, this new elementKey is a child
                    Pattern childPattern = Pattern.compile(prevParentKey + ", " + curElementKey);
                    Matcher childMatcher = childPattern.matcher(curPath);
                    boolean childMatcherFound = childMatcher.find();
                    if (childMatcherFound && !childStartFound){
                        //If it is a child and you haven't started printing child elements yet, mark the start here
                        isChildElement = true;
                        indentLevel++;
                        childStartFound = true;
                        output += "{\n";
                    } else if (childMatcherFound && childStartFound){
                        //If it's a child but you've already started printing child elements, mark isChildElement, but don't increase indent
                        isChildElement = true;
                        //If it doesn't have a comma on the end, add one here
                        String tempString = output.replaceAll("\\n","").replaceAll("\\t","");
//                        if (!tempString.matches("^.*,$")){
                        if (!tempString.matches("^(.|\\n|\\t)*,\\n*$")){
                            output += ",\n";
                        }

                    } else if (!childMatcherFound && childStartFound){
                        //First, check if it's moved on to yet another child element
                        Pattern subsequentChildPattern = Pattern.compile(prevKey + ", " + curElementKey);
                        Matcher subsequentChildMatcher = subsequentChildPattern.matcher(curPath);
                        if (subsequentChildMatcher.find()){
                            //A new child has started
                            pause = true;
                            isChildElement = true;
                            indentLevel++;
                            childStartFound = true;
                            output += "{\n";
                            prevParentKey = prevKey;
                        } else {
                            //If it's not a child element, but you have previously been printing child elements
                            //Need this so you can close up the brackets and start it as a new element
                            childElementEnd = true;
                            childStartFound = false;
                            isChildElement = false;
                        }

                    } else {
                        isChildElement = false;
                        prevParentKey = curElementKey;
                    }
                } else {
                    //Grab initial key and store it here
                    prevParentKey = curElementKey;
                }

                //Save previous key for later comparison to next element
                prevKey = curElementKey;
                //Save curPath as prevPath for comparison to next element.
                prevPath = curPath;
            }

            if (lineValue.find()){
                valueFound = true;
                curValue = lineValue.group(1);
            }
            if (lineAttributes.find()){
                //Check for attributesOrKeyValuePairs header, get ready to parse attributesOrKeyValuePairs
                attributesFound = true;
            }

            //Keep track if there's children, so you can print everything correctly on the next "Element:"
            if (childrenMatcher.find()){
                childrenFound = true;
            }

            if (attributesFound){
                //Attributes found, parse as they come.
                if (lineAttributesParse.find() && !childrenFound){
                    KeyValuePair tempKeyValuePair = new KeyValuePair();
                    tempKeyValuePair.setKey(lineAttributesParse.group(1));
                    tempKeyValuePair.setValue(lineAttributesParse.group(2));
                    tempAttributeArray.add(tempKeyValuePair);
                } else {
//                    attributesFound = false;
                }
            }



            //At the end Element:stop:
            if (line.equalsIgnoreCase("Element:stop")){
                //Close remaining brackets
                for (int i = indentLevel; i > 0; i--) {
                    indentLevel--;
                    output += "\n" + getIndents(indentLevel) + "}";
                }



            }


        }

        //Remove excess commas
        output = output.replaceAll(",,",",");
        return output;
    }

    public static String printAsXML(String input){
        //input must be in intermediaryFormat(String)
        //Iterate through KeyValuePairs, printing to output in XML format
        //How to keep track of closing tags?
        //Each frame should remember which ones it needs to close
        String output = "";

        String inputNew = input.replaceAll("(?m)^[ \t]*\r?\n", "");
//        System.out.println(inputNew);

        input = input.replaceAll("\n\n","\n");
        String[] lines = input.split("\n");

        //Patterns
        Pattern elementPattern = Pattern.compile("Element:");
        Pattern pathPattern = Pattern.compile("path = (.*)");
        Pattern valuePattern = Pattern.compile("value = \"?([^\"]*)\"?");
        Pattern attributesPattern = Pattern.compile("attributes:");
        Pattern attributeParsePattern = Pattern.compile("([^\\s]*)\\s=\\s(.*)");

        //Flags
        boolean elementStart = false;
        boolean valueFound = false;
        boolean attributesFound = false;
        boolean isChildElement = false;
        boolean finishElement = false;
        boolean closeParentTag = false;
        boolean rootOrNot = false;

        //Counters
        int indentLevel = 0;
        int index = 0;
        int rootTestCounter = 0;
        int maxIndentLevel = 0;

        //Recurring variables
        String prevPath = "";
        String curPath = "";
        String curElementName = "";
        String prevElementName = "";
        String curValue = "";
        String attribKey = "";
        String attribValue = "";
        String curParentPath = "path = ";
        String prevFullPath = "";
        String curFullPath = "";
        Path latestPath;

        //Lists
        ArrayList<String> parentTags = new ArrayList<>();
        ArrayList<Path> pathList = new ArrayList<>();
        ArrayList<Path> pathListFull = new ArrayList<>();


        for (String line :
                lines) {
            Matcher lineElement = elementPattern.matcher(line);
            Matcher linePath = pathPattern.matcher(line);
            Matcher lineValue = valuePattern.matcher(line);
            Matcher lineAttributes = attributesPattern.matcher(line);
            Matcher lineAttributesParse = attributeParsePattern.matcher(line);

            boolean pause = true;
            boolean elementHeaderFound = false;
            if(lineElement.find()){
                elementHeaderFound = true;
            }
            if(elementHeaderFound && prevPath.equalsIgnoreCase("")){
                //first elementKey
                //Nothing really needs done here
            }

            //When you know that elementKey is done
            //NEED TO SET UP STUFF FOR WHEN YOURE AT THE LAST ELEMENT
            // || index == lines.length-1
            if (elementHeaderFound && !prevPath.equalsIgnoreCase("") ){
                //Wrap stuff up here from previous elementKey
                finishElement = true;
                attributesFound = false;
            }

            //Path is where you find out if it's child or not
            if (linePath.find()){
                //Parse path here, compare to previous path
                curFullPath = line;

                //Handle first elementKey
                if (prevPath.equalsIgnoreCase("")) {
                    prevFullPath = line;

                    prevPath = linePath.group(1);
                    curPath = linePath.group(1);
                    String[] pathArray = curPath.replaceAll("\\s","").split(",");
                    curElementName = pathArray[pathArray.length-1];
                    output += "<" + curElementName;

                    //Save elementName to prevElementName for future comparisons
                    prevElementName = pathArray[pathArray.length-1];


                    //No closing bracket yet, check for attributesOrKeyValuePairs
                } else if (finishElement){
                    finishElement = false;

                    //Subsequent elements
                    curPath = linePath.group(1);
                    String[] pathArray = curPath.replaceAll("\\s","").split(",");
                    curElementName = pathArray[pathArray.length-1];

                    //if curpath still contains previous elementKey, this new elementKey is a child
                    Pattern childPattern = Pattern.compile(prevElementName);
                    Matcher childMatcher = childPattern.matcher(curPath);
                    boolean childTest2 = false;

                    //If pathList is size 0, ignore fancy rules
//                    if(pathList.size() == 0){
                    if (childMatcher.find()){
                        String prevElementPath = "";
                        if (pathListFull.size() > 0) {
                            prevElementPath = pathListFull.get(pathListFull.size()-1).getPathToElement();
                        }
                        String pathCompare = prevElementPath + ", " + curElementName;
                        if(line.equalsIgnoreCase(pathCompare)){
                            isChildElement = true;
                        }

                    }
//                    } else {
                    //Otherwise, do the additional check to see if it's not a childElement

//                        notChild = line.equalsIgnoreCase(prevElementPath);
//                        if (childMatcher.find() && notChild){
//                            isChildElement = true;
//                        }
//                    }


                    //if it isChildElement, indentLevel++
                    if (isChildElement){
                        Path tempPath = new Path();
                        tempPath.setPrevElementName(prevElementName);
                        tempPath.setPathToElement(prevFullPath);
                        pathList.add(tempPath);
//                        parentTags.add(prevElementName);
//                        curParentPath = curPath;
                        indentLevel++;
                    }

                    //If there's a value, print it with closing tag
//                    if (!curValue.equalsIgnoreCase("")){

                    if (!curValue.equalsIgnoreCase("null") && !isChildElement){
                        output += ">" + curValue + "</" + prevElementName + ">";
                    } else if (curValue.equalsIgnoreCase("null") && !isChildElement){
                        output += " />";
                    } else {
                        output += ">";
                    }
//                    }




                    //Check if parentTag needs closing
                    //WHEN SHOULD WE DO THIS?? Maybe set a boolean that is checked at new line
                    //If at this point it needs to be closed off, it would have to be done here.


                    //Close parentTags here
                    if (pathList.size() > 0) {
                        for (int i = pathList.size() - 1; i >= 0; i--) {
                            latestPath = pathList.get(i);
                            Pattern latestPathPattern = Pattern.compile(latestPath.getPathToElement());
                            Matcher latestPathMatcher = latestPathPattern.matcher(curFullPath);

                            if (latestPathMatcher.find()) {
                                break;
                            } else {
                                indentLevel--;
                                String lastParentTag = latestPath.getPrevElementName();
                                output += "\n" + getIndents(indentLevel) + "</" + lastParentTag + ">";
                                pathList.remove(i);
                            }
                        }
                    }
                    //End of previous elementKey here
                    output += "\n";







                    //Add tabs depending on indent
                    output += getIndents(indentLevel);

                    //END OF LINE
                    isChildElement = false;



                    //replace 0 with indentLevel
                    output += "<" + curElementName;

                    //Save elementName to prevElementName for future comparisons
                    prevElementName = curElementName;
                    prevFullPath = curFullPath;
                }

                //Test for whether or not you need <root> here.
                //basically, if there'd be only one child of root, do root.
                //If there's more than one, trigger the rootOrNot flag so you can remove it at the end.
                if (indentLevel == 1) {
                    rootTestCounter++;
                }

                //Add element to pathListFull
                Path tempPath = new Path();
                tempPath.setPrevElementName(prevElementName);
                tempPath.setPathToElement(prevFullPath);
                pathListFull.add(tempPath);

            }
            if (lineValue.find()){
                valueFound = true;
                curValue = lineValue.group(1);
            }
            if (lineAttributes.find()){
                //Check for attributesOrKeyValuePairs header, get ready to parse attributesOrKeyValuePairs
                attributesFound = true;
            }
            if (attributesFound){
                //Attributes found, parse as they come.
                if (lineAttributesParse.find()){
                    attribKey = lineAttributesParse.group(1);
                    attribValue = lineAttributesParse.group(2);
                    output += " " + attribKey + "=" + attribValue;
                }
            }
            //This part runs at the very end
            if (line.equalsIgnoreCase("Element:stop")){
                //If there's a value, print it with closing tag
//                if (!curValue.equalsIgnoreCase("")){
                if (!curValue.equalsIgnoreCase("null")){
                    output += ">" + curValue + "</" + prevElementName + ">";
                } else if (curValue.equalsIgnoreCase("null")){//(!isChildElement){
                    output += " />";
                } else {
                    output += ">";
                }
                //Check for rootOrNot and delete root and remove extra indents if needed
                if (rootTestCounter == 1 && pathList.size() == 0){
                    //Remove root tags
                    output = output.replaceAll("</?root>\n?","");
                    //Remove 1 of each tab
                    String tabReduceRegex = "";
                    String tabReplaceString = "";
                    for (int i = 1; i <= maxIndentLevel; i++) {
                        tabReduceRegex = "[^\t]" + getIndents(i) + "[^\t]";
                        tabReplaceString = getIndents(i-1);
                        output = output.replaceAll(tabReduceRegex,tabReplaceString);
                    }

                }
            }
            index++;
            //Keep track of maxIndentLevel to use with rootOrNot at end of conversion
            if (maxIndentLevel < indentLevel){
                maxIndentLevel = indentLevel;
            }
        }



        //Append closing tags, if they're still there
        if (pathList.size() > 0) {
            for (int i = pathList.size() - 1; i >= 0; i--) {
                latestPath = pathList.get(i);

                indentLevel--;
                String lastParentTag = latestPath.getPrevElementName();
                output += "\n" + getIndents(indentLevel) + "</" + lastParentTag + ">";
                pathList.remove(i);
            }


            //Check for rootOrNot and delete root and remove extra indents if needed
            if (rootTestCounter == 1){
                //Remove root tags
                output = output.replaceAll("\n?</?root>\n?","");
                //Remove 1 of each tab
                String tabReduceRegex = "";
                String tabReplaceString = "";

                tabReduceRegex = "([^\t\\n]?)(\t)(?=[^\t])";
                output = output.replaceAll(tabReduceRegex,"");
                boolean pause = true;


            }
        }

        return output;
    }

    public static String convertJSONToIntermediaryFormat(KeyValuePair input, int depth, String path) {
        String output = "";
        Pattern attribPattern = Pattern.compile("^@.*");
        Pattern elementValuePattern = Pattern.compile("^#.*");
//        if (depth != 0) {
        if (depth > 0) {
            output += "\n";
        }
        output += "Element:\n";
        output += "path = " + path + "\n";
        if (input.getValue().equalsIgnoreCase("") && input.getValueArray().size() == 0) {
            if (input.getValue().equalsIgnoreCase("null")) {
                output += "value = null"+ "\n";
            } else {
                output += "value = \"\""+ "\n";
            }
        } else if (!input.getValue().equalsIgnoreCase("") && input.getValueArray().size() == 0) {
            output += "value = " + input.getValue()+ "\n";
        }
        Iterator i = input.getValueArray().iterator();
        while (i.hasNext()) {
            KeyValuePair temp = (KeyValuePair) i.next();
            Matcher elementValueMatcher = elementValuePattern.matcher(temp.getKey());
            if (!elementValueMatcher.find() || temp.getValueArray().size() != 0) continue;
            output += "value = " + temp.getValue() + "\n";
            i.remove();
            break;
        }
//        }
        boolean attribHeaderPrinted = false;

        //This flag makes sure it doesn't run the depth == 0 portion twice and print double
        boolean depthZeroRan = false;

        //Prevent printing a section twice
        boolean hasBeenPrinted = false;

        //Iterate through KeyValuePairs held within the root ValueArray
        if (input.getValueArray().size() > 0) {
            for (KeyValuePair keyValuePair : input.getValueArray()) {
                //Do this if at depth of 0 AKA root
                if (depth == 0) {
                    path = path + ", " + keyValuePair.getKey();
                    String tempPath = path;
                    output += convertJSONToIntermediaryFormat(keyValuePair, (depth + 1), tempPath);
                    path = (path).replaceFirst(", " + keyValuePair.getKey(), "");

//                    for (KeyValuePair valueArrayKeyValuePair : input.getValueArray()) {
//                        path = path + ", " + valueArrayKeyValuePair.getKey();
//                        String tempPath = path;
//                        output += convertJSONToIntermediaryFormat(valueArrayKeyValuePair, (depth + 1), tempPath);
//                        path = (path).replaceFirst(", " + valueArrayKeyValuePair.getKey(), "");
//                    }
                    depthZeroRan = true;
                    hasBeenPrinted = true;
                    continue;
                } else {///ADDED THIS ONE HERE
                    String key;
                    Matcher attribMatcher = attribPattern.matcher(keyValuePair.getKey());
                    Matcher elementValueMatcher = elementValuePattern.matcher(keyValuePair.getKey());
                    boolean invalid = false;
                    if (attribMatcher.find()) {
                        key = keyValuePair.getKey().replaceFirst("@", "");
                        if (key.length() == 0) {
                            invalid = true;
                            continue;
                        }
                        if (depth != 0 && !attribHeaderPrinted) {
                            output += "attributes:" + "\n";
                            attribHeaderPrinted = true;
                        }
                        if (keyValuePair.getValue().equalsIgnoreCase("null")) {
                            output += key + " = \"\"" + "\n";
                            continue;
                        }
                        output += key + " = " + keyValuePair.getValue() + "\n";
                        continue;
                    }
                    if (elementValueMatcher.find()) {
                        key = keyValuePair.getKey().replaceFirst("#", "");
                        if (keyValuePair.getValue().equalsIgnoreCase("") && keyValuePair.getValueArray().size() > 0) {
                            for (KeyValuePair valueArrayKeyValuePair : keyValuePair.getValueArray()) {
                                path = path + ", " + valueArrayKeyValuePair.getKey();
                                String tempPath = path;
                                output += convertJSONToIntermediaryFormat(valueArrayKeyValuePair, (depth + 1), tempPath);
                                path = (path).replaceFirst(", " + valueArrayKeyValuePair.getKey(), "");
                            }
                            continue;
                        }
                        output += "value = " + keyValuePair.getValue() + "\n";
                        continue;
                    } else {
                        ///ADDED THIS ONE HERE
                        //Do I need to change this?

                        path = path + ", " + keyValuePair.getKey();
                        output += convertJSONToIntermediaryFormat(keyValuePair, (depth + 1), path);
                        path = (path).replaceFirst(",[^,]*$", "");
                    }
                }

            }
        }
        if (depth != 0) {
            boolean temp = true;
        }
        //add Element: to end to help printAsXML wrap things up after last elementKey
        if (depth == 0){
            output += "Element:stop\n";
        }

        return output;
    }

    public static String convertXMLToIntermediaryFormat(Node node, int depth, String path) {
        path = (path).equalsIgnoreCase("") ? node.elementKey : path + ", " + node.elementKey;
        String output = "";
        output += "Element:\n";
        output += "path = " + path + "\n";
        if (node.getChildren().size() == 0) {
            if (node.getElementValue().equalsIgnoreCase("null")) {
                output += "value = null\n";
            } else {
                output += "value = \"" + node.getElementValue() + "\"\n";
            }
        }
        if (node.getAttributesOrKeyValuePairs().size() > 0) {
            output += "attributes:\n";
            for (Map.Entry attribute : node.getAttributesOrKeyValuePairs().entrySet()) {
                output += attribute.getKey() + " = \"" + attribute.getValue() + "\"\n";
            }
        }
        //Add line that lets you know what children are upcoming
        if (node.getChildren().size() > 0) {
            String tempChildrenString = "children = ";
            for (Node child : node.getChildren()) {
                tempChildrenString += child.getElementKey();
                if (node.getChildren().indexOf(child) < node.getChildren().size() - 1) {
                    tempChildrenString += "|";
                }
            }
            output += tempChildrenString + "\n";
        }
        ++depth;
        output += "\n";
        //Process child Nodes
        if (node.getChildren().size() > 0) {

            for (Node child : node.getChildren()) {
                output += convertXMLToIntermediaryFormat((Node)child, depth, path);
            }
        }
        if (depth == 1){
            output += "\nElement:stop";
        }
        return output;
    }
    //</editor-fold>
}