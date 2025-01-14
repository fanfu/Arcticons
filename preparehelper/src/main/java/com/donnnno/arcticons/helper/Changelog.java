package com.donnnno.arcticons.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Changelog {
    public static void main(String[] args) {
        //String
        String rootDir = System.getProperty("user.dir");
        // Get the path of the root directory
        Path rootPath = Paths.get(rootDir);
        // Get the name of the root directory
        String rootDirName = rootPath.getFileName().toString();
        if (rootDirName.equals("preparehelper")) {
            rootDir = "..";
        }
        String valuesDir = rootDir+"/app/src/main/res/values";
        String appFilter = rootDir + "/newicons/appfilter.xml";
        String changelogXml = valuesDir +"/changelog.xml";
        String generatedDir = rootDir +"/generated";

       generateChangelogs(generatedDir, valuesDir+"/custom_icon_count.xml", appFilter, changelogXml,false);
    }





    public static void generateChangelogs(String generatedDir, String customIconCountXml, String appFilter, String changelogXml,boolean newRelease) {
        String newXML = generatedDir + "/newdrawables.xml";
        int countTotal = getCustomIconsCount(customIconCountXml);
        int countNew = countAll(newXML);
        int countFilterTotal = countAll(appFilter);
        int countFilterOld = readCountFilterOld(generatedDir);//19762; //tag11.4.6(21744)
        int countReused = countFilterTotal - countFilterOld - countNew;

        createChangelogXML(countTotal, countNew, countReused, changelogXml);
        createChangelogMd(countTotal, countNew, countReused, generatedDir);

        if (newRelease) {
            //save countFilterTotal to file
            try {
                writeToFile(String.valueOf(countFilterTotal), generatedDir + "/countFilterTotal.txt");
                System.out.println("countFilterTotal saved to: " + generatedDir + "/countFilterTotal.txt");
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }
        }
    }



    public static int readCountFilterOld(String generatedDir) {
        //read count from File
        try {
            Path path = Paths.get(generatedDir + "/countFilterTotal.txt");
            String content = new String(Files.readAllBytes(path)).strip();
            return Integer.parseInt(content);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

      return  0 ;
    }
    public static void createChangelogMd(int countTotal, int countNew, int countReused, String changelogMd) {
        StringBuilder output = new StringBuilder("* \uD83C\uDF89 **");
                output.append(countNew);
                output.append("** new and updated icons!\n");
                output.append("* \uD83D\uDCA1 Added support for **");
                output.append(countReused);
                output.append("** apps using existing icons.\n");
                output.append("* \uD83D\uDD25 **");
                output.append(countTotal);
                output.append("** icons in total!");

        try {
            writeToFile(output.toString(), changelogMd + "/changelog.md");
            System.out.println("Changelog saved to: " + changelogMd + "/changelog.md");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void createChangelogXML(int countTotal, int countNew, int countReused, String changelogXml){
        StringBuilder output = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "\n" +
                "    <!-- Leave empty if you don't want to show changelog date -->\n" +
                "    <string name=\"changelog_date\">");
        output.append(currentDate());
        output.append("</string>\n\n");
        output.append("    <!-- Changelog support html formatting\n");
        output.append("    * <b> for Bold\n");
        output.append("    * <i> for Italic\n");
        output.append("    * <u> for Underline\n");
        output.append("    * <a href=\"linkUrl\">Link Text</a> for links -->\n");
        output.append("    <string-array name=\"changelog\">\n");
        output.append("        <item>🎉 <b>");
        output.append(countNew);
        output.append("</b> new and updated icons!</item>\n");
        output.append("        <item>💡 Added support for <b>");
        output.append(countReused);
        output.append("</b> apps using existing icons.</item>\n");
        output.append("        <item>🔥 <b>");
        output.append(countTotal);
        output.append("</b> icons in total!</item>\n");
        output.append("    </string-array>\n");
        output.append("</resources>");

        // Write the output to the file
        try {
            writeToFile(output.toString(), changelogXml);
            System.out.println("Changelog saved to: " + changelogXml);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

    }

    // Method to write a string to a file
    public static void writeToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes());
    }
    // Method to get the current date in "MMM dd, yyyy" format
    public static String currentDate() {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return date.format(formatter);
    }

    public static int countAll(String drawableXml){
        try {
            // Path to the XML file
            Path xmlPath = Paths.get(drawableXml);

            // Create a DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Create a DocumentBuilder
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML file
            Document document = builder.parse(xmlPath.toFile());

            // Normalize the document (optional, but recommended)
            document.getDocumentElement().normalize();

            // Get all <item> nodes
            NodeList itemList = document.getElementsByTagName("item");

            // Count the <item> nodes
            int itemCount = itemList.getLength();

            // Output the count
            System.out.println("Number of <item> entries: " + itemCount);
            return itemCount;
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
        return 0;
    }

    public static int getCustomIconsCount(String XmlIconCount)  {
        try {
        // Path to the XML file
        Path xmlPath = Paths.get(XmlIconCount);
        // Create a DocumentBuilderFactory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Create a DocumentBuilder
        DocumentBuilder builder = factory.newDocumentBuilder();
        // Parse the XML file
        Document document = builder.parse(xmlPath.toFile());
        // Normalize the document (optional, but recommended)
        document.getDocumentElement().normalize();
        // Get all <item> nodes
        NodeList itemList = document.getElementsByTagName("integer");
        // Iterate through the NodeList and retrieve the value of each <integer> element
            for (int i = 0; i < itemList.getLength(); i++) {
                // Get the individual node at index i
                Node node = itemList.item(i);

                // Ensure the node is an element (in case there are other types of nodes)
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    // Cast to an Element
                    Element element = (Element) node;

                    // Retrieve the text content of the <integer> element
                    return Integer.parseInt(element.getTextContent());
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
        return 0;
    }

}
