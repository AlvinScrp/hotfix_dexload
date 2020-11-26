package com.a.library;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mawenqiang on 2017/11/28.
 */

public class JSONUtils {


    public static <T> String toJSONString(T t) {

        String text = "";
        if (t == null) {
            return text;
        } else {
            try {
                text = new Gson().toJson(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return text;
    }


    public static <T> JSONObject toJSONObject(T t) {

        JSONObject object = new JSONObject();
        if (t == null) {
            return object;
        } else {
            try {
                object = new JSONObject(new Gson().toJson(t));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    public static <T> JsonObject toJsonObject(T t) {


        JsonObject object = new JsonObject();
        if (t == null) {
            return object;
        } else {
            try {
                JsonElement jelem = new Gson().fromJson(toJSONString(t), JsonElement.class);
                JsonObject jobj = jelem.getAsJsonObject();
                return  jobj;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    public static <T> T parseObject(String text, Type typeOfT) {

        if (TextUtils.isEmpty(text)) {
            return null;
        } else {
            try {
                T t = new Gson().fromJson(text, typeOfT);
                return t;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {

        if (TextUtils.isEmpty(text)) {
            return null;
        } else {
            try {
                Type type = new TypeToken<ArrayList<JsonObject>>() {
                }.getType();
                ArrayList<JsonObject> jsonObjects = new Gson().fromJson(text, type);

                ArrayList<T> arrayList = new ArrayList<>();
                for (JsonObject jsonObject : jsonObjects) {
                    arrayList.add(new Gson().fromJson(jsonObject, clazz));
                }
                return arrayList;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    public static <T> T parseObject(String text, Class<T> clzz) {
        if (TextUtils.isEmpty(text)) {
            return null;
        } else {
            try {
                T t = new Gson().fromJson(text, clzz);
                return t;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    public static <T> T parseObject(JSONObject obj, Class<T> clzz) {
        if (obj == null) {
            return null;
        }
        try {
            T t = new Gson().fromJson(obj.toString(), clzz);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static <T> T parseObject(JsonObject obj, Class<T> clzz) {
        if (obj == null) {
            return null;
        }
        try {
            T t = new Gson().fromJson(toJSONString(obj), clzz);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String optString(JSONObject jsonParams, String key) {
        if (jsonParams != null && jsonParams.has(key)) {
            return jsonParams.optString(key);
        }
        return null;
    }

    public static int optInt(JSONObject jsonParams, String key) {
        if (jsonParams != null && jsonParams.has(key)) {
            return jsonParams.optInt(key);
        }
        return 0;
    }

    public static String optString(JsonObject jsonParams, String key) {
        String value = "";
        try {
            if (jsonParams != null && jsonParams.has(key)) {
                value = jsonParams.get(key).getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }


}
