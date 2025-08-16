import java.time.*;
import java.util.*;

public class ParkhausApp {
    public static void main(String[] args) {
        Parkhaus parkhaus = new Parkhaus("Zentrum", 10, 0.05); 
        Scanner sc = new Scanner(System.in);

        System.out.println("Willkommen im Parkhaus\n");
        boolean running = true;
        while (running) {
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
                    Ticket t = parkhaus.ticketAusgeben();
                    if (t != null) {
                        System.out.println("Ticket ausgegeben: ID " + t.getId());
                        parkhaus.getEinfahrt().öffnen();
                    } else {
                        System.out.println("Parkhaus voll – kein Ticket verfügbar.");
                    }
                    break;
                case "2":
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


class Parkhaus {
    private final String name;
    private final int kapazität;
    private int freiePlätze;
    private final Map<String, Ticket> tickets = new HashMap<>();
    private int nextId = 1; 

    private final Anzeigetafel anzeige = new Anzeigetafel();
    private final Schranke einfahrt = new Schranke("Einfahrt");
    private final Schranke ausfahrt = new Schranke("Ausfahrt");
    private final Zahlstation zahlstation; 

    public Parkhaus(String name, int kapazität, double minutenPreis) {
        this.name = name;
        this.kapazität = kapazität;
        this.freiePlätze = kapazität;
        this.zahlstation = new Zahlstation(minutenPreis);
    }

    public Ticket ticketAusgeben() {
        if (freiePlätze <= 0 || nextId > kapazität) return null;
        String id = String.valueOf(nextId++);
        Ticket t = Ticket.neu(id);
        tickets.put(t.getId(), t);
        freiePlätze--;
        anzeige.anzeigen(freiePlätze);
        return t;
    }

    public void zahlungRegistrieren(Ticket t) {
        
    }

    public void ticketBeenden(Ticket t) {
        t.beenden();
        if (freiePlätze < kapazität) freiePlätze++;
        anzeige.anzeigen(freiePlätze);
        tickets.remove(t.getId());
    }

    public Ticket findeTicket(String id) { return tickets.get(id); }

    public int getFreiePlätze() { return freiePlätze; }

    public Anzeigetafel getAnzeige() { return anzeige; }
    public Schranke getEinfahrt() { return einfahrt; }
    public Schranke getAusfahrt() { return ausfahrt; }
    public Zahlstation getZahlstation() { return zahlstation; }
}

class Ticket {
    private final String id;
    private final LocalDateTime start;
    private LocalDateTime ende; 
    private boolean bezahlt;

    private Ticket(String id) {
        this.id = id;
        this.start = LocalDateTime.now();
        this.bezahlt = false;
    }

    public static Ticket neu(String id) {
        return new Ticket(id);
    }

    public void bezahlen() { this.bezahlt = true; }

    public void beenden() { this.ende = LocalDateTime.now(); }

    public Duration dauerBis(LocalDateTime zeitpunkt) {
        return Duration.between(start, zeitpunkt.isBefore(start) ? start : zeitpunkt);
    }

    public String getId() { return id; }
    public boolean isBezahlt() { return bezahlt; }
    public LocalDateTime getStart() { return start; }
}

class Zahlstation {
    private final double preisProMinute; 

    public Zahlstation(double preisProMinute) { this.preisProMinute = preisProMinute; }

    public double berechnen(Ticket t) {
        Duration d = t.dauerBis(LocalDateTime.now());
        long minuten = Math.max(1, (d.toSeconds() + 59) / 60); 
        return minuten * preisProMinute;
    }

    public void bezahlen(Ticket t) {
        t.bezahlen();
    }
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