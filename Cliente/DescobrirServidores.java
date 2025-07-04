import java.net.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DescobrirServidores {
  private final static int porta = 2025;
  private static final String broadCast = obterBroadcast();
  private static final int timeOutMs = 1000; // tempo de espera de resposta do servidor para saber se caiu ou nao (1s)
  private static final int intervaloDeSincronizacao = 1000; // Tempo entre sincronizações (1s)
  private Principal app;
  private InetAddress ipLocal;

  DescobrirServidores(Principal app) {
    this.app = app;
    try {
      this.ipLocal = InetAddress.getLocalHost();
      System.out.println("IP Local: " + ipLocal.getHostAddress());
    } catch (Exception e) {
      System.err.println("Erro ao obter IP local: " + e.getMessage());
      this.ipLocal = null; // Garante que não seja usada uma variável nula
    }
  }

  /*
   * ***************************************************************
   * Metodo: iniciarSincronizacao
   * Funcao: cria uma thread para iniciar a sincronizacao para descobrir quais sao
   * os servidores ativos
   * Parametros: sem parametro.
   * Retorno: sem retorno
   * ***************************************************************
   */
  public void iniciarSincronizacao() {
    new Thread(this::enviarSinc).start();
  }

  /*
   * ***************************************************************
   * Metodo: enviarSinc
   * Funcao: cria um socket para enviar via broadcast a apdu AREYOUALIVE e esperar
   * a respostas dos servidores que estao disponiveis, se o servidore que era o
   * ativo nao responder, elege um novo servidor para ser o ativo
   * Parametros: sem parametro.
   * Retorno: sem retorno
   * ***************************************************************
   */
  private void enviarSinc() {
    DatagramSocket socket = null;
    try {
      socket = new DatagramSocket();
      socket.setSoTimeout(timeOutMs);
      System.out.println("Socket criado para envio de sincronização");

      while (true) {
        try {
          // Verificações preventivas
          if (app == null) {
            throw new NullPointerException(
                "ERRO: `app` está NULL! A instância de `Principal` não foi passada corretamente.");
          }

          if (ipLocal == null) {
            throw new NullPointerException("ERRO: `ipLocal` está NULL! Falha ao obter o IP local.");
          }

          byte[] mensagem = "AREYOUALIVE".getBytes();
          DatagramPacket pacote = new DatagramPacket(mensagem, mensagem.length, InetAddress.getByName(broadCast),
              porta);
          socket.send(pacote);
          System.out.println("APDU AREYOUALIVE enviada via broadcast para " + broadCast);

          // Marca todos os servidores como inativos antes de receber respostas
          // app.setAllServidoresFalse();
          // boolean servidorAtivoAindaVivo = false;

          long inicio = System.currentTimeMillis();
          TreeSet<String> servidoresQueResponderam = new TreeSet<>();
          while (System.currentTimeMillis() - inicio < timeOutMs) {
            byte[] buffer = new byte[1024];
            DatagramPacket resposta = new DatagramPacket(buffer, buffer.length);

            try {
              socket.receive(resposta);
              InetAddress ipResposta = resposta.getAddress();
              servidoresQueResponderam.add(ipResposta.getHostAddress());
              String msg = new String(resposta.getData(), 0, resposta.getLength());

              System.out.println("Resposta recebida de " + ipResposta.getHostAddress() + ": " + msg);



              if (msg.equals("IMALIVE")) {
                // app.atualizaServidores(ipResposta, true);
                app.setServidoresConhecidos(ipResposta.getHostAddress()); //adiciona o ip do novo servidor descoberto

              }
            } catch (SocketTimeoutException e) {
              // Nenhuma resposta no tempo esperado
            }
          }

          Boolean servidorAindaAtivo = false;
          for (String ip : servidoresQueResponderam) {
            if (ip.equals(app.getIpServidor())){
              servidorAindaAtivo = true;
            }
          }

          if (!servidorAindaAtivo) {
            if (servidoresQueResponderam.size() > 0) {
              app.setIpServidor(servidoresQueResponderam.first());
            }
          }

          // Se o servidor ativo caiu, eleger um novo
          // if (!servidorAtivoAindaVivo) {
          //   elegerNovoServidorAtivo();
          // }
          // app.configurarHeader();
          Thread.sleep(intervaloDeSincronizacao);
        } catch (Exception e) {
          System.err.println("ERRO DURANTE O LOOP:");
          e.printStackTrace();
        }
      }

    } catch (Exception e) {
      System.err.println("ERRO AO CRIAR SOCKET OU ENVIAR SINC:");
      e.printStackTrace();
    } finally {
      if (socket != null && !socket.isClosed()) {
        socket.close();
        System.out.println("Socket fechado.");
      }
    }
  }

  /*
   * ***************************************************************
   * Metodo: elegerNovoServidorAtivo
   * Funcao: pega os servidores disponiveis e escolhe o primeiro para ser o ativo
   * Parametros: sem parametro.
   * Retorno: sem retorno
   * ***************************************************************
   */
  // private void elegerNovoServidorAtivo() {
  //   List<InetAddress> servidoresDisponiveis = app.getServidoresAtivos();

  //   if (!servidoresDisponiveis.isEmpty()) {
  //     InetAddress novoAtivo = servidoresDisponiveis.get(0);
  //     app.setServidorAtivo(novoAtivo);
  //     System.out.println("Novo servidor ativo eleito: " + novoAtivo.getHostAddress());
  //   } else {
  //     System.out.println("Nenhum servidor disponivel para se tornar o ativo!");
  //     app.setServidorAtivo(null);
  //   }
  // }

  /*
   * ***************************************************************
   * Metodo: obterBroadCast
   * Funcao: retorna o ip de broadcast da rede
   * Parametros: sem parametro.
   * Retorno: retorna um string
   * ***************************************************************
   */
  public static String obterBroadcast() {
    try {
      InetAddress ipLocal = InetAddress.getLocalHost();
      NetworkInterface netInterface = NetworkInterface.getByInetAddress(ipLocal);

      if (netInterface == null) {
        System.err.println("Nenhuma interface de rede encontrada para " + ipLocal.getHostAddress());
        return "255.255.255.255";
      }

      for (InterfaceAddress interfaceAddress : netInterface.getInterfaceAddresses()) {
        InetAddress broadcast = interfaceAddress.getBroadcast();
        if (broadcast != null) {
          System.out.println("Endereço de broadcast detectado: " + broadcast.getHostAddress());
          return broadcast.getHostAddress();
        }
      }
    } catch (Exception e) {
      System.err.println("Erro ao obter o endereço de broadcast: " + e.getMessage());
    }
    return "255.255.255.255";
  }
}
