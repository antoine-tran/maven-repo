package de.l3s.gossen.burstdetection;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Utils {

    private Utils() { }

    public static String cleanFilename(String term) {
        return term.replaceAll("\\W", "_");
    }

    public static boolean all(int[] values, int comp) {
        for (int value : values) {
            if (value != comp) {
                return false;
            }
        }
        return true;
    }

    public static String cleanTerm(String term) {
        return term.toLowerCase().trim().replaceAll("[^\\w\\d]", " ")
                .replaceAll("\\s{2,}", " ");
    }

    static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "";
        }
    }

    public static long sum(int[] array) {
        long result = 0;
        for (int elem : array) {
            result += elem;
        }
        return result;
    }


    public static long sum(long[] array) {
        long result = 0;
        for (long elem : array) {
            result += elem;
        }
        return result;
    }

}
