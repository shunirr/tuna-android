package jp.s5r.android.tuna.model;

import lombok.Data;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

@Data
public class Log {
    private String uuid;
    private String command;
    private String from;
    private String message;
    private long created_at;
    private Channel channel;

    public String getPlainMessage() {
        String s = this.message;
        s = Pattern.compile("<[^>]+>").matcher(s).replaceAll("");
        s = StringEscapeUtils.unescapeHtml4(s);
        return s;
    }

    public String getFormatCreatedAt() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        return sdf.format(new Date(created_at * 1000));
    }
}
