package lvc.pro.com.free.utils;

import android.content.Context;

public class StringUtils {

    public static final String PRIVACY_POLICY = "https://megaclipgames.com/privacy-policy/";

    public static String prepareContacts(Context ctx, String number) {
        if (number != null && !number.isEmpty()) {
            String preparednumbers = number.trim();
            preparednumbers = "#" + preparednumbers;
            // preparednumbers = preparednumbers.replace("#0", "");
            preparednumbers = preparednumbers.replace("#", "");
            preparednumbers = preparednumbers.replace(" ", "");
            preparednumbers = preparednumbers.replace("(", "");
            preparednumbers = preparednumbers.replace(")", "");
            if (preparednumbers.contains("+")) {
                try {
                    //  preparednumbers = preparednumbers.replace(preparednumbers.substring(0, 3), ""); //to remove country code
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            preparednumbers = preparednumbers.replace("-", "");
            return preparednumbers;
        } else {
            return "";
        }
    }
}
