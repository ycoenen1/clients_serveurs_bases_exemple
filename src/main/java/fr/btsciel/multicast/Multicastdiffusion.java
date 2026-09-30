package fr.btsciel.multicast;

import fr.btsciel.modele.ConfigServ;
import fr.btsciel.tcp.Serveur_TCP_Base;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Multicastdiffusion {
    public static boolean sessionActive;
    private  MulticastSocket dsResult;
    private final String INTERFACE = "ethernet_32769";
    private InetAddress ia = InetAddress.getByName("224.0.0.250");
    private byte[] data = new byte[512];
    private  int port = 5555;

    private byte [] dataSortie = (InetAddress.getLocalHost().getHostAddress() + ";" + Serveur_TCP_Base.PORT + ";" + "5000").getBytes(StandardCharsets.UTF_8);
    private int portRetour= 5556;
    private byte ttl=60;
    private DatagramPacket dp;
    private MulticastSocket ms;

    public Multicastdiffusion() throws IOException {
        ms = new MulticastSocket();
        NetworkInterface ni = NetworkInterface.getByName(INTERFACE);
        ms.setNetworkInterface(ni);
        ms.setTimeToLive(ttl);

        dsResult = new MulticastSocket(port);
        dsResult.joinGroup(new InetSocketAddress(ia, port), ni);
        new Thread(() -> {

            while (true) {
                try {
                    dp = new DatagramPacket(data, data.length);
                    dsResult.receive(dp);

                    String recu = new String(dp.getData(), 0, dp.getLength(), StandardCharsets.UTF_8);
                    if (recu.equals("Tu es qui?" )&& !sessionActive) {
                        DatagramPacket reponse = new DatagramPacket(dataSortie, dataSortie.length, dp.getAddress(), portRetour);
                        ms.send(reponse);
                        System.out.println(new String(dataSortie, StandardCharsets.UTF_8));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        }


    }

