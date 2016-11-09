package restless.realms.server.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class WebUtils {

    public static String getUrlContents(String urlString) {
        StringBuilder ret = new StringBuilder();
        try {
            URL url = null;
            try {
                url = new URL(urlString);
            } catch(MalformedURLException e) {
                e.printStackTrace();
            }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
    
            String inputLine;
            
            boolean first = true;
            while ((inputLine = in.readLine()) != null) {
                if(first) {
                    first = false;
                } else {
                    ret.append("\n");
                }
                ret.append(inputLine);
            }
    
            in.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return ret.toString();
    }
}
