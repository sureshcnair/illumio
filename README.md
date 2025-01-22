# Flow Log Parser
# **Overview**

This Java program is designed to parse a flow log file and map each entry to a tag based on a predefined lookup table. The lookup table defines mappings between destination port (dstport), protocol, and a tag. The program generates an output file containing two sections:

- **Tag Counts**: The number of times each tag appears in the flow log.
- **Port/Protocol Combination Counts**: The count of matches for each destination port and protocol combination.

The lookup table is an inout file in CSV format with three columns:

- **dstport** (Destination Port)
- **protocol**
- **tag**

This table can be easily modified to add or update mappings. When the program is re-executed, it will reflect any changes made to the lookup table, providing updated results.

---

# **Presumptions**

- **Limited Columns**: The flow log is assumed to contain only two specific fields: dstport (destination port) and protocol. Although real-world flow logs can contain many more fields, this program simplifies the problem by focusing on these two attributes. Note that in a real world scenario, the FlowLogReader can be a library with ability to read multiple attributes . We are limiting ourselves to  only dstport  and protocol. Purposefully, two addiitonal attributes are added ( not used for the current implementation ) but to show how it works.
- **Predefined Lookup Table**: The lookup table is used to map a combination of dstport and protocol to a specific tag.
- **Flow Log Format**: The flow log format is presumed to be CSV, where each line contains a destination port and protocol.

The program expects the lookup table and flow log to be in the same directory as the source code or within specified file paths. The result is written to an output file.

---

# **Features**

- **Tag Count Generation**: Count how many times each tag appears in the flow log.
- **Port/Protocol Combination Count**: Track the number of occurrences for each port/protocol combination.
- **Lookup Table Flexibility**: Modify the lookup table (CSV file) as needed. When the program is rerun, it will consider the updated lookup table for mapping dstport and protocol combinations.
