package com.compare.xsd;

import com.compare.xsd.comparison.XsdComparer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XsdCompareStarter {
    public static void main( String[] args ) throws Exception {
        System.exit(run(args));
    }

    @SuppressWarnings("deprecation")
    public static int run( String[] args ) throws Exception {
        final List<String> fileNames = new ArrayList<String>();

        boolean gui = false;
        boolean single = false;

        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].trim();
            if (args[i].equalsIgnoreCase("-h")
                    || args[i].equalsIgnoreCase("-help")
                    || args[i].equalsIgnoreCase("-?")) {
                usage();
                return -1;
            }
            else if (args[i].equalsIgnoreCase("-singleline")
                    || args[i].equalsIgnoreCase("-s")
                    || args[i].equalsIgnoreCase("--singleline")
                    || args[i].equalsIgnoreCase("--s")) single = true;
            else if (args[i].equalsIgnoreCase("-multiline")
                    || args[i].equalsIgnoreCase("-m")
                    || args[i].equalsIgnoreCase("--multiline")
                    || args[i].equalsIgnoreCase("--m")) single = false;
            else if (args[i].equalsIgnoreCase("-gui")
                    || args[i].equalsIgnoreCase("-g")
                    || args[i].equalsIgnoreCase("--gui")
                    || args[i].equalsIgnoreCase("--g")) gui = true;
            else if (args[i].equalsIgnoreCase("-ui")
                    || args[i].equalsIgnoreCase("-u")
                    || args[i].equalsIgnoreCase("--ui")
                    || args[i].equalsIgnoreCase("--u")) gui = false;

                /* usually the version number was added ot manifest during build
            } else if (args[i].equalsIgnoreCase("-version") || args[i].equalsIgnoreCase("-v")) {
                printVersion();
                return -1;
            }*/

            else {
                if( args[i].charAt(0)=='-' ) {
                    System.err.println("Unrecognized option: " + args[i]);
                    usage();
                    return -1;
                }
                fileNames.add(args[i]);
            }
        }
        if(fileNames.size() != 2){
            usage();
        }else{
            String currentDir = System.getProperty("user.dir") + File.separator;
            if(gui){
                String[] guiArgs = {currentDir + fileNames.get(0), currentDir + fileNames.get(1)};
                XsdCompareApplication.main(guiArgs);
            }else {
                XsdComparer comparer = new XsdComparer(currentDir + fileNames.get(0), currentDir + fileNames.get(1));
                System.out.println(comparer.compareAsString());
            }
            return 0;
        }
        return -1;
    }


    /** Prints the usage screen. */
    private static void usage() {
        System.out.println(USAGE_UI);
    }

    private static final String USAGE_UI =
    "usage: java -jar xsd-compare-jar-with-dependencies.jar <opts> <old-xsd-grammar> <new-xsd-grammar>\n\n" +
            "options:\n" +
            "\t\t-singleline or -s: one single line for each change with XPath in the start.\n" +
            "\t\t-multiline or -m: multiple lines indented per change sorted by XSD change. (default).\n" +
            "\t\t-gui: starts a JavaFX GUI front-end for comparison.\n" +
            "\t\t-ui: omits the GUI and returns only a text result for comparision (default).\n" +
            "\t\t-help or -h: this help text.\n";
            // + "\t\t-version   : display version number.\n";

}
