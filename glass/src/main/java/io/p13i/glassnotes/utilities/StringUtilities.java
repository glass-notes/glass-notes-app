package io.p13i.glassnotes.utilities;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import io.p13i.glassnotes.exceptions.GlassNotesRuntimeException;

public class StringUtilities {
    public static String readInputStream(InputStream inputStream) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        String line;

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();


        return stringBuilder.toString();
    }

    public static String base64Decode(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        byte[] decodedBytes = Base64.decode(string, Base64.DEFAULT);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public static String base64EncodeToString(String string) {
        byte[] data = string.getBytes(StandardCharsets.UTF_8);
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static String sha(String string) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new GlassNotesRuntimeException(e);
        }
        byte[] hash = digest.digest(string.getBytes(StandardCharsets.UTF_8));
        return new String(hash);
    }

    public static List<Character> asList(String string) {
        List<Character> characterList = new ArrayList<Character>(string.length());
        for (int i = 0; i < string.length(); i++) {
            characterList.add(string.charAt(i));
        }
        return characterList;
    }

    public static String asString(Iterable<Character> characters) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Character c : characters) {
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
}
