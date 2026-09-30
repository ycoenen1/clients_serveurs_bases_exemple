package fr.btsciel.tcp;

import fr.btsciel.multicast.Multicastdiffusion;
import fr.btsciel.utils.aes.Aes_cbc;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;


public class Serveur_TCP_Base {
    public static final int PORT = 4000;

    static Aes_cbc aesCbc;

    private static final String MESSAGE_ACCUEIL = """
            Rentrez une phrase pour la mettre en majuscule.
            • Si cette requête est « HELLO », le serveur répond un message de bienvenue.
            • Si cette requête est « TIME », le serveur répond la date qu'il est.
            • Si cette requête est « ECHO …une phrase… », le serveur répond en répétant la phrase.
            • Si cette requête est « YOU » ou « WHOAREYOU? », le serveur renvoi sa socket (IP@ et port).
            • Si cette requête est « ME » ou « WHOAMI? », le serveur renvoi la socket du client (IP@ et
            port) qu'i récupère dans la connexion établie par le client.
            • Si cette requête est « EXIT », le client est déconnecté.
            """;

    static void main(String[] args) throws IOException {
        Multicastdiffusion multicastdiffusion = new Multicastdiffusion();

        ServerSocket serveur = new ServerSocket(PORT);
        System.out.println("Serveur en fonctionnement sur le port " + PORT + ".");
        while (true) {
            Socket client = serveur.accept();
            Multicastdiffusion.sessionActive = true;
            try {
                int oclus = 0;
                InputStream in = client.getInputStream();
                OutputStream out = client.getOutputStream();
                System.out.println("Connexion avec : " + client);
                aesCbc = new Aes_cbc("mot de passe aes".getBytes(StandardCharsets.UTF_8),"ici vecteur d'in".getBytes(StandardCharsets.UTF_8));

                out.write(aesCbc.cryptage(MESSAGE_ACCUEIL.getBytes(StandardCharsets.UTF_8)));
                String messageRecu;
                byte[] chars = new byte[65535];
                while(true){
                    oclus = in.read(chars);
                    if (oclus == -1) {
                        break;
                    }
                    byte[] octFin = Arrays.copyOfRange(chars,0,oclus);
                    if (oclus >0) {
                        messageRecu = new String(aesCbc.decryptage(octFin));

                        if (messageRecu.equalsIgnoreCase("exit")) {
                            System.out.println("Message reçu : " + messageRecu);
                            try {
                                if (TransportCase(messageRecu, out, client)) {
                                    break;
                                }
                            } catch (Exception e) {

                            }

                        }
                        System.out.println("Message reçu : " + messageRecu);
                        TransportCase(messageRecu, out, client);
                    }
                }
            } catch (Exception e) {
                System.err.println("Connexion interrompue : " + e.getMessage());
            } finally {
                client.close();
                Multicastdiffusion.sessionActive = false;
            }
            System.out.println("Client déconnecté.");
        }
    }

//    private static void TransportIf(String message, OutputStream out, Socket client) {
//        if (message.trim().equalsIgnoreCase("exit")) {
//            sortie.println("JE VOUS DECONNECTE !!!");
//
//        } else if (message.trim().equalsIgnoreCase("HELLO")) {
//            sortie.println("Bienvenue sur le serveur!");
//
//        } else if (message.trim().equalsIgnoreCase("TIME")) {
//
//            sortie.println("La date actuel est " + LocalTime.now());
//
//        } else if (message.trim().equalsIgnoreCase("ECHO")) {
//            sortie.println("La phrase est " + message);
//
//        } else if (message.trim().equalsIgnoreCase("YOU") || message.trim().equalsIgnoreCase("WHOAREYOU?")) {
//            try {
//                sortie.println(InetAddress.getLocalHost());
//            } catch (Exception e) {
//                System.out.println("Impossible");
//            }
//
//
//        } else if (message.trim().equalsIgnoreCase("ME") || message.trim().equalsIgnoreCase("WHOAMI?")) {
//            sortie.println(client.getLocalAddress());
//
//        } else {
//            String reponse = message.toUpperCase();
//            sortie.println(reponse);
//            System.out.println("Message émis : " + reponse);
//        }
//    }

    private static boolean TransportCase(String message, OutputStream out, Socket client) throws IOException {
        message = message.trim();
        switch (message.toLowerCase()) {
            case "fin","exit":
                out.write(aesCbc.cryptage("JE VOUS DECONNECTE !!!".getBytes(StandardCharsets.UTF_8)));

                return true;

            case "hello":
                out.write(aesCbc.cryptage("Bienvenue sur le serveur!".getBytes(StandardCharsets.UTF_8)));

                break;
            case "time":
                out.write(aesCbc.cryptage(("La date actuel est " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).getBytes()));

                break;
            case "you", "whoareyou?":
                try {
                    out.write(aesCbc.cryptage((InetAddress.getLocalHost() + ":" + PORT).getBytes()));

                } catch (UnknownHostException e) {
                    out.write(aesCbc.cryptage("Erreur lors de la recuperation de l'adresse ip".getBytes()));
                }
                break;
            case "me", "whoami?":
                out.write(aesCbc.cryptage(client.getRemoteSocketAddress().toString().getBytes(StandardCharsets.UTF_8)));
                break;
            default:
                if (message.toLowerCase().substring(0, 4).equalsIgnoreCase("echo")) {
                    out.write(aesCbc.cryptage(message.substring(4).getBytes()));

                    break;
                }
                String reponse = message.toUpperCase();
                out.write(aesCbc.cryptage(reponse.getBytes(StandardCharsets.UTF_8)));
                System.out.println("Message émis : " + reponse);

                break;

        }

        return false;
    }

    private static void TransportRegex(String message, PrintWriter sortie, Socket client) {

    }

}