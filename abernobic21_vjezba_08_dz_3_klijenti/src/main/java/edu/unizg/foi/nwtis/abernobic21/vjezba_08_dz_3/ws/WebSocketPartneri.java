package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ws;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.ws.rs.core.Response;

@ServerEndpoint("/ws/partneri")
public class WebSocketPartneri {

  @Inject
  ServisPartnerKlijent klijent;

  @Inject
  GlobalniPodaci globalniPodaci;

  @Inject
  @ConfigProperty(name = "partnerID", defaultValue = "1")
  private int partnerId;

  static Queue<Session> queue = new ConcurrentLinkedQueue<>();

  public static void send(String poruka) {
    try {
      for (Session session : queue) {
        if (session.isOpen()) {
          System.out.println("Šaljem poruku partner: " + poruka);
          session.getBasicRemote().sendText(poruka);
        }
      }
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @OnOpen
  public void openConnection(Session session, EndpointConfig conf) {
    queue.add(session);
    System.out.println("Otvorena veza.");
  }

  @OnClose
  public void closedConnection(Session session, CloseReason reason) {
    queue.remove(session);
    System.out.println("Zatvorena veza.");
  }

  @OnMessage
  public void Message(Session session, String poruka) {
    System.out.println("Primljena poruka partner: " + poruka);
    String formatiranaPoruka = formirajStatusPoruku(poruka);
    WebSocketPartneri.send(formatiranaPoruka);
  }

  @OnError
  public void error(Session session, Throwable t) {
    queue.remove(session);
    System.out.println("Zatvorena veza zbog pogreške.");
  }

  public String formirajStatusPoruku(String internaPoruka) {
    String status = serverRadi() ? "RADI" : "NE RADI";
    int brojOtvorenihNarudzbi = globalniPodaci.getBrojOtvorenihNarudzbi(partnerId);
    int brojPlacenihRacuna = globalniPodaci.getBrojRacuna(partnerId);

    String poruka = status + ";" + brojOtvorenihNarudzbi + ";" + brojPlacenihRacuna;

    return poruka;
  }

  private boolean serverRadi() {
    try {
      Response odgovor = klijent.headPosluzitelj();
      return odgovor.getStatus() == 200;
    } catch (Exception e) {
      return false;
    }
  }
}

