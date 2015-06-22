package jkmg.com.androidappobjectscommunicants;

import java.io.Serializable;

/**
 * Created by GuillaumeB on 21/06/2015.
 */
public class Parameter implements Serializable{

    private String hostAddr;
    private int camPort;
    private int cmdPort;

    public String getHostAddr() {
        return hostAddr;
    }

    public void setHostAddr(String hostAddr) {
        this.hostAddr = hostAddr;
    }

    public int getCamPort() {
        return camPort;
    }

    public void setCamPort(int camPort) {
        this.camPort = camPort;
    }

    public int getCmdPort() {
        return cmdPort;
    }

    public void setCmdPort(int cmdPort) {
        this.cmdPort = cmdPort;
    }
}
