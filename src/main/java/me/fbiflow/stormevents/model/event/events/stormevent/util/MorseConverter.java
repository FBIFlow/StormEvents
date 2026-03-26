package me.fbiflow.stormevents.model.event.events.stormevent.util;

import java.util.Map;

public class MorseConverter {
    private static final Map<Character, String> morseCodeMap = Map.of(
            '0', "_____",
            '1', ".____",
            '2', "..___",
            '3', "...__",
            '4', "...._",
            '5', ".....",
            '6', "_....",
            '7', "__...",
            '8', "___..",
            '9', "____."
    );

    public static String convertToMorse(int number) {
        String numberStr = String.valueOf(Math.abs(number));
        StringBuilder morseCode = new StringBuilder();
        if (number < 0) {
            morseCode.append("- ");
        }
        for (char d : numberStr.toCharArray()) {
            if (morseCodeMap.containsKey(d)) {
                morseCode.append(morseCodeMap.get(d)).append(" ");
            }
        }
        return morseCode.toString().trim();
    }
}
