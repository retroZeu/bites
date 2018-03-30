package com.example.mzdoes.bites;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by zeucudatcapua2 on 3/28/18.
 */

public class Utility {

    public static void saveList(Context context, String key, List<Article> articleList) throws IOException {
        FileOutputStream fos = context.openFileOutput (key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject (articleList);
        oos.close ();
        fos.close ();
    }

    public static void saveString(Context context, String key, String toSave) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(toSave);
        oos.close();
        fos.close();
    }

    public static void saveBoolean(Context context, String key, boolean toSave) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(toSave);
        oos.close();
        fos.close();
    }

    public static List<Article> readList(Context context, String key) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput (key);
        ObjectInputStream ois = new ObjectInputStream (fis);
        List<Article> articleList = (List<Article>) ois.readObject();
        return articleList;
    }

    public static String readString(Context context, String key) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput (key);
        ObjectInputStream ois = new ObjectInputStream (fis);
        String string = (String) ois.readObject();
        return string;
    }

    public static boolean readBool(Context context, String key) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput (key);
        ObjectInputStream ois = new ObjectInputStream (fis);
        boolean theBool = (boolean) ois.readObject();
        return theBool;
    }
}
