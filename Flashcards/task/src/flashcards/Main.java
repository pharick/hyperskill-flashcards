package flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class Cards {

    private Map<String, String> data = new HashMap<>();
    private Map<String, Integer> mistakes = new HashMap<>();

    boolean add(String term, String definition) {
        if (!data.containsKey(term) && !data.containsValue(definition)) {
            data.put(term, definition);
            mistakes.put(term, 0);
            return true;
        }
        return false;
    }

    boolean remove(String term) {
        if (data.containsKey(term)) {
            data.remove(term);
            mistakes.remove(term);
            return true;
        }
        return false;
    }

    int size() {
        return data.size();
    }

    boolean containsTerm(String term) {
        return data.containsKey(term);
    }

    boolean containsDefinition(String term) {
        return data.containsValue(term);
    }

    void serialize(File file) throws IOException {
        FileWriter writer = new FileWriter(file);

        for (var card : data.entrySet()) {
            writer.write(card.getKey() + "\n" + card.getValue() + "\n" + mistakes.get(card.getKey()) + "\n");
        }

        writer.close();
    }

    int deserialize(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        String term;
        String definition;
        int m;
        int count = 0;

        while (scanner.hasNext()) {
            term = scanner.nextLine();
            definition = scanner.nextLine();
            m = Integer.parseInt(scanner.nextLine());

            data.put(term, definition);
            mistakes.put(term, m);
            count++;
        }

        return count;
    }

    Map<String, String> getInverse() {
        Map<String, String> inverse = new HashMap<>();

        for (var entry : data.entrySet()) {
            inverse.put(entry.getValue(), entry.getKey());
        }

        return inverse;
    }


    void addMistake(String term) {
        if (mistakes.containsKey(term)) {
            mistakes.put(term, mistakes.get(term) + 1);
        }
    }

    Map<String, Integer> getMistakes() {
        return mistakes;
    }

    void clearMistakes() {
        for (var card : mistakes.entrySet()) {
            card.setValue(0);
        }
    }
}

public class Main {

    final static Cards cards = new Cards();
    final static ArrayList<String> log = new ArrayList<>();
    static String importPath = "";
    static String exportPath = "";

    public static void main(String[] args) {
        processArgs(args);

        if (importPath.length() > 0) {
            importCards(importPath);
        }

        String action;
        boolean exit = false;

        while (!exit) {
            write("Input the action (add, remove, import, export, ask, hardest card, reset stats, log, exit):");
            action = read();

            switch (action) {
                case "add":
                    addCard();
                    break;
                case "remove":
                    removeCard();
                    break;
                case "export":
                    exportCards();
                    break;
                case "import":
                    importCards();
                    break;
                case "ask":
                    ask();
                    break;
                case "hardest card":
                    hardest();
                    break;
                case "reset stats":
                    resetStats();
                    break;
                case "log":
                    log();
                    break;
                case "exit":
                    exit = true;
                    write("Bye bye!");

                    if (exportPath.length() > 0) {
                        exportCards(exportPath);
                    }

                    break;
            }

            System.out.println();
        }
    }

    private static void processArgs(String[] args) {
        String command1 = "";
        String path1 = "";
        String command2 = "";
        String path2 = "";

        if (args.length >= 2) {
            command1 = args[0];
            path1 = args[1];
        }
        if (args.length >= 4) {
            command2 = args[2];
            path2 = args[3];
        }

        if ("-import".equals(command1)) {
            importPath = path1;
        } else if ("-import".equals(command2)) {
            importPath = path2;
        }

        if ("-export".equals(command1)) {
            exportPath = path1;
        } else if ("-export".equals(command2)) {
            exportPath = path2;
        }
    }

    private static void resetStats() {
        cards.clearMistakes();
        write("Card statistics has been reset.");
    }

    private static void hardest() {
        Map<String, Integer> mistakes = cards.getMistakes();
        int max = 0;

        for (var card : mistakes.entrySet()) {
            if (card.getValue() > max) {
                max = card.getValue();
            }
        }

        ArrayList<String> cards = new ArrayList<>();

        if (max > 0) {
            for (var card : mistakes.entrySet()) {
                if (card.getValue() == max) {
                    cards.add(card.getKey());
                }
            }
        }

        if (cards.size() > 1) {
            String line = "The hardest cards are ";

            for (int i = 0; i < cards.size(); i++) {
                line += "\"" + cards.get(i) + "\"" + (i == cards.size() - 1 ? ". " : ", ");
            }

            line += "You have " + max + " errors answering them.";

            write(line);
        } else if (cards.size() > 0) {
            write("The hardest card is \"" + cards.get(0) + "\". You have " + max + " errors answering it.");
        } else {
            write("There are no cards with errors.");
        }
    }

    public static void addCard() {
        write("The card:");
        String term = read();

        if (cards.containsTerm(term)) {
            write("The card \"" + term + "\" already exists.");
            return;
        }

        write("The definition of the card:");
        String definition = read();

        if (cards.containsDefinition(definition)) {
            write("The definition \"" + definition + "\" already exists.");
            return;
        }

        cards.add(term, definition);
        write("The pair (\"" + term + "\":\"" + definition + "\") has been added.");
    }

    public static void removeCard() {
        write("The card:");
        String term = read();

        if (cards.remove(term)) {
            write("The card has been removed.");
        } else {
            write("Can't remove \"" + term + "\": there is no such card.");
        }
    }

    public static void exportCards(String path) {
        File file = new File(path);

        try {
            cards.serialize(file);
            write(cards.size() + " cards have been saved.");
        } catch (IOException e) {
            write("Export error.");
        }
    }

    public static void exportCards() {
        write("File name:");
        String path = read();
        exportCards(path);
    }

    public static void importCards(String path) {
        File file = new File(path);

        try {
            int count = cards.deserialize(file);
            write(count + " cards have been loaded.");
        } catch (FileNotFoundException e) {
            write("File not found.");
        }
    }

    public static void importCards() {
        write("File name:");
        String path = read();
        importCards(path);
    }

    public static void ask() {
        write("How many times to ask?");
        int count = Integer.parseInt(read());

        Map<String, String> cardsInverse = cards.getInverse();
        List<String> definitions = new ArrayList<>(cardsInverse.keySet());
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            String definition = definitions.get(random.nextInt(definitions.size()));
            String term = cardsInverse.get(definition);

            write("Print the definition of \"" + term + "\":");
            String answer = read();

            if (answer.equals(definition)) {
                write("Correct answer");
            } else {
                if (cardsInverse.containsKey(answer)) {
                    write("Wrong answer. The correct one is \"" + definition + "\"" + ", you've just written the definition of \"" + cardsInverse.get(answer) + "\".");
                } else {
                    write("Wrong answer. The correct one is \"" + definition + "\".");
                }

                cards.addMistake(term);
            }
        }
    }

    public static String read() {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine().trim();
        log.add(line);
        return line;
    }

    public static void write(String line) {
        log.add(line);
        System.out.println(line);
    }

    public static void log() {
        write("File name:");
        String path = read();
        File file = new File(path);

        try {
            FileWriter writer = new FileWriter(file);

            for (var line : log) {
                writer.write(line + "\n");
            }

            writer.close();
            write("The log has been saved.");
        } catch (IOException e) {
            write("Export error.");
        }
    }
}