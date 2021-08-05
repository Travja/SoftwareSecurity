package me.travja.vault.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.travja.vault.App;

@NoArgsConstructor
public class DataEntry {

    @Getter
    @Setter
    private String id, username, password, url;

    public DataEntry(String id, String username, String password, String url) {
        this(id, username, password, url, true);
    }

    public DataEntry(String id, String username, String password, String url, boolean raw) {
        if (raw)
            this.password = App.encryptPassword(password);
        else
            this.password = password;
        this.id = id;
        this.username = username;
        this.url = url;
    }

    public String decodedPassword() {
        return App.decryptPassword(password);
    }


}
