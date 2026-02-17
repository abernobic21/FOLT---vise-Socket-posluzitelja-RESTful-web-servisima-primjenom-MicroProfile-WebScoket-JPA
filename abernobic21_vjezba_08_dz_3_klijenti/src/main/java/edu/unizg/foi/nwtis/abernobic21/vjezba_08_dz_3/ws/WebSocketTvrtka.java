package edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ws;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.ServisTvrtkaKlijent;
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

@ServerEndpoint("/ws/tvrtka")
public class WebSocketTvrtka {

  @Inject
  ServisTvrtkaKlijent klijent;

  @Inject
  GlobalniPodaci globalniPodaci;

  static Queue<Session> queue = new ConcurrentLinkedQueue<>();

  public static void send(String poruka) {
    try {
      for (Session session : queue) {
        if (session.isOpen()) {
          System.out.println("Šaljem poruku: " + poruka);
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
    System.out.println("Primljena poruka: " + poruka);
    String formatiranaPoruka = formirajStatusPoruku(poruka);
    WebSocketTvrtka.send(formatiranaPoruka);
  }

  @OnError
  public void error(Session session, Throwable t) {
    queue.remove(session);
    System.out.println("Zatvorena veza zbog pogreške.");
  }

  public String formirajStatusPoruku(String internaPoruka) {
    String status = serverRadi() ? "RADI" : "NE RADI";
    int brojObracuna = globalniPodaci.getBrojObracuna();

    String poruka =
        status + ";" + brojObracuna + ";" + (internaPoruka != null ? internaPoruka : "");

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
