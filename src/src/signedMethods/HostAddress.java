package signedMethods;

import java.security.interfaces.RSAPrivateKey;

/**
 * Created by shan on 5/26/17.
 */
public class HostAddress {

    private String hostName;
    private String hostIp;
    private int hostPort;
    private RSAPrivateKey privateKey;


    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(RSAPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public HostAddress(String hostName, String hostIp, int hostPort) {
        this.hostName = hostName;
        this.hostIp = hostIp;
        this.hostPort = hostPort;
    }

    public HostAddress(String hostName) {
        this.hostName = hostName;
    }

    public HostAddress(String hostIp, int hostPort) {
        this.hostIp = hostIp;
        this.hostPort = hostPort;
    }

    public String getHostName() {
        return hostName;
    }

    public String getHostIp() {
        return hostIp;
    }

    public int getHostPort() {
        return hostPort;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

}
