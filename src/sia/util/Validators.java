package sia.util;

import java.util.regex.Pattern;

public final class Validators {
    private Validators() {}
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$").matcher(email).matches();
    }
    
    public static boolean isValidDate(String s) {
        return s != null && Pattern.matches("\\d{4}-\\d{2}-\\d{2}", s);
    }

    public static boolean isValidTime(String s) {
        if (s == null || !Pattern.matches("^\\d{2}:\\d{2}$", s)) {
            return false;
        }
        String[] parts = s.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours >= 0 && hours < 24 && minutes >= 0 && minutes < 60;
    }
    
    public static boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty() && id.length() <= 20;
    }
    
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() <= 100;
    }
}