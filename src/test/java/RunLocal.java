import com.yobravo.App;

/**
 * to be accepted by weChat server,  the port need to be 80 o 443 in your linux box
 * local testing can be any port number. you pack jar file, can run by below command
 * java -Dport=port443or80 -DappId=yourWxAppId -DappSecret=yourWxAppSever -DserverToken=yourServerToken -jar yourPackedJarFile.jar
 * locally just run below main file
 */
public class RunLocal {
    public static void main(String[] args) {
        //port 80 or 443 need root access
        System.setProperty("port", "8080");
        System.setProperty("appId", "yourWxAppId");
        System.setProperty("appSecret", "yourWxAppSever");
        System.setProperty("serverToken", "yourServerToken");
        App.main(null);
    }

}
