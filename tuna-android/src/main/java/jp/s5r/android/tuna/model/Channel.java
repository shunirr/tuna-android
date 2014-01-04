package jp.s5r.android.tuna.model;

import lombok.Data;

@Data
public class Channel {
    private int id;
    private String name;
    private String topic;
    private Network network;

    public Channel(int id, String name, String topic, Network network) {
        this.id = id;
        this.name = name;
        this.topic = topic;
        this.network = network;
    }
}
