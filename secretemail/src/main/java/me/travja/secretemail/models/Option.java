package me.travja.secretemail.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Option {

    @Getter
    @Setter
    private String text;
    @Getter
    @Setter
    private Action action;

    public interface Action {
        void run();
    }

}