package host;

import java.util.ArrayList;
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
    static public void main(String args[]) {

        Scanner in = new Scanner(System.in);
        String userInput;
        while (true) {
            System.out.print("Client => ");
            userInput = in.nextLine();
            userInput.trim();
            String cmdCode = decodeCommand(userInput);
            if (cmdCode == "add") {
                ArrayList<HostAddress> serverInfos = addMultipleParser(userInput);
                String[] allHostInfo = new String[serverInfos.size()];
                // send host address to all host, unencrypted
                // message is String type, format goes like:
                // instruction code
                // host addresses count number
                // send host information repeatedly
                //      hostIP, hostPort
                for (int i = 0; i < allHostInfo.length; i ++) {
                    allHostInfo[i] = new String();
                    allHostInfo[i] += serverInfos.get(i).getHostIp();
                    allHostInfo[i] += ", ";
                    allHostInfo[i] += serverInfos.get(i).getHostPort();
                }

                //
                for(HostAddress s : serverInfos){
                    try{
                        Socket socket = new Socket(s.getHostIp(), s.getHostPort());
                        ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
                        outStream.writeChars("AddHostAddresses");
                        outStream.flush();
                        outStream.writeInt(allHostInfo.length);
                        outStream.flush();
                        for (int i = 0; i < allHostInfo.length; i ++) {
                            outStream.writeChars(allHostInfo[i]);
                            outStream.flush();
                        }
                        int waitingForACK = inStream.readInt();
                        if(waitingForACK != ClientInstructionCode.Ackowledgement){
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
                        e.printStackTrace();
                    }
                }
            }else if (cmdCode == "byzantineenable") {
                HostAddress leader = findLeader();
                try{
                    Socket socket = new Socket(leader.getHostIp(), leader.getHostPort());
                    ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
                    outStream.writeChars("byzantineEnable");
                    outStream.flush();
                    int waitingForACK = inStream.readInt();
                    if(waitingForACK != ClientInstructionCode.Ackowledgement){
                        System.out.print("ACK NOT RECEIVED\n");
                        // maybe need to try again
                    }
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }else if (cmdCode == "byzantinedisable") {
                HostAddress leader = findLeader();
                try{
                    Socket socket = new Socket(leader.getHostIp(), leader.getHostPort());
                    ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
                    outStream.writeChars("byzantineDisable");
                    outStream.flush();
                    int waitingForACK = inStream.readInt();
                    if(waitingForACK != ClientInstructionCode.Ackowledgement){
                        System.out.print("ACK NOT RECEIVED\n");
                        // maybe need to try again
                    }
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else if (cmdCode == "quit") {
                // no need to do so
            }else {
                System.out.println("invalid command");
            }
        }


    }
    static private HostAddress findLeader() {
            return null;
    }

    static private String decodeCommand(String input) {
        if (input.length() >= 3 && input.substring(0, 3).equals("add")){
            return "add";
        }else if ( input.length() >= "byzantineenable".length() && input.substring(0, "byzantineenable".length()).toLowerCase().equals("byzantineenable")){
            return "byzantineenable";
        }else if ( input.length() >= "byzantinedisable".length() && input.substring(0, "byzantinedisable".length()).toLowerCase().equals("byzantinedisable")){
            return "byzantinedisable";
        }else if ( input.length() >= "quit".length() && input.substring(0, "quit".length()).equals("quit")){
            return "quit";
        }else{
            return "";
        }
    }

    private static ArrayList<HostAddress> addMultipleParser(String str){
        ArrayList<String> matches = new ArrayList<String>();
        Matcher m = Pattern.compile("\\((.*?)\\)").matcher(str);
        while(m.find()) {
            matches.add(m.group(1));
        }

        ArrayList<HostAddress> result = new ArrayList<HostAddress>();
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
        if(partsOfOneBlock.length == 3){

            String[] ipBlock = partsOfOneBlock[1].split("\\.");
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

            int portNumber = Integer.parseInt(partsOfOneBlock[2]);
            if(!(portNumber >= 1024 && portNumber <= 65535)){
                System.out.print("You have to set port within 1024 to 65535.\n");
                return false;
            }

            ip.delete(0, ip.length());
            ip.append(partsOfOneBlock[1]);
            port.delete(0, port.length());
            port.append(partsOfOneBlock[2]);
            return true;
        }else{
            System.out.print("Please key in the correct format add(hostname, ip, port).\n");
        }
        return false;
    }
}
