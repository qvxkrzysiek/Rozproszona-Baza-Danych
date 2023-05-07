import java.util.ArrayList;
import java.util.Objects;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

/***
 * Projekt SKJ Rozproszona baza danych
 * @author Krzysztof Bartoszek s24321
 */
public class DatabaseNode {

    //============================ NODE START ============================

    /***
     * Starts whole node
     * @param args Arguments
     */
    public static void main(String[] args) {

        int port = 0;
        int key = 0;
        int value = 0;
        ArrayList<String> connectionsList = new ArrayList<>();

        for(int i = 0 ; i < args.length ; i = i + 2){
            //CHECKING TCP
            if(Objects.equals(args[i], "-tcpport")){
                port = Integer.parseInt(args[i+1]);
            }
            //CHECKING RECORD
            if(Objects.equals(args[i], "-record")){
                String[] str = args[i+1].split(":");

                key = Integer.parseInt(str[0]);
                value = Integer.parseInt(str[1]);
            }
            //CHECKING CONNECT
            if(Objects.equals(args[i], "-connect")){
                connectionsList.add(args[i+1]);
            }
        }

        //CREATING NODE
        new DatabaseNode(port,key,value,connectionsList);

    }

    //============================ NODE CORE ============================

    //NODE FIELDS
    private final int PORT;
    private int KEY;
    private int VALUE;
    private ArrayList<String> CONNECTIONS_LIST;

    /***
     * Node constructor
     * @param port Node port
     * @param key Node start key
     * @param value Node start value
     * @param connectionsList Node start connections list
     */
    private DatabaseNode(int port, int key, int value, ArrayList<String> connectionsList) {
        this.PORT = port;
        this.KEY = key;
        this.VALUE = value;
        this.CONNECTIONS_LIST = connectionsList;
        tryToConnect();
        showNode();
        listenSocket(this.PORT);
    }

    //============================ CORE OPERATIONS ============================

    /***
     * Shows changes of the node to console
     */
    private void showNode(){
        System.out.println("[INFO]: =====[ STATUS HAS CHANGED ]=====");
        System.out.println("[INFO]: * Data: " + KEY+":"+VALUE);
        System.out.println("[INFO]: * Connections: " + CONNECTIONS_LIST);
        System.out.println("[INFO]: ================================");
    }

    /***
     * A node informs other nodes about its creation so that they can add it to the list of connections
     */
    private void tryToConnect(){
        for (String connection : CONNECTIONS_LIST) {
            if (Objects.equals(sender(connection, "new-connection localhost:" + PORT), "OK")) {
                System.out.println("[INFO]: Connection success!");
            }
        }
    }

    /***
     * Adds a new node to the connection list
     * @param connection connection to add
     * @return "OK" if added
     */
    private String newConnection(String connection){
        if(!CONNECTIONS_LIST.contains(connection)) CONNECTIONS_LIST.add(connection);
        System.out.println("[INFO]: Successfully added new connection!");
        showNode();
        return "OK";
    }

    //============================ OPERATIONS ============================

    /***
     * Sets a new value for the given key
     * @param key The key under which the value is needed to be changed
     * @param value The value we change to
     * @param usedNodesList List of nodes that responded to this task
     * @return Returns "OK" if the value was changed successfully, returns "ERROR" if not
     */
    private String setValue(int key, int value, ArrayList<String> usedNodesList){
        if(KEY == key){
            VALUE = value;
            System.out.println("[INFO]: Value was found in node!");
            showNode();
            return "OK";
        } else {
            System.out.println("[INFO]: No such key in node, starting network search.");
            for (String connection : CONNECTIONS_LIST) {
                if(!usedNodesList.contains(connection)){
                    String str = sender(connection, "set-value " + key+":"+value,usedNodesList);
                    if(!Objects.equals(str, "ERROR")){
                        System.out.println("[INFO]: Node: " + connection + " found and changed value!");
                        return str;
                    }
                }
            }
            System.out.println("[INFO]: Key not found.");
        }
        return "ERROR";
    }

    /***
     * Returns the value under the given key
     * @param key Given key
     * @param usedNodesList List of nodes that responded to this task
     * @return Returns value under the given key, returns "ERROR" if there is not such key
     */
    private String getValue(int key, ArrayList<String> usedNodesList){
        if(KEY == key){
            System.out.println("[INFO]: Value was found in node!");
            return KEY + ":" + VALUE;
        } else {
            System.out.println("[INFO]: No such key in node, starting network search.");
            for (String connection : CONNECTIONS_LIST) {
                if(!usedNodesList.contains(connection)){
                    String str = sender(connection, "get-value " + key,usedNodesList);
                    if(!Objects.equals(str, "ERROR")){
                        System.out.println("[INFO]: Node: " + connection + " found searched value!");
                        return str;
                    }
                }
            }
            System.out.println("[INFO]: Value not found.");
        }
        return "ERROR";
    }

    /***
     * Returns the IP and port that has the given key
     * @param key Given key
     * @param usedNodesList List of nodes that responded to this task
     * @return Returns the IP and port UNDER THE GIVEN KEY, returns "ERROR" if there is not such key
     */
    private String findKey(int key, ArrayList<String> usedNodesList){
        if(KEY == key){
            System.out.println("[INFO]: Key was found in node!");
            return "localhost:" + PORT;
        } else {
            System.out.println("[INFO]: No such key in node, starting network search.");
            for (String connection : CONNECTIONS_LIST) {
                if(!usedNodesList.contains(connection)){
                    String str = sender(connection, "find-key " + key,usedNodesList);
                    if(!Objects.equals(str, "ERROR")){
                        System.out.println("[INFO]: Node: " + connection + " found searched value!");
                        return str;
                    }
                }
            }
            System.out.println("[INFO]: Value not found.");
        }
        return "ERROR";
    }

    /***
     * If it is the second or every subsequent node that responds to this task,
     * it returns the maximum value in the database
     * @param MAX Current known maximum value
     * @param usedNodesList List of nodes that responded to this task
     * @return Maximum value in the database
     */
    private String getMax(String MAX, ArrayList<String> usedNodesList){
        String[] str = MAX.split(":");
        int max = Integer.parseInt(str[1]);
        if(VALUE > max){
            max = VALUE;
            str[0] = String.valueOf(KEY);
        }
        for (String connection : CONNECTIONS_LIST) {
            if(!usedNodesList.contains(connection)){
                str = sender(connection, "get-max " + str[0] + ":" + max,usedNodesList).split(":");
                max = Integer.parseInt(str[1]);
            }
        }
        return str[0] + ":" + max;
    }

    /***
     * If it is the first node that responds to this task,
     * it returns the maximum value in the database
     * @param usedNodesList List of nodes that responded to this task
     * @return Maximum value in this node
     */
    private String getMax(ArrayList<String> usedNodesList){
        int max = VALUE;
        int key = KEY;
        String[] str;
        for (String connection : CONNECTIONS_LIST) {
            if(!usedNodesList.contains(connection)){
                str = sender(connection, "get-max " + key + ":" + max,usedNodesList).split(":");
                max = Integer.parseInt(str[1]);
                key = Integer.parseInt(str[0]);
            }
        }
        return key + ":" + max;
    }

    /***
     * If it is the second or every subsequent node that responds to this task,
     * it returns the minimum value in the database
     * @param MIN Current known minimum value
     * @param usedNodesList List of nodes that responded to this task
     * @return Minimum value in the database
     */
    private String getMin(String MIN, ArrayList<String> usedNodesList){
        String[] str = MIN.split(":");
        int min = Integer.parseInt(str[1]);
        if(VALUE < min){
            min = VALUE;
            str[0] = String.valueOf(KEY);
        }
        for (String connection : CONNECTIONS_LIST) {
            if(!usedNodesList.contains(connection)){
                str = sender(connection, "get-min " + str[0] + ":" + min,usedNodesList).split(":");
                min = Integer.parseInt(str[1]);
            }
        }
        return str[0] + ":" + min;
    }
    /***
     * If it is the first node that responds to this task,
     * it returns the minimum value in the database
     * @param usedNodesList List of nodes that responded to this task
     * @return Minimum value in this node
     */
    private String getMin(ArrayList<String> usedNodesList){
        int min = VALUE;
        int key = KEY;
        String[] str;
        for (String connection : CONNECTIONS_LIST) {
            if(!usedNodesList.contains(connection)){
                str = sender(connection, "get-min " + key + ":" + min,usedNodesList).split(":");
                min = Integer.parseInt(str[1]);
                key = Integer.parseInt(str[0]);
            }
        }
        return key + ":" + min;
    }

    /***
     * Sets a new key:value pair on this node
     * @param key New key
     * @param value New value
     * @return Returns "OK" if key and value were changed successfully
     */
    private String newRecord(String key, String value){
        KEY = Integer.parseInt(key);
        VALUE = Integer.parseInt(value);
        System.out.println("[INFO]: Successfully created a new record!");
        showNode();
        return "OK";
    }

    /***
     * Kills this node and informs other nodes about it
     * @param usedNodesList List of nodes that responded to this task
     * @return Returns "OK" if informed other nodes
     */
    private String terminate(ArrayList<String> usedNodesList){
        for (String connection : CONNECTIONS_LIST) {
            sender(connection, "terminate localhost:" + PORT, usedNodesList);
        }
        System.out.println("[INFO]: Terminating node!");
        return "OK";
    }
    /***
     * Remove the node from the connection list
     * @param connection connection to remove
     * @return "OK" if removed
     */
    private String terminate(String connection){
        CONNECTIONS_LIST.remove(connection);
        System.out.println("[INFO]: Successfully removed connection!");
        showNode();
        return "OK";
    }

    //============================ LISTENING THREAD ============================

    /***
     * Starts listen on given port
     * @param port Given port
     */
    private void listenSocket(int port) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            System.out.println("[INFO]: Node listens on port: " + server.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                Socket client = server.accept();
                new Thread(() -> {
                    ArrayList<String> usedList = new ArrayList<>();
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        PrintWriter out = new PrintWriter(client.getOutputStream(), true);

                        String line = in.readLine();
                        System.out.println("[CORE INFO]: Line 1: " + line);
                        String[] str = line.split(" ");

                        if (Objects.equals((str[0]), "identify")) {
                            System.out.println("[INFO]: The connection identify as node: " + str[1]);
                            usedList.addAll(Arrays.asList(str).subList(1, str.length));
                            line = in.readLine();
                            System.out.println("[CORE INFO]: Line 2: " + line);
                            str = line.split(" ");
                        }

                        switch (str[0]) {
                            case "new-connection": {
                                out.println(newConnection(str[1]));
                                break;
                            }
                            case "set-value": {
                                String[] s = str[1].split(":");
                                out.println(setValue(Integer.parseInt(s[0]), Integer.parseInt(s[1]), usedList));
                                break;
                            }
                            case "get-value": {
                                out.println(getValue(Integer.parseInt(str[1]), usedList));
                                break;
                            }
                            case "find-key": {
                                out.println(findKey(Integer.parseInt(str[1]), usedList));
                                break;
                            }
                            case "get-max": {
                                if (str.length == 1) {
                                    out.println(getMax(usedList));
                                } else {
                                    out.println(getMax(str[1], usedList));
                                }
                                break;
                            }
                            case "get-min": {
                                if (str.length == 1) {
                                    out.println(getMin(usedList));
                                } else {
                                    out.println(getMin(str[1], usedList));
                                }
                                break;
                            }
                            case "new-record": {
                                String[] s = str[1].split(":");
                                out.println(newRecord(s[0], s[1]));
                                break;
                            }
                            case "terminate": {
                                if (str.length == 1) {
                                    out.println(terminate(usedList));
                                    System.exit(0);
                                } else {
                                    out.println(terminate(str[1]));
                                }
                                break;
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //============================ SENDER ============================

    /***
     * Connects to other node to assign a task (recursion)
     * @param connection Given node to connect
     * @param task Given task
     * @param usedNodesList List of nodes that responded to this task
     * @return Returns respond from connected node
     */
    private String sender(String connection, String task, ArrayList<String> usedNodesList){

        String returnStr = "ERROR";
        System.out.println("[INFO]: Trying connect to: " + connection);

        StringBuilder list = new StringBuilder();
        for (String s : usedNodesList) {
            list.append(" ").append(s);
        }

        String[] strings = connection.split(":");

        try {
            Socket socket = new Socket(strings[0], Integer.parseInt(strings[1]));

            InputStream inputStream = socket.getInputStream();
            Scanner scanner = new Scanner(inputStream);

            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printwriter = new PrintWriter(outputStream, true);

            printwriter.println("identify localhost:" + PORT + list);
            printwriter.println(task);
            returnStr = scanner.nextLine();
        } catch (IOException e) {
            System.out.println("[WARNING]: Connection to: " + connection + " has failed!");
            System.out.println("[WARNING]: The connection has been canceled.");
        }

        return returnStr;
    }
    /***
     * Connects to other node to assign a task (not recursion)
     * @param connection Given node to connect
     * @param task Given task
     * @return Returns respond from connected node
     */
    private String sender(String connection, String task){

        String returnStr = "ERROR";
        System.out.println("[INFO]: Trying connect to: " + connection);

        String[] strings = connection.split(":");

        try {
            Socket socket = new Socket(strings[0], Integer.parseInt(strings[1]));

            InputStream inputStream = socket.getInputStream();
            Scanner scanner = new Scanner(inputStream);

            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printwriter = new PrintWriter(outputStream, true);

            printwriter.println(task);
            returnStr = scanner.nextLine();
        } catch (IOException e) {
            System.out.println("[WARNING]: Connection to: " + connection + " has failed!");
            System.out.println("[WARNING]: The connection has been canceled.");
        }

        return returnStr;
    }

}