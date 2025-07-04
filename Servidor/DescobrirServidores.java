import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.TreeSet;

public class DescobrirServidores {
  private final static int porta = 2025;
  private static final String broadCast = obterBroadcast();
  private static final int timeOutMs = 250;
  private static final int intervaloDeSincronizacao = 250; // Tempo entre sincronizações (5s)
  private Principal app;
  private InetAddress ipLocal;
  private final java.util.Map<String, Integer> contagemFalhas = new java.util.HashMap<>();

  DescobrirServidores(Principal app) {
    this.app = app;
    try {
      this.ipLocal = InetAddress.getLocalHost();
    } catch (Exception e) {
      System.err.println("Erro ao obter IP local: " + e.getMessage());
    }
  }

  /*
   * ***************************************************************
   * Metodo: iniciarSincronizacao.
   * Funcao: cria duas threads, uma para enviar mensagens para descobrir os
   * servidores disponiveis e outra para receber essas mensagens e responder os
   * outros servidores
   * Parametros: sem paramentros.
   * Retorno: sem retorno.
   * ***************************************************************
   */
  public void iniciarDescobrimento() {

    new Thread(this::enviarSinc).start();
    new Thread(this::receberSinc).start();
  }

  /*
   * ***************************************************************
   * Metodo: enviarSinc.
   * Funcao: envia uma APDU SINC para os servidores via broadcast e espera uma
   * resposta que contem o horario da maquina do servidor que respondeu
   * Parametros: sem paramentros.
   * Retorno: sem retorno.
   * ***************************************************************
   */
  private void enviarSinc() {
    try {
      DatagramSocket socket = new DatagramSocket();
      while (true) {
        byte[] mensagem = "AREYOUALIVE".getBytes();
        DatagramPacket pacote = new DatagramPacket(mensagem, mensagem.length, InetAddress.getByName(broadCast), porta);
        socket.send(pacote);
        // System.out.println("APDU AREYOUALIVE enviada via broadcast");

        // espera respostas
        long inicio = System.currentTimeMillis();
        TreeSet<String> servidoresQueResponderam = new TreeSet<>();
        while (System.currentTimeMillis() - inicio < timeOutMs) {
          byte[] buffer = new byte[1024];
          DatagramPacket resposta = new DatagramPacket(buffer, buffer.length);
          try {
            socket.setSoTimeout(timeOutMs);
            socket.receive(resposta);
            InetAddress ipResposta = resposta.getAddress();
            servidoresQueResponderam.add(ipResposta.getHostAddress());

            if (!resposta.getAddress().equals(ipLocal)) { // ignora se foi ele mesmo que se respondeu
              String msg = new String(resposta.getData(), 0, resposta.getLength());

              if (msg.equals("IMALIVE")) {
                app.setServidoresConhecidos(resposta.getAddress().getHostAddress()); // adiciona o ip do servidor
                                                                                     // descoberto
                // System.out.println("Resposta recebida de " + resposta.getAddress());
              }
            }

          } catch (SocketTimeoutException e) {
            // TODO: handle exception
          }
        }

        Thread.sleep(intervaloDeSincronizacao);

        // Marca quem respondeu com 0 falhas
        for (String ip : servidoresQueResponderam) {
          contagemFalhas.put(ip, 0); // reset
        }

        // Verifica quem NÃO respondeu
        for (String ipServidorConhecido : new java.util.HashSet<>(app.getServidoresConhecidos())) {
          if (!servidoresQueResponderam.contains(ipServidorConhecido)) {
            int falhas = contagemFalhas.getOrDefault(ipServidorConhecido, 0) + 1;
            contagemFalhas.put(ipServidorConhecido, falhas);

            if (falhas >= 3) {
              System.out.println("Servidor " + ipServidorConhecido + " removido após 3 falhas. Servidor caiu!");
              app.removerServidor(ipServidorConhecido);
              contagemFalhas.remove(ipServidorConhecido); // limpa o contador também
            }
          }
        }

      }

    } catch (Exception e) {
      System.err.println("Erro ao enviar APDU SINC: " + e.getMessage());
    }
  }

  /*
   * ***************************************************************
   * Metodo: receberSinc.
   * Funcao: fica apto a receber mensagens de outros servidores ou dos clientes,
   * caso a APDU que chegar for SINC, entao ele responde com o seu horario, se for
   * AREYOUALIVE, ele responde ao cliente que esta disponivel
   * Parametros: sem paramentros.
   * Retorno: sem retorno.
   * ***************************************************************
   */
  private void receberSinc() {
    try {
      DatagramSocket socket = new DatagramSocket(porta);
      while (true) {
        byte[] buffer = new byte[1024];
        DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);
        socket.receive(pacoteRecebido);

        String mensagem = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());

        // if (mensagem.equals("SINC")) {
        // String horaAtual =
        // app.getHorarioMaquina().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        // byte[] resposta = ("HORA|" + horaAtual).getBytes();

        // DatagramPacket pacoteResposta = new DatagramPacket(resposta, resposta.length,
        // pacoteRecebido.getAddress(),
        // pacoteRecebido.getPort());
        // socket.send(pacoteResposta);
        // System.out.println("Respondi com meu horário: " + horaAtual);

        // } else
        if (mensagem.equals("AREYOUALIVE")) {
          byte[] resposta = ("IMALIVE").getBytes();

          DatagramPacket pacoteResposta = new DatagramPacket(resposta, resposta.length, pacoteRecebido.getAddress(),
              pacoteRecebido.getPort());
          socket.send(pacoteResposta);
          System.out.println("Respondi que estou ativo");
        }
      }
    } catch (Exception e) {
      System.err.println("Erro ao receber APDU SINC: " + e.getMessage());
    }
  }

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

      for (InterfaceAddress interfaceAddress : netInterface.getInterfaceAddresses()) {
        InetAddress broadcast = interfaceAddress.getBroadcast();
        if (broadcast != null) {
          return broadcast.getHostAddress();
        }
      }
    } catch (Exception e) {
      System.err.println("Erro ao obter o endereço de broadcast: " + e.getMessage());
    }
    return "255.255.255.255"; // Padrão genérico caso não seja possível detectar
  }

}
