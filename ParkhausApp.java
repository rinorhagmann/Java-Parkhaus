import java.time.*;
import java.util.*;

// OOP: Hauptklasse inkl Main-Methode
public class ParkhausApp {
    public static void main(String[] args) {
        Parkhaus parkhaus = new Parkhaus("Zentrum", 10, 0.05); // OOP: Objekt Parkhaus
        Scanner sc = new Scanner(System.in); // OOP: Eingabeobjekt

        System.out.println("Willkommen im Parkhaus\n");
        boolean running = true; 
        while (running) {
            // Menüausgabe
            System.out.println("--- Menü ---");
            System.out.println("1) Einfahren");
            System.out.println("2) Bezahlen");
            System.out.println("3) Ausfahren");
            System.out.println("4) Freie Plätze anzeigen");
            System.out.println("0) Beenden");
            System.out.print("> ");
            String input = sc.nextLine().trim(); // Eingabe wird in var input gespeichert

            // Auswahlmenü mit switch
            switch (input) {
                case "1": // Ticket ziehen
                    Ticket t = parkhaus.ticketAusgeben();
                    if (t != null) {
                        System.out.println("Ticket ausgegeben: ID " + t.getId());
                        parkhaus.getEinfahrt().öffnen(); // Schranke öffnen
                    } else {
                        System.out.println("Parkhaus voll.");
                    }
                    break;
                case "2": // Ticket bezahlen
                    System.out.print("Ticket-ID: ");
                    String idPay = sc.nextLine().trim(); // Eingabe ID wird in var idPay gespeichert
                    Ticket payT = parkhaus.findeTicket(idPay); // Ticket suchen
                    if (payT == null) {
                        System.out.println("Unbekannte Ticket-ID.");
                        break;
                    }
                    double betrag = parkhaus.getZahlstation().berechnen(payT); // Preis berechnen
                    System.out.printf("Zu zahlen: %.2f CHF\n", betrag);
                    System.out.print("Jetzt bezahlen? (j/n) ");
                    if (sc.nextLine().trim().equalsIgnoreCase("j")) {
                        parkhaus.getZahlstation().bezahlen(payT); // Ticket bezahlen
                        parkhaus.zahlungRegistrieren(payT); 
                        System.out.println("Bezahlt. Quittung: Ticket " + payT.getId());
                    }
                    break;
                case "3": // Ausfahren
                    System.out.print("Ticket-ID: ");
                    String idExit = sc.nextLine().trim(); // Eingabe ID wird in var idExit gespeichert
                    Ticket exitT = parkhaus.findeTicket(idExit); // Ticket suchen
                    if (exitT == null) {
                        System.out.println("Unbekannte Ticket-ID."); // Falls Ticket nicht gefunden
                        break;
                    }
                    if (exitT.isBezahlt()) {
                        parkhaus.getAusfahrt().öffnen(); // Schranke öffnen
                        parkhaus.ticketBeenden(exitT);   // Ticket schliessen, Platz freigeben
                        System.out.println("Gute Fahrt!");
                    } else {
                        System.out.println("Bitte zuerst bezahlen.");
                    }
                    break;
                case "4": // Freie Plätze anzeigen
                    parkhaus.getAnzeige().anzeigen(parkhaus.getFreiePlätze());
                    break;
                case "0": // Programm beenden
                    running = false;
                    break;
                default:
                    System.out.println("Ungültige Eingabe.");
            }
            System.out.println();
        }
    }
}

// OOP: Klasse Parkhaus
// SOLID: Single Responsibility Principle (hat nur eine Aufgabe: Parkhaus verwalten)
class Parkhaus {
    private final String name;           // Name des Parkhauses
    private final int kapazität;         // Maximale Plätze
    private int freiePlätze;             // Aktuell freie Plätze
    private final Map<String, Ticket> tickets = new HashMap<>(); // Ticketverwaltung (verwendet Bibliothek HashMap für schnelle Suche)
    private int nextId = 1;              // Nächste Ticket-ID

    // Objekte
    private final Anzeigetafel anzeige = new Anzeigetafel(); // Anzeigetafel für freie Plätze
    private final Schranke einfahrt = new Schranke("Einfahrt"); // Schranke mit Name einfahrt
    private final Schranke ausfahrt = new Schranke("Ausfahrt"); // Schranke mit Name ausfahrt
    private final Zahlstation zahlstation; 

    // Konstruktor
    public Parkhaus(String name, int kapazität, double minutenPreis) {
        this.name = name;
        this.kapazität = kapazität;
        this.freiePlätze = kapazität;
        this.zahlstation = new Zahlstation(minutenPreis);
    }

    // Ticket erstellen, wenn Platz frei - Methode
    public Ticket ticketAusgeben() {
        if (freiePlätze <= 0 || nextId > kapazität) return null; // Falls kein Platz oder ID zu hoch
        String id = String.valueOf(nextId++); // ID wird mit jedem Ticket erhöht
        Ticket t = Ticket.neu(id);
        tickets.put(t.getId(), t);
        freiePlätze--; // Freie Plätze verringern
        anzeige.anzeigen(freiePlätze);
        return t;
    }

    // Zahlung registrieren
    public void zahlungRegistrieren(Ticket t) {
        System.out.println("Zahlung registriert für Ticket " + t.getId()); // Ausgabe
    }

    // Ticket beenden, Platz wieder freigeben
    public void ticketBeenden(Ticket t) {
        t.beenden();
        if (freiePlätze < kapazität) freiePlätze++; // Platz wieder freigeben
        anzeige.anzeigen(freiePlätze); // Anzeige aktualisieren
        tickets.remove(t.getId()); // Ticket entfernen
    }

    // Ticket suchen
    public Ticket findeTicket(String id) { return tickets.get(id); }

    // Getter
    public int getFreiePlätze() { return freiePlätze; } 
    public Anzeigetafel getAnzeige() { return anzeige; }
    public Schranke getEinfahrt() { return einfahrt; }
    public Schranke getAusfahrt() { return ausfahrt; }
    public Zahlstation getZahlstation() { return zahlstation; }
}

// OOP: Klasse Ticket – speichert Parkvorgang
// SOLID: SRP – nur Ticket-Daten
class Ticket {
    private final String id;            // Ticket-ID
    private final LocalDateTime start;  // Startzeit
    private LocalDateTime ende;         // Endzeit
    private boolean bezahlt;            // Bezahlstatus

    // privater Konstruktor
    private Ticket(String id) {
        this.id = id; // ID wird gesetzt
        this.start = LocalDateTime.now(); // Zeitstempel jetzt
        this.bezahlt = false; // nicht bezahlt
    }

    // Ticket erzeugen
    public static Ticket neu(String id) { return new Ticket(id); } // neues Ticket mit ID

    // Methoden zum Status ändern
    public void bezahlen() { this.bezahlt = true; } // Um bezahlstatus zu ändern
    public void beenden() { this.ende = LocalDateTime.now(); } // Endzeit setzen

    // Dauer berechnen
    public Duration dauerBis(LocalDateTime zeitpunkt) {
        return Duration.between(start, zeitpunkt.isBefore(start) ? start : zeitpunkt); // Ausrechnung der Dauer (Differenz)
    }

    // Getter
    public String getId() { return id; } // ID des Tickets
    public boolean isBezahlt() { return bezahlt; } // Bezahlstatus
    public LocalDateTime getStart() { return start; } // Startzeit des Tickets
}

// OOP: Klasse Zahlstation
// SOLID: Single Responsibility Principle
class Zahlstation {
    private final double preisProMinute; // Preis pro Minute
    public Zahlstation(double preisProMinute) { this.preisProMinute = preisProMinute; } // Konstruktor

    // Kosten berechnen
    public double berechnen(Ticket t) {
        Duration d = t.dauerBis(LocalDateTime.now()); // Dauer bis jetzt mit methode dauerBis berechnen
        long minuten = Math.max(1, (d.toSeconds() + 59) / 60);  // Runden auf volle Minute
        return minuten * preisProMinute; // Preis berechnen
    }

    // Ticket als bezahlt markieren
    public void bezahlen(Ticket t) { t.bezahlen(); }
}

// OOP: Klasse Schranke
// SOLID: Single Responsibility Principle
class Schranke {
    private final String name; // Name der Schranke
    public Schranke(String name) { this.name = name; } // Konstruktor mit Name
    public void öffnen() { System.out.println("Schranke (" + name + ") öffnet."); } // Schranke öffnen
}

// OOP: Klasse Anzeigetafel
// SOLID: Single Responsibility Principle
class Anzeigetafel {
    public void anzeigen(int freie) {
        System.out.println("Freie Plätze: " + freie); // Anzeige der freien Plätze
    }
}
