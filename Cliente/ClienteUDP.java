import java.net.*;

public class ClienteUDP {
  private final DatagramSocket clienteSocket;
  private final DatagramSocket clienteSocket1;
  private InetAddress enderecoServidor;
  private final int portaServidor;

  public ClienteUDP(String ipServidor, int portaServidor) throws Exception {
    this.clienteSocket = new DatagramSocket(); // Socket para comunicação UDP
    this.enderecoServidor = InetAddress.getByName(ipServidor); // Endereço do servidor
    this.portaServidor = portaServidor;
    this.clienteSocket1 = new DatagramSocket(9876); // Escolha uma porta fixada
  }


  /* ***************************************************************
  * Metodo: enviarMensagem
  * Funcao: Envia uma mensagem UDP para o servidor configurado.
  * Parametros: String mensagem - conteúdo da mensagem a ser enviada.
  * Retorno: void
  *************************************************************** */
  public void enviarMensagem(String mensagem) {
    try {
      byte[] dadosEnvio = mensagem.getBytes();
      DatagramPacket pacoteEnvio = new DatagramPacket(
          dadosEnvio,
          dadosEnvio.length,
          enderecoServidor,
          portaServidor);
      clienteSocket.send(pacoteEnvio);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* ***************************************************************
  * Metodo: receberMensagem
  * Funcao: Aguarda e retorna uma mensagem recebida do servidor.
  * Parametros: sem parâmetros.
  * Retorno: String - conteúdo da mensagem recebida.
  *************************************************************** */
  public String receberMensagem() {
    try {
      byte[] dadosRecebidos = new byte[1024];
      DatagramPacket pacoteRecebido = new DatagramPacket(dadosRecebidos, dadosRecebidos.length);
      clienteSocket1.receive(pacoteRecebido); // Bloqueia até receber um pacote
      return new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
    } catch (Exception e) {
      e.printStackTrace();
      return "Erro ";
    }
  }

  /* ***************************************************************
  * Metodo: setIpServidor
  * Funcao: Atualiza o IP do servidor utilizado pelo cliente UDP.
  * Parametros: String novoIP - novo endereço IP do servidor.
  * Retorno: void
  *************************************************************** */
  public void setIpServidor(String novoIP) {
    try {
      enderecoServidor = InetAddress.getByName(novoIP);
    } catch (Exception e) {
      // TODO: handle exception
    }
  }

  /* ***************************************************************
  * Metodo: fechar
  * Funcao: Fecha o socket UDP utilizado para envio de mensagens.
  * Parametros: sem parâmetros.
  * Retorno: void
  *************************************************************** */
  public void fechar() {
    // escutando = false; // Para a thread de escuta
    clienteSocket.close();
  }
}
