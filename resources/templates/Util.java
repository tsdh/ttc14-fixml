package {{{pkg-name}}};

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

class Util {
    private static final SimpleDateFormat dateFormat
	= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    public static Date parseDate(String date) {
	try {
	    return dateFormat.parse(date);
	} catch (ParseException e) {
	    throw new RuntimeException(e);
	}
    }
}
