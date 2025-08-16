import java.time.*;
import java.util.*;

// Hauptklasse (OOP: Klasse, Einstiegspunkt, keine Vererbung nötig)
public class ParkhausApp {
    public static void main(String[] args) {
        // Objekt-Erzeugung (OOP: Objekt, Kapselung)
        Parkhaus parkhaus = new Parkhaus("Zentrum", 10, 0.05); 
        Scanner sc = new Scanner(System.in);

        System.out.println("Willkommen im Parkhaus\n");
        boolean running = true;
        while (running) {
            // Menüausgabe
            System.out.println("--- Menü ---");
            System.out.println("1) Einfahren (Ticket ziehen)");
            System.out.println("2) Bezahlen");
            System.out.println("3) Ausfahren");
            System.out.println("4) Freie Plätze anzeigen");
            System.out.println("0) Beenden");
            System.out.print("> ");
            String input = sc.nextLine().trim();
            switch (input) {
                case "1":
                    // Ticket ziehen
                    Ticket t = parkhaus.ticketAusgeben();
                    if (t != null) {
                        System.out.println("Ticket ausgegeben: ID " + t.getId());
                        parkhaus.getEinfahrt().öffnen();
                    } else {
                        System.out.println("Parkhaus voll – kein Ticket verfügbar.");
                    }
                    break;
                case "2":
                    // Ticket bezahlen
                    System.out.print("Ticket-ID: ");
                    String idPay = sc.nextLine().trim();
                    Ticket payT = parkhaus.findeTicket(idPay);
                    if (payT == null) {
                        System.out.println("Unbekannte Ticket-ID.");
                        break;
                    }
                    double betrag = parkhaus.getZahlstation().berechnen(payT);
                    System.out.printf("Zu zahlen: %.2f CHF\n", betrag);
                    System.out.print("Jetzt bezahlen? (j/n) ");
                    if (sc.nextLine().trim().equalsIgnoreCase("j")) {
                        parkhaus.getZahlstation().bezahlen(payT);
                        parkhaus.zahlungRegistrieren(payT);
                        System.out.println("Bezahlt. Quittung: Ticket " + payT.getId());
                    }
                    break;
                case "3":
                    // Ausfahren
                    System.out.print("Ticket-ID: ");
                    String idExit = sc.nextLine().trim();
                    Ticket exitT = parkhaus.findeTicket(idExit);
                    if (exitT == null) {
                        System.out.println("Unbekannte Ticket-ID.");
                        break;
                    }
                    if (exitT.isBezahlt()) {
                        parkhaus.getAusfahrt().öffnen();
                        parkhaus.ticketBeenden(exitT); 
                        System.out.println("Gute Fahrt!");
                    } else {
                        System.out.println("Bitte zuerst bezahlen!");
                    }
                    break;
                case "4":
                    // Anzeige freie Plätze
                    parkhaus.getAnzeige().anzeigen(parkhaus.getFreiePlätze());
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Ungültige Eingabe.");
            }
            System.out.println();
        }
    }
}

// OOP: Klasse Parkhaus, Single Responsibility (S aus SOLID: Eine Verantwortung)
class Parkhaus {
    // OOP: Felder sind gekapselt (private), Kapselung
    private final String name; // Name des Parkhauses
    private final int kapazität; // Maximale Anzahl Plätze
    private int freiePlätze; // Aktuell freie Plätze
    private final Map<String, Ticket> tickets = new HashMap<>(); // Aktive Tickets
    private int nextId = 1; // Nächste Ticket-ID

    // OOP: Zusammensetzung (Objekte als Felder), Dependency Injection (D aus SOLID)
    private final Anzeigetafel anzeige = new Anzeigetafel(); // Anzeige für freie Plätze
    private final Schranke einfahrt = new Schranke("Einfahrt"); // Schranke Einfahrt
    private final Schranke ausfahrt = new Schranke("Ausfahrt"); // Schranke Ausfahrt
    private final Zahlstation zahlstation; // Zahlstation für Bezahlung

    // Konstruktor (OOP: Initialisierung, Kapselung)
    public Parkhaus(String name, int kapazität, double minutenPreis) {
        this.name = name;
        this.kapazität = kapazität;
        this.freiePlätze = kapazität;
        this.zahlstation = new Zahlstation(minutenPreis);
    }

    // Gibt ein Ticket aus, wenn Platz frei ist
    public Ticket ticketAusgeben() {
        if (freiePlätze <= 0 || nextId > kapazität) return null;
        String id = String.valueOf(nextId++);
        Ticket t = Ticket.neu(id);
        tickets.put(t.getId(), t);
        freiePlätze--;
        anzeige.anzeigen(freiePlätze);
        return t;
    }

    // Registrierung der Zahlung (optional für Statistik)
    public void zahlungRegistrieren(Ticket t) {
        
    }

    // Ticket beenden und Platz freigeben
    public void ticketBeenden(Ticket t) {
        t.beenden();
        if (freiePlätze < kapazität) freiePlätze++;
        anzeige.anzeigen(freiePlätze);
        tickets.remove(t.getId());
    }

    // Ticket anhand der ID suchen
    public Ticket findeTicket(String id) { return tickets.get(id); }

    // Anzahl freie Plätze abfragen
    public int getFreiePlätze() { return freiePlätze; }

    // Getter für Anzeige, Schranken und Zahlstation
    public Anzeigetafel getAnzeige() { return anzeige; }
    public Schranke getEinfahrt() { return einfahrt; }
    public Schranke getAusfahrt() { return ausfahrt; }
    public Zahlstation getZahlstation() { return zahlstation; }
}

// OOP: Klasse Ticket, Single Responsibility (S aus SOLID)
class Ticket {
    // OOP: Felder sind gekapselt (private)
    private final String id; // Ticket-ID
    private final LocalDateTime start; // Einfahrtszeit
    private LocalDateTime ende; // Ausfahrtszeit
    private boolean bezahlt; // Status bezahlt

    // Konstruktor (OOP: Initialisierung, Kapselung)
    private Ticket(String id) {
        this.id = id;
        this.start = LocalDateTime.now();
        this.bezahlt = false;
    }

    // Statische Fabrikmethode (OOP: Factory Pattern)
    public static Ticket neu(String id) {
        return new Ticket(id);
    }

    // Ticket als bezahlt markieren
    public void bezahlen() { this.bezahlt = true; }

    // Ticket beenden (Ausfahrtszeit setzen)
    public void beenden() { this.ende = LocalDateTime.now(); }

    // Berechnet die Parkdauer bis zum angegebenen Zeitpunkt
    public Duration dauerBis(LocalDateTime zeitpunkt) {
        return Duration.between(start, zeitpunkt.isBefore(start) ? start : zeitpunkt);
    }

    // Getter für Ticket-ID
    public String getId() { return id; }
    // Getter für bezahlt-Status
    public boolean isBezahlt() { return bezahlt; }
    // Getter für Startzeit
    public LocalDateTime getStart() { return start; }
}

// OOP: Klasse Zahlstation, Single Responsibility (S aus SOLID)
class Zahlstation {
    private final double preisProMinute; // Preis pro Minute

    // Konstruktor (OOP: Initialisierung)
    public Zahlstation(double preisProMinute) { this.preisProMinute = preisProMinute; }

    // Berechnet den zu zahlenden Betrag für ein Ticket
    public double berechnen(Ticket t) {
        Duration d = t.dauerBis(LocalDateTime.now());
        long minuten = Math.max(1, (d.toSeconds() + 59) / 60); 
        return minuten * preisProMinute;
    }

    // Markiert das Ticket als bezahlt
    public void bezahlen(Ticket t) {
        t.bezahlen();
    }
}

// OOP: Klasse Schranke, Single Responsibility (S aus SOLID)
class Schranke {
    private final String name; // Name der Schranke
    public Schranke(String name) { this.name = name; }
    // Öffnet die Schranke (Ausgabe)
    public void öffnen() {
        System.out.println("Schranke (" + name + ") öffnet.");
    }
}

// OOP: Klasse Anzeigetafel, Single Responsibility (S aus SOLID)
class Anzeigetafel {
    // Zeigt die Anzahl freier Plätze an
    public void anzeigen(int freie) {
        System.out.println("Freie Plätze: " + freie);
    }
}