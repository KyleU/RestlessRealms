package restless.realms.server.commerce;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * SocialGold Client
 * <p>
 * 
 * methods that make a call to the service: - getBalance - getTransactionStatus - getTransaction - creditUser - debitUser - refundUser - buyGoods
 * 
 * methods that generate a URL for merchant to render in a page: - getBuyPaymentsURL - getBuyGoodsURL
 */

public class SocialGoldClient {
    private String secretMerchantKey = null;
    private String serverName = null;

    public SocialGoldClient(String serverName, String secretMerchantKey) {
        this.secretMerchantKey = secretMerchantKey;
        this.serverName = serverName;
    }

    public String getBuyCurrencyURL(String offerId, String userID) throws Exception {
        Hashtable<String, Object> baseParams = new Hashtable<String, Object>();
        baseParams.put("action", "buy_with_socialgold");
        baseParams.put("offer_id", offerId);
        baseParams.put("user_id", userID);
        baseParams.put("format", "iframe"); // TODO: validate format is valid
        Hashtable<String, Object> requiredParams = new Hashtable<String, Object>();
        //requiredParams.put("currency_label", currency_label);
        Hashtable<String, Object> optionalParams = new Hashtable<String, Object>();

        String cmdURL = getURL(offerId, baseParams, requiredParams, optionalParams, true);
        return cmdURL;
    }

    public String getBuyGoodsURL(String offerId, String userID) throws Exception {
        Hashtable<String, Object> baseParams = new Hashtable<String, Object>();
        baseParams.put("action", "buy_goods_with_socialgold");
        baseParams.put("offer_id", offerId);
        baseParams.put("user_id", userID);
        baseParams.put("format", "iframe"); // TODO: validate format is valid
        Hashtable<String, Object> requiredParams = new Hashtable<String, Object>();
//        requiredParams.put("amount", usdAmount);
//        requiredParams.put("title", title);
        Hashtable<String, Object> optionalParams = new Hashtable<String, Object>();
        String cmdURL = getURL(offerId, baseParams, requiredParams, optionalParams, true);
        return cmdURL;
    }

    @SuppressWarnings("unchecked")
    private String getURL(String offerId, Hashtable<String, Object> baseParams, Hashtable<String, Object> requiredParams, Hashtable<String, Object> optionalParams, Boolean requiresSSL) {
        String userID = baseParams.get("user_id").toString();
        String action = baseParams.get("action").toString();
        String format = baseParams.get("format").toString();
        long timestamp = System.currentTimeMillis() / 1000;

        Hashtable<String, Object> signatureParams = (Hashtable<String, Object>)baseParams.clone();
        signatureParams.remove("format"); // format is NOT in signature
        signatureParams.remove("action"); // action is NOT in signature
        signatureParams.put("ts", timestamp);
        mergeParams(requiredParams, signatureParams);
        mergeParams(optionalParams, signatureParams);

        String signature = calculateSignature(signatureParams);
        StringBuffer uri = new StringBuffer("/payments/v1/" + offerId + "/" + action + "/?sig=" + signature + "&ts=" + timestamp + "&format=" + format + "&user_id=" + userID);
        uri.append(paramHashToURI(requiredParams));
        uri.append(paramHashToURI(optionalParams));

        String proto = (requiresSSL) ? "https://" : "http://";
        String url = proto + this.serverName + uri.toString();
        return url;
    }

    private void mergeParams(Hashtable<String, Object> srcParams, Hashtable<String, Object> destParams) {
        if(srcParams == null) {
            destParams = new Hashtable<String, Object>();
        }
        if(srcParams != null) {
            String key = null;
            for(Enumeration<String> keys = srcParams.keys(); keys.hasMoreElements();) {
                key = keys.nextElement();
                destParams.put(key, srcParams.get(key));
            }
        }
    }

    private String paramHashToURI(Hashtable<String, Object> params) {
        StringBuffer result = new StringBuffer();
        if(params != null) {
            String key = null;
            Object value = null;
            for(Enumeration<String> keys = params.keys(); keys.hasMoreElements();) {
                key = keys.nextElement();
                value = params.get(key);
                if(value != null && (value = value.toString().trim()) != "") {
                    String v = "";
                    try {
                        v = URLEncoder.encode(value.toString(), "UTF-8");
                        result.append("&" + key.toString() + "=" + v);
                    } catch(Exception e) {
                    }
                }
            }
        }
        return result.toString();
    }

    private String calculateSignature(Hashtable<String, Object> signatureParams) {
        String signature = null;
        StringBuffer sigSrc = new StringBuffer();
        if(signatureParams != null) {
            Object key = null;
            Object value = null;

            // System.out.println("sigParams = |"+signatureParams+"|");

            Vector<String> keyVector = new Vector<String>(signatureParams.keySet());
            Collections.sort(keyVector);

            for(Enumeration<String> keys = keyVector.elements(); keys.hasMoreElements();) {
                key = keys.nextElement();
                value = signatureParams.get(key);
                if(value != null) {
                    sigSrc.append(key.toString() + value.toString());
                }
            }
        }

        sigSrc.append(this.secretMerchantKey);
        String sigSrcString = sigSrc.toString();
        // System.out.println("sigSrc = |"+sigSrcString+"|"); // WARNING - shows secretMerchantKey - for debug only!!!

        try {
            byte[] md5hash = MessageDigest.getInstance("MD5").digest(sigSrcString.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder(md5hash.length * 2);
            for(byte b : md5hash) {
                if((b & 0xff) < 0x10)
                    hexString.append("0");
                hexString.append(Long.toString(b & 0xff, 16));
            }

            signature = hexString.toString();
            // System.out.println("signature = |"+signature+"|");

        } catch(Exception e) {
            System.err.println("MD5 related exception" + e);
            signature = "";
        }

        return signature;
    }
}
