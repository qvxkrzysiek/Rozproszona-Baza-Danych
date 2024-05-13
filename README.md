# Documentation

To view documentation for individual functions, run Documentation.bat or index.html in the JavaDoc folder.
The entire project operates on the TCP protocol.
.bat files can be used for quick node startup.

# Network Organization

When creating a new node and wanting to connect it to an existing one, you must establish a connection and communicate your presence. Only then can node-to-node connections be established; otherwise, communication would be one-sided. To achieve this, I've introduced the "new-connection" function, which is sent in the form of a string.
Nodes do not differ in communication from regular clients since they connect via the TCP protocol and are themselves clients. However, to differentiate between them, I've implemented the "identify" function, which is sent as the first string. This allows us to know that we're dealing with a node and enables us to conduct certain redundant operations correctly.
Several functions in the code are overloaded to facilitate the execution of redundant operations.
I avoid unwanted recursion by sending the string "identify" and a list of nodes that responded to this query.

# Compiling

I'm providing the DatabaseNode file in raw (.java) form and as a binary file (.class).
If the basic invocation of the database failed due to compilation issues, follow these steps:
Open the Windows console.
Navigate to the file using the cd command.
Enter: javac DatabaseNode.java
After these steps, the program can be invoked again.
Example invocation: java DatabaseNode -tcpport 80 -record 1:25

# Console Information

- [INFO] -> Information about current node operations.
- [INFO CORE] -> Lines of information received from another node.
- [WARNING] -> Warnings about potential network malfunctions.
- [ERROR] -> Critical error resulting in program termination.
