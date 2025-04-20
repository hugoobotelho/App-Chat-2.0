
/* ***************************************************************
* Autor............: Hugo Botelho Santana
* Matricula........: 202210485
* Inicio...........: 21/11/2024
* Ultima alteracao.: 28/11/2024
* Nome.............: Camada de Transporte/Aplicação - Aplicativo de Instant Messaging
* Funcao...........: Aplicativo de chat para troca de mensagens com o modelo cliente servidor
*************************************************************** */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Principal {

  private static final Map<String, Usuario> usuarios = new HashMap<>(); // Gerencia usuários
  private Set<String> servidoresConhecidos;
  private static DescobrirServidores descobrirServidores;
  private static Principal app;
  private static GrupoManager grupoManager; // Gerencia grupos
  private Set<AtualizarServidores> servidoresTCP = new HashSet<>();

  public static void main(String[] args) {

    app = new Principal();

    descobrirServidores = new DescobrirServidores(app);
    descobrirServidores.iniciarDescobrimento();

    grupoManager = new GrupoManager(app);

    // Inicia o servidor UDP em uma thread separada
    Thread servidorUDPThread = new Thread(() -> {
      ServidorUDP servidorUDP = new ServidorUDP(grupoManager, usuarios);
      servidorUDP.iniciar();
    });

    // Inicia o servidor TCP em uma thread separada
    Thread servidorTCPThread = new Thread(() -> {
      ServidorTCP servidorTCP = new ServidorTCP(grupoManager, usuarios);
      servidorTCP.iniciar();
    });

    // Inicia as threads
    servidorUDPThread.start();
    servidorTCPThread.start();

    System.out.println("Servidores UDP e TCP iniciados...");
  }

  // Getter para o GrupoManager
  public static GrupoManager getGrupoManager() {
    return grupoManager;
  }

  // Getter para o mapa de usuários
  public static Map<String, Usuario> getUsuarios() {
    return usuarios;
  }

  public Set<String> getServidoresConhecidos() {
    return servidoresConhecidos;
  }

  public void setServidoresConhecidos(String novoServidor) {
    if (!servidoresConhecidos.contains(novoServidor)) {
      AtualizarServidores atualizarServidores = new AtualizarServidores(novoServidor, 6789, app);
      servidoresTCP.add(atualizarServidores);
      servidoresConhecidos.add(novoServidor);
      System.out.println("Servidor adicionado: " + novoServidor);
    }

    // if (ipServidor == null) {
    // ipServidor = novoServidor;
    // if (clienteTCP == null) {
    // clienteTCP = new ClienteTCP(ipServidor, 6789, this);
    // }
    // if (clienteUDP == null) {
    // criarClienteUDP(ipServidor, 6789);
    // }
    // }
  }

  public Set<AtualizarServidores> getServidoresTCP() {
    return servidoresTCP;
  }
}
