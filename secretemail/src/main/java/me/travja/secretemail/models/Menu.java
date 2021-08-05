package me.travja.secretemail.models;

import lombok.Getter;
import lombok.Setter;
import me.travja.secretemail.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Menu {

    @Getter
    private List<Option> options = new ArrayList<>();

    @Getter
    @Setter
    private String prompt;

    @Getter
    @Setter
    private boolean allowExit = true;

    /**
     * Presents the menu and returns the integer selection of the option selected
     *
     * @return
     */
    public Optional<Option> present() {

        boolean valid;
        int input;

        for (int i = 0; i < options.size(); i++) {
            Option opt = options.get(i);
            System.out.println((i + 1) + ". " + opt.getText());
        }

        if (allowExit)
            System.out.println("\n0. Exit");

        do {
            input = Util.getInt("Enter your selection: ");
            valid = input <= options.size() && input >= (allowExit ? 0 : 1);

            if (!valid) System.out.println("Invalid input. Please try again.");
        } while (!valid);

        if (input == 0)
            return Optional.empty();

        return Optional.of(options.get(input - 1));
    }

    public Menu addOption(Option option) {
        options.add(option);
        return this;
    }

    public void removeOption(Option option) {
        options.remove(option);
    }

    public void removeOption(int index) {
        options.remove(index);
    }

}
