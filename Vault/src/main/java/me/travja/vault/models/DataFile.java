package me.travja.vault.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.travja.vault.App;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class DataFile {

    @Getter
    @JsonIgnore
    public final File file;
    @Getter
    @Setter
    public List<DataEntry> data = new ArrayList<>();

    @Getter
    @Setter
    public String masterPassword;

    public DataFile() {
        this.file = new File("taxes.dat");
    }

    public DataFile(File file) {
        this.file = file;
        if (!file.exists())
            return;
        try {
            DataFile df = App.getMapper().readValue(file, this.getClass());
            if (df != null) {
                this.data = df.getData();
                this.masterPassword = df.getMasterPassword();
            } else
                System.err.println("Data file is null");
        } catch (IOException e) {
            System.err.println("Couldn't read object from file '" + file.getName() + "'");
            e.printStackTrace();
        }

    }

    public void addEntry(DataEntry entry) {
        data.add(entry);
        App.getInstance().updateList();
    }

    public DataEntry getEntry(String id) {
        return data.stream().filter(d -> d.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public void save() {
        try {
            App.getMapper().writeValue(file, this);
        } catch (IOException e) {
            System.err.println("Could not write data to file.");
            e.printStackTrace();
        }
    }

}
