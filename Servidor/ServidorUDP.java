
import java.net.*;
import java.util.Map;

public class ServidorUDP {
  private final GrupoManager grupoManager;
  private final Map<String, Usuario> usuarios;

  public ServidorUDP(GrupoManager grupoManager, Map<String, Usuario> usuarios) {
    this.grupoManager = grupoManager;
    this.usuarios = usuarios;
  }

  /*
   * ***************************************************************
   * Metodo: iniciar
   * Funcao: Inicia o servidor UDP na porta 6789 e aguarda mensagens dos clientes.
   * A cada mensagem recebida, cria uma nova thread para processá-la.
   * Parametros: nenhum
   * Retorno: void
   */
  public void iniciar() {
    try {
      DatagramSocket servidorSocket = new DatagramSocket(6789); // Porta do servidor
      System.out.println("Servidor UDP iniciado na porta 6789...");

      while (true) {
        // Buffer para receber dados
        byte[] dadosRecebidos = new byte[1024];
        DatagramPacket pacoteRecebido = new DatagramPacket(dadosRecebidos, dadosRecebidos.length);

        // Aguarda uma mensagem de um cliente
        servidorSocket.receive(pacoteRecebido);

        // Inicia uma nova thread para processar a mensagem recebida
        new Thread(new ProcessaMensagem(pacoteRecebido, servidorSocket)).start();
      }
    } catch (Exception e) {
      System.err.println("Erro no servidor UDP: " + e.getMessage());
    }
  }

  private class ProcessaMensagem extends Thread {
    private final DatagramPacket pacoteRecebido;
    private final DatagramSocket servidorSocket;

    public ProcessaMensagem(DatagramPacket pacoteRecebido, DatagramSocket servidorSocket) {
      this.pacoteRecebido = pacoteRecebido;
      this.servidorSocket = servidorSocket;
    }

    /*
     * ***************************************************************
     * Metodo: run
     * Funcao: Executa o processamento da mensagem recebida no formato:
     * "SEND|Grupo|Usuario|Mensagem", e reencaminha a mensagem
     * para os demais membros do grupo, exceto o remetente.
     * Parametros: nenhum
     * Retorno: void
     */
    @Override
    public void run() {
      try {
        // Converte os dados recebidos em String
        String mensagemRecebida = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
        System.out.println(
            "Mensagem recebida de " + pacoteRecebido.getAddress() + ":" + pacoteRecebido.getPort());
        System.out.println("Conteudo: " + mensagemRecebida);

        // Divide a mensagem pela estrutura definida:
        // "SEND|NomeGrupo|NomeUsuario|Mensagem"
        String[] partes = mensagemRecebida.split("\\|", 4);
        if (partes.length != 4) {
          System.err.println("Formato inválido ou tipo de mensagem desconhecido.");
          return;
        }

        // Extração dos campos
        String tipoMensagem = partes[0];
        String nomeGrupo = partes[1];
        String nomeUsuario = partes[2];
        String conteudoMensagem = partes[3];

        // Verifica se a mensagem é do tipo SEND
        if (tipoMensagem.equals("SEND")) {
          // Sincroniza para garantir consistência na manipulação de usuários
          Usuario remetente;
          synchronized (usuarios) {
            remetente = usuarios.computeIfAbsent(nomeUsuario,
                k -> new Usuario(nomeUsuario, pacoteRecebido.getAddress(), pacoteRecebido.getPort()));
          }

          // // Adiciona o remetente ao grupo (se necessário)
          // synchronized (grupoManager) {
          // grupoManager.adicionarUsuario(nomeGrupo, remetente);
          // }

          // Reencaminha a mensagem para todos os membros do grupo, exceto o remetente
          for (Usuario usuario : grupoManager.obterMembros(nomeGrupo)) {
            if (!usuario.equals(remetente)) {
              InetAddress enderecoCliente = usuario.getEndereco();
              // int portaCliente = usuario.getPorta();
              byte[] dadosSaida = String.format("SEND|%s|%s|%s", nomeGrupo, nomeUsuario, conteudoMensagem).getBytes();
              DatagramPacket pacoteResposta = new DatagramPacket(dadosSaida, dadosSaida.length, enderecoCliente, 9876);
              servidorSocket.send(pacoteResposta);
            }
          }
        } else {
          System.err.println("Tipo de mensagem desconhecido: " + tipoMensagem);
        }
      } catch (Exception e) {
        System.err.println("Erro ao processar mensagem: " + e.getMessage());
      }
    }
  }
}
