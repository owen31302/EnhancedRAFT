import java.net.Socket;
import java.util.HashMap;

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
        hostList = new HashMap<>();
        hostList.put(hostName, myAddress);
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
}
