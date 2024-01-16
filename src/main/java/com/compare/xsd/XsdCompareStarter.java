package com.compare.xsd;

import com.compare.xsd.comparison.TextReport;
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
        /** For instance, the SingleLineChangeTextReport creates only a single text line for every XSD change
         * this harder to read, but assists  to compare the result with other XSD comparison tools */
        TextReport.implementation reportType = TextReport.implementation.MULTI_LINE_CHANGE;

        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].trim();
            if (args[i].equalsIgnoreCase("-h")
                    || args[i].equalsIgnoreCase("-help")
                    || args[i].equalsIgnoreCase("-?")) {
                usage();
                return -1;
            }
            else if (args[i].equalsIgnoreCase("-single")
                    || args[i].equalsIgnoreCase("-s")
                    || args[i].equalsIgnoreCase("--single")
                    || args[i].equalsIgnoreCase("--s")) reportType = TextReport.implementation.SINGLE_LINE;
            else if (args[i].equalsIgnoreCase("-multi")
                    || args[i].equalsIgnoreCase("-m")
                    || args[i].equalsIgnoreCase("--multi")
                    || args[i].equalsIgnoreCase("--m")) reportType = TextReport.implementation.MULTI_LINE_CHANGE;
            else if (args[i].equalsIgnoreCase("--extensions-only")
                    || args[i].equalsIgnoreCase("-e")
                    || args[i].equalsIgnoreCase("-extensions-only")
                    || args[i].equalsIgnoreCase("--e")) reportType = TextReport.implementation.ONLY_EXTENSIONS;
            else if (args[i].equalsIgnoreCase("--restrictions-only")
                    || args[i].equalsIgnoreCase("-r")
                    || args[i].equalsIgnoreCase("-restrictions-only")
                    || args[i].equalsIgnoreCase("--r")) reportType = TextReport.implementation.ONLY_RESTRICTIONS;
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
                XsdComparer comparer = new XsdComparer(currentDir + fileNames.get(0), currentDir + fileNames.get(1), TextReport.implementation.SINGLE_LINE);
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
            "\t\t--ui: omits the GUI and returns only a text result for comparision (default).\n" +
            "\t\t--gui: starts a JavaFX GUI front-end for comparison.\n" +
            "\t\t--multi or -m: multiple lines indented per change sorted by XSD change. (default).\n" +
            "\t\t--single or -s: one single line for each change with XPath in the start, harder to read but easier for compare the output with other tools.\n" +
            "\t\t--extensions-only  or -e: one single line for each extension of the grammar sorted by type. Restrictions/limitations are being neglected.\n" +
            "\t\t--restrictions-only  or -r: one single line for each new restrictions, requirements or limitations of the new grammar sorted by type. Extensions are being neglected.\n" +
            "\t\t--help or -h: this help text.\n";
            // + "\t\t-version   : display version number.\n";

}
