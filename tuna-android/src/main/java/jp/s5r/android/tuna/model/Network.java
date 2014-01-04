package jp.s5r.android.tuna.model;

import lombok.Data;

@Data
public class Network {
    private long id;
    private String name;

    public Network(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
