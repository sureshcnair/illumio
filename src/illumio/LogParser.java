package illumio;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class LogParser {
    //Constant defined to be used for validating/ reading the flow log
    static final int FLOWLOG_DSTPORT = 3;
    static final int FLOWLOG_PROTOCOL = 4;
    static final int VALID_FLOWLOG_LENGTH = 5;

    static final int LOOKUP_DSTPORT = 0;
    static final int LOOKUP_PROTOCOL = 1;
    static final int LOOKUP_TAG = 2;
    static final int VALID_LOOKUP_ENTRY_LENGTH = 3;

    // Method to load the lookup table from passed in CSV file
    private static Map<FlowLogEntry, String> loadLookupTable(String lookupFile) throws IOException {
        Map<FlowLogEntry, String> lookupTable = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(lookupFile))) {
            String line;
            // Skip the header line
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                //Check for valid entries and skip otherwise
                if (parts.length != VALID_LOOKUP_ENTRY_LENGTH) continue;
                int dstport = Integer.parseInt(parts[LOOKUP_DSTPORT].trim());
                String protocol = parts[LOOKUP_PROTOCOL].trim();
                String tag = parts[LOOKUP_TAG].trim();
                lookupTable.put(new FlowLogEntry(dstport, protocol), tag);
            }
        }
        return lookupTable;
    }

    // Method to parse the flow log file and return counts of tags and port/protocol combinations
    private static void readFlowLog(String flowLogFile, Map<FlowLogEntry, String> lookupTable,
                                    Map<String, Integer> tagCounts, Map<FlowLogEntry, Integer> portProtocolCounts) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(flowLogFile))) {
            String line;
            br.readLine();//Skip the first line as it states the column names and read only form the next line onwards

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                // If there is any malformed flow log entries, skip them
                if (parts.length != VALID_FLOWLOG_LENGTH) continue;
                int dstport = Integer.parseInt(parts[FLOWLOG_DSTPORT].trim());
                String protocol = parts[FLOWLOG_PROTOCOL].trim().toLowerCase();

                // Look up the tag from the lookup table
                String tag = lookupTable.getOrDefault(new FlowLogEntry(dstport, protocol), "Untagged");

                // Update tag count
                tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);

                // Update port/protocol combination count
                FlowLogEntry flowLogEntry = new FlowLogEntry(dstport, protocol);
                portProtocolCounts.put(flowLogEntry, portProtocolCounts.getOrDefault(flowLogEntry, 0) + 1);
            }
        }
    }

    // Method to write the output to a file
    private static void writeOutput(Map<String, Integer> tagCounts, Map<FlowLogEntry, Integer> portProtocolCounts, String outputFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile))) {
            writer.write("Tag Counts:\n");
            writer.write(String.format("%-15s %-6s\n", "Tag", "Count"));
            for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
                writer.write(String.format("%-15s %-6d\n", entry.getKey(), entry.getValue()));
            }

            writer.write("\nCount of matches for each port/protocol combination:\n");
            writer.write(String.format("%-8s %-8s %-6s\n", "Port", "Protocol", "Count"));
            for (Map.Entry<FlowLogEntry, Integer> entry : portProtocolCounts.entrySet()) {
                FlowLogEntry key = entry.getKey();
                writer.write(String.format("%-8d %-8s %-6d\n", key.dstport, key.protocol, entry.getValue()));
            }
        }
    }

    //Custom class defined to
    static class FlowLogEntry {
        int dstport;
        String protocol;

        public FlowLogEntry(int dstport, String protocol) {
            this.dstport = dstport;
            this.protocol = protocol.toLowerCase();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            FlowLogEntry fObj = (FlowLogEntry) obj;
            return dstport == fObj.dstport && protocol.equals(fObj.protocol);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dstport, protocol);
        }
    }

    public static void main(String[] args) {
        // Input and output file paths
        String lookupFile = "src/illumio/look_up.csv";
        String flowLogFile = "src/illumio/flow_log.csv";
        String outputFile = "src/illumio/output.txt";

        // Maps to hold tags and port/protocol counts interim to write to the output
        //In real time, this can also be kept as live data structure and the reports can be constructed per day once or
        // other scheduled time, if the flow log is read from S3 bucket or a goprwing file without restarting the whole program again
        Map<String, Integer> tagCounts = new HashMap<>();
        Map<FlowLogEntry, Integer> portProtocolCounts = new HashMap<>();

        try {
            // Construct the lookup table as defined in the input
            System.out.println("Loading the Lookup table");
            Map<FlowLogEntry, String> lookupTable = loadLookupTable(lookupFile);

            // Parse the flow log
            System.out.println("Reading the flow log entries");
            readFlowLog(flowLogFile, lookupTable, tagCounts, portProtocolCounts);

            // Write the results to an output file
            System.out.println("Generating output");
            writeOutput(tagCounts, portProtocolCounts, outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

