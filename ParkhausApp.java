import java.time.*;
import java.util.*;

public class ParkhausApp {
    public static void main(String[] args) {
        Parkhaus parkhaus = new Parkhaus("Zentrum", 10, 0.05); // 5 Cent pro Minute
        Scanner sc = new Scanner(System.in);

        System.out.println("Willkommen im Parkaus\n");
    }
}

class Parkhaus {
    private final String name;
    private final int kapazität;
    private int freiePlätze;
    private final Map<String, Ticket> tickets = new HashMap<>();
    private int nextId = 1; // Ticket-ID von 1 bis kapazität
}

class Ticket {
    private final String id;
    private final LocalDateTime start;
    private LocalDateTime ende; // gesetzt beim Bezahlen
    private boolean bezahlt;
}

class Zahlstation {
    private final double preisProMinute;
}

class Schranke {
    private final String name;
    public Schranke(String name) { this.name = name; }
    public void öffnen() {
        System.out.println("Schranke (" + name + ") öffnet.");
    }
}

class Anzeigetafel {
     public void anzeigen(int freie) {
        System.out.println("Freie Plätze: " + freie);
    }
}
