package host;

import java.net.Socket;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used for manager all host in the cluster
 * Created by TC_Yeh on 5/28/2017.
 */
public class HostManager {
    private HostAddress myAddress;
    private HostAddress leaderAddress;
    private HashMap<String ,HostAddress> hostList;

    HostManager(String hostName, String ip, int port){
        myAddress = new HostAddress(hostName, ip, port);
        leaderAddress = null;
        hostList = new HashMap<String, HostAddress>();
        hostList.put(hostName, myAddress);
    }

    HostManager(HostAddress myAddress){
        this.myAddress = myAddress;
        leaderAddress = null;
        hostList = new HashMap<String, HostAddress>();
        hostList.put(myAddress.getHostName(), myAddress);
    }
    /**
     * This method add a new host's address into list.
     * If list already contains a same host name, update corresponding information
     * @param aHost a new host's address
     * @return true if no same name in the list
     */
    public boolean addHostToList(HostAddress aHost){
        //return boolean for possible future use
        if (!hostList.containsKey(aHost.getHostName())){
            hostList.put(aHost.getHostName(), aHost);
            return true;
        }
        else {
            hostList.put(aHost.getHostName(), aHost);
            return false;
        }
    }

    /**
     * This method add a new host's address into list.
     * If list already contains a same host name, update corresponding information
     * @param hostName a host name
     * @param ip ip
     * @param port port
     * @return true if no same name in the list
     */
    public boolean addHostToList(String hostName, String ip, int port){
        //return boolean for possible future use
        if (!hostList.containsKey(hostName)){
            hostList.put(hostName, new HostAddress(hostName, ip, port));
            return true;
        }
        else {
            hostList.put(hostName, new HostAddress(hostName, ip, port));
            return false;
        }
    }

    /**
     * This method add a new host's address into list.
     * If list already contains a same host name, update corresponding information
     * @param hostName host name
     * @param aSocket socket
     * @return true if no same name in the list
     */
    public boolean addHostToList(String hostName, Socket aSocket){
        String ip = aSocket.getInetAddress().toString().substring(1);
        if (!hostList.containsKey(hostName)){
            hostList.put(hostName, new HostAddress(hostName, ip, aSocket.getPort()));
            return true;
        }
        else {
            hostList.put(hostName, new HostAddress(hostName, ip, aSocket.getPort()));
            return false;
        }
    }

    /**
     * This method removes a host's address from host list.
     * @param aHost a host needed to be remove
     * @return true if the host found and removed from host list
     */
    public boolean removeHostFroList(HostAddress aHost){
        //return boolean for possible future use
        if (hostList.containsKey(aHost.getHostName())){
            hostList.remove(aHost.getHostName());
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * This method returns host list
     * @return host list
     */
    public HashMap<String, HostAddress> getHostList(){
        return hostList;
    }

    public Set<String> getHostNames(){
        return hostList.keySet();
    }

    /**
     * This method returns the name of this host
     * @return host name
     */
    public String getMyHostName(){
        return myAddress.getHostName();
    }

    /**
     * This method returns address of this host
     * @return address
     */
    public HostAddress getMyAddress(){
        return myAddress;
    }

    /**
     * This method stores the leader's address information
     * @param leader leader's address
     */
    public void setLeaderAddress(HostAddress leader){
        leaderAddress = leader;
    }

    public void setLeaderAddress(String leaderName){
        leaderAddress = hostList.get(leaderName);
    }
    /**
     * This method returns leader's information.
     * @return leader's address
     */
    public HostAddress getLeaderAddress(){
        return leaderAddress;
    }

    /**
     * This method returns leader's host name
     * @return leader's host name
     */
    public String getLeaderName(){
        return leaderAddress.getHostName();
    }

    public HostAddress getHostAddress(String hostName){
        return hostList.get(hostName);
    }

    /**
     * This method replaces entire host list with new host list
     * @param newList new host list
     */
    public void replaceHostList(HashMap<String ,HostAddress> newList){
        hostList = newList;
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        for (Map.Entry<String, HostAddress> a: hostList.entrySet()) {
            result.append("Host Name: " + a.getValue().getHostName() + " IP:" + a.getValue().getHostIp() + "\n");
        }
        return result.toString();
    }

    /**
     * This method checks if a ip, port pair in the host list
     * @param ip ip
     * @return true if in the host list
     */
    public boolean isInHostList(String ip){

        for (Map.Entry<String, HostAddress> a: hostList.entrySet()) {
            if (a.getValue().getHostIp().equals(ip)) {
                return true;
            }
        }
        return false;
    }

    public RSAPublicKey getPublicKey(String ip){
        for (Map.Entry<String, HostAddress> a: hostList.entrySet()) {
            if (a.getValue().getHostIp().equals(ip)) {
                return a.getValue().getPublicKey();
            }
        }
        return null;
    }

    public RSAPublicKey getLeaderPublicKey(){
        return leaderAddress.getPublicKey();
    }
}