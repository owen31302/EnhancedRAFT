package host;

/**
 * Created by TC_Yeh on 6/3/2017.
 */
public class Protocol {
    public static final int ADDHOSTADDRESS = 0;
    public static final int ACKOWLEDGEMENT = 1;
    public static final int EnableByzantine = 2;
    public static final int DisableByzantine = 3;
    public static final int ASKHOSTNAME = 4;
    public static final int REPLYHOSTNAME = 5;
    public static final int UPDATEHOSTLIST = 6;
    public static final int REPLYHOSTLIST = 7;
    public static final int RPCREQUEST = 8;
    public static final int CHANGEVALUE = 9;
    public static final int REQUESTLEADERADDRESS = 10;
}
