# XSD Compare
XSD Compare allows you to compare different versions of XML schema files against each other and gives a visual representation of the difference(s).

![XSD Compare example](https://i.imgur.com/JatqEim.png)

## System Requirements

### Runtime

All dependencies are present in the executables for runtime.

- CPU: 1GHz
- Memory: 250MB

### Development

* Java 17+
* OpenJFX 21+

#### Development program arguments

When wanting to run this application locally from your IDE, it's recommended to add the following VM options:

```shell
-Dsun.awt.disablegrab=true 
-Dprism.verbose=true 
-Xms100M 
-XX:+UseG1GC 
-XX:+HeapDumpOnOutOfMemoryError 
-p "/path/to/openjfx/21/lib" 
--add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.web,javafx.swing
```

## Features

### 0.0.6
* Display XML schema files in a tree view
* Drag-and-drop loading of XML schema files
* Synchronizing scroll and selection between the 2 tree views
* Showing differences between the 2 XML schema files
* Show detailed properties of fields (more properties will be added)
* Copy field as XPath to clipboard
* Copy field as XML to clipboard
* Exporting the differences to an Excel file
* Add Excel colors
* Settings for shown columns

### Upcoming features
* Batch comparison of XML schema files
