package jp.s5r.android.tuna.service;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import jp.s5r.android.tuna.model.Log;
import jp.s5r.android.tuna.util.L;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TunaClient extends WebSocketClient {

    private String mPassword;

    public interface OnMessageListener {
        void onMessage(Log log);
    }

    private List<OnMessageListener> mMessageListenerList =
            new ArrayList<OnMessageListener>();

    public TunaClient(URI serverURI) {
        super(serverURI);
        L.trace();
    }

    public TunaClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
        L.trace();
    }

    public TunaClient(URI serverUri, Draft draft, Map<String, String> headers, int connectTimeout) {
        super(serverUri, draft, headers, connectTimeout);
        L.trace();
    }

    public void connect(String password) {
        mPassword = password;
        super.connect();
    }

    public void addOnMessageListener(OnMessageListener listener) {
        if (!mMessageListenerList.contains(listener)) {
            mMessageListenerList.add(listener);
        }
    }

    public boolean removeOnMessageListener(OnMessageListener listener) {
        return mMessageListenerList.remove(listener);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        L.trace();
        if (!TextUtils.isEmpty(mPassword)) {
            getConnection().send(mPassword);
        }
    }

    @Override
    public void onMessage(String s) {
        String message = unescapeMessage(s);
        Gson gson = new Gson();
        Log log = null;
        try {
            log = gson.fromJson(message, Log.class);
        } catch (JsonSyntaxException e) {
            L.e(e);
        }
        if (log != null) {
            for (OnMessageListener listener : mMessageListenerList) {
                listener.onMessage(log);
            }
        }
    }

    private String unescapeMessage(String s) {
        s = Pattern.compile("^\"").matcher(s).replaceFirst("");
        s = Pattern.compile("\"$").matcher(s).replaceFirst("");
        s = StringEscapeUtils.unescapeJava(s);
        return s;
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        L.trace();
    }

    @Override
    public void onError(Exception e) {
        L.e(e);
    }
}
