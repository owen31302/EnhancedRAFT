package host;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

/**
 * Created by owen on 5/26/17.
 */
public class Client {
    static private ArrayList<HostAddress> serverInfos;
    static public void main(String args[]) {
        // instruction goes like
        // instruction string first
        // corresponding content
        // waiting for ACK
        // if not received, we might need to send the instruction to another host
        Scanner in = new Scanner(System.in);
        String userInput;

        while (true) {
            System.out.print("Client => ");
            userInput = in.nextLine();
            userInput.trim();
            String cmdCode = decodeCommand(userInput);
            if (Objects.equals(cmdCode, "add")) {
                serverInfos = addMultipleParser(userInput);
                if (serverInfos.isEmpty()) {
                    System.out.println("no host has added");
                    continue;
                }
                HostAddress s = serverInfos.get(0);
                try{
                    Socket socket = new Socket(s.getHostIp(), s.getHostPort());
                    ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                    outStream.flush();
                    outStream.writeInt(Protocol.ADDHOSTADDRESS);
                    outStream.flush();
                    outStream.writeObject(serverInfos);
                    outStream.flush();
                    ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
                    if(inStream.readInt() != Protocol.ACKOWLEDGEMENT){
                        System.out.print("ACK NOT RECEIVED\n");
                        // maybe need to try again
                    }
                    socket.close();
                }catch (IOException e){
                    System.out.println("Please check the server is active or key in the correct address and port.");
                    System.out.print("Failed on server ");
                    System.out.print(s.getHostIp());
                    System.out.print(", port number ");
                    System.out.print(s.getHostPort());
                    System.out.println(".");
                }
            }else if (Objects.equals(cmdCode, "byzantineenable")) {
                HostAddress leader = findLeader();
                if (leader == null) {
                    System.out.println("currently no leader");
                    continue;
                }
                try{
                    Socket socket = new Socket(leader.getHostIp(), leader.getHostPort());
                    ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
                    outStream.flush();
                    outStream.writeInt(Protocol.EnableByzantine);
                    outStream.flush();
                    int waitingForACK = inStream.readInt();
                    if(waitingForACK != Protocol.ACKOWLEDGEMENT){
                        System.out.print("ACK NOT RECEIVED\n");
                        // maybe need to try again
                    }
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }else if (Objects.equals(cmdCode, "byzantinedisable")) {
                HostAddress leader = findLeader();
                if (leader == null) {
                    System.out.println("currently no leader");
                    continue;
                }
                try{
                    Socket socket = new Socket(leader.getHostIp(), leader.getHostPort());
                    ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
                    outStream.flush();
                    outStream.writeInt(Protocol.DisableByzantine);
                    outStream.flush();
                    int waitingForACK = inStream.readInt();
                    if(waitingForACK != Protocol.ACKOWLEDGEMENT){
                        System.out.print("ACK NOT RECEIVED\n");
                        // maybe need to try again
                    }
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else if (Objects.equals(cmdCode, "quit")) {
                // no need to do this
                // could just ctrl + C
                break;
            }else if (Objects.equals(cmdCode, "changeValue")) {
                // input goes like
                // changevalue <state name> <new value>
                userInput.trim();
                String[] inputParts = userInput.split(" ");
                int newValue;
                if (inputParts.length != 3) {
                    System.out.println("Please input: changeValue <state name> <new value>");
                    continue;
                }
                try {
                    newValue = Integer.valueOf(inputParts[2]);
                }catch (NumberFormatException e) {
                    System.out.println("please enter a integer number");
                    continue;
                }
                if ((inputParts[1] == null) || Objects.equals(inputParts[1], " ")) {
                    inputParts[1] = "default";
                }
                HostAddress leader = findLeader();
                if (leader == null) {
                    System.out.println("currently no leader");
                    continue;
                }
                try{
                    Socket socket = new Socket(leader.getHostIp(), leader.getHostPort());
                    ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
                    outStream.flush();
                    outStream.writeInt(Protocol.CHANGEVALUE);
                    outStream.flush();
                    outStream.writeObject(inputParts[1]);
                    outStream.flush();
                    outStream.writeInt(newValue);
                    outStream.flush();
                    int waitingForACK = inStream.readInt();
                    if(waitingForACK != Protocol.ACKOWLEDGEMENT){
                        System.out.print("ACK NOT RECEIVED\n");
                        // maybe need to try again
                    }
                    try {
                        String receivedMessage = (String)inStream.readObject();
                        if (Objects.equals(receivedMessage.toLowerCase(), "yes")) {
                            System.out.println("commit successed");
                        }else if (Objects.equals(receivedMessage.toLowerCase(), "no")){
                            System.out.println("commit rejected");
                        }else {

                        }

                    }catch (ClassNotFoundException x) {
                        System.out.println("received is not String object");
                    }

                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else {
                System.out.println("invalid command");
            }
        }


    }
    private static HostAddress findLeader() {
        // ask for leader ip and port
        // wait for host to reply
        HostAddress leader = null;
        String receivedLear = null;
        if ((serverInfos == null) || serverInfos.isEmpty()) {
            return null;
        }
        for (HostAddress s : serverInfos) {
            Socket socket = null;
            try {
                socket = new Socket(s.getHostIp(), s.getHostPort());
            } catch (IOException e) {
                System.out.println("can not set up socket");
                continue;
            }
            try {
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
                outStream.writeInt(Protocol.REQUESTLEADERADDRESS);
                outStream.flush();
                receivedLear = (String)inStream.readObject();
                socket.close();
                // success get a leader
                break;
            } catch (IOException e) {
                // request timeout
                System.out.print("request to ");
                System.out.print(s.getHostIp());
                System.out.println(" timeout");
            }catch (ClassNotFoundException e) {
                System.out.println("received a invalid string object");
            }

        }
        if (receivedLear == null) {
            System.out.println("cant find a leader");
            return null;
        }
        for (HostAddress s: serverInfos) {
            if (Objects.equals(s.getHostIp(), receivedLear)) {
                leader = s;
                break;
            }
        }
        return leader;
    }

    private static String decodeCommand(String input) {
        if (input.length() >= 3 && input.substring(0, 3).equals("add")){
            return "add";
        }else if (input.length() >= "byzantineenable".length() && input.substring(0, "byzantineenable".length()).toLowerCase().equals("byzantineenable")){
            return "byzantineenable";
        }else if (input.length() >= "byzantinedisable".length() && input.substring(0, "byzantinedisable".length()).toLowerCase().equals("byzantinedisable")){
            return "byzantinedisable";
        }else if (input.length() >= "quit".length() && input.substring(0, "quit".length()).toLowerCase().equals("quit")){
            return "quit";
        }else if (input.length() >= "changevalue".length() && input.substring(0, "changevalue".length()).toLowerCase().equals("changevalue")){
            return "changeValue";
        }else{
            return "";
        }
    }

    private static ArrayList<HostAddress> addMultipleParser(String str){
        ArrayList<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile("\\((.*?)\\)").matcher(str);
        while(m.find()) {
            matches.add(m.group(1));
        }

        ArrayList<HostAddress> result = new ArrayList<>();
        StringBuilder ip;
        StringBuilder port;
        for(String s : matches){
            ip = new StringBuilder();
            port = new StringBuilder();
            if(addParser(s, ip, port)){
                result.add(new HostAddress(ip.toString(), Integer.parseInt(port.toString())));
            }
        }
        return result;
    }

    private static boolean addParser(String s, StringBuilder ip, StringBuilder port){
        String[] partsOfOneBlock = s.split(",");
        for(int i = 0; i<partsOfOneBlock.length; i++){
            partsOfOneBlock[i] = partsOfOneBlock[i].trim();
        }
        // check three parts
        // check ip addr format
        // check port range
        if(partsOfOneBlock.length == 2){
            String[] ipBlock = partsOfOneBlock[0].split("\\.");
            for(int i = 0; i < ipBlock.length; i++){
                ipBlock[i] = ipBlock[i].trim();
            }
            if(ipBlock.length != 4){
                System.out.print("Please key in the correct format xxx.xxx.xxx.xxx .\n");
                return false;
            }
            for (int i = 0; i < 4; i++) {
                int value = Integer.parseInt(ipBlock[i]);
                if (value < 0 || value > 255) {
                    return false;
                }
            }

            int portNumber = Integer.parseInt(partsOfOneBlock[1]);
            if(!(portNumber >= 1024 && portNumber <= 65535)){
                System.out.print("You have to set port within 1024 to 65535.\n");
                return false;
            }
            ip.delete(0, ip.length());
            ip.append(partsOfOneBlock[0]);
            port.delete(0, port.length());
            port.append(partsOfOneBlock[1]);
            return true;
        }else{
            System.out.print("Please enter in the correct format add(ip, port).\n");
        }
        return false;
    }
}
