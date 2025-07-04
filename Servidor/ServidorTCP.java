
import java.net.*;
import java.io.*;
import java.util.Map;

public class ServidorTCP {

  private final GrupoManager grupoManager;
  private final Map<String, Usuario> usuarios;
  private Principal app;

  public ServidorTCP(GrupoManager grupoManager, Map<String, Usuario> usuarios, Principal app) {
    this.grupoManager = grupoManager;
    this.usuarios = usuarios;
    this.app = app;
  }

  /*
   * ***************************************************************
   * Metodo: iniciar
   * Funcao: Inicia o servidor TCP na porta 6789, aguardando conexões.
   * Cada conexão é tratada por uma nova thread.
   * Parametros: nenhum
   * Retorno: void
   */
  public void iniciar() {
    try {
      int portaLocal = 6789;
      ServerSocket servidorSocket = new ServerSocket(portaLocal); // Socket do servidor
      System.out.println("Servidor TCP iniciado na porta " + portaLocal + "...");

      while (true) {
        Socket conexao = servidorSocket.accept(); // Aceita conexões de clientes
        new Thread(new ProcessaCliente(conexao)).start();
      }
    } catch (Exception e) {
      System.err.println("Erro no servidor TCP: " + e.getMessage());
    }
  }

  private class ProcessaCliente extends Thread {
    private final Socket conexao;

    public ProcessaCliente(Socket conexao) {
      this.conexao = conexao;
    }

    /*
     * ***************************************************************
     * Metodo: run
     * Funcao: Lê a mensagem recebida, processa a operação (JOIN/LEAVE),
     * envia resposta ao cliente e fecha a conexão.
     * Parametros: nenhum
     * Retorno: void
     */
    @Override
    public void run() {
      ObjectInputStream entrada = null;
      ObjectOutputStream saida = null;
      try {
        entrada = new ObjectInputStream(conexao.getInputStream());
        String mensagemRecebida = (String) entrada.readObject(); // Lê a mensagem do cliente
        System.out.println("Mensagem recebida via TCP: " + mensagemRecebida);

        String resposta = processarMensagem(mensagemRecebida, conexao);

        saida = new ObjectOutputStream(conexao.getOutputStream());
        saida.writeObject(resposta); // Envia a resposta
        saida.flush(); // Garante que a resposta será enviada ao cliente

      } catch (IOException e) {
        System.err.println("Erro de I/O ao processar cliente: " + e.getMessage());
      } catch (ClassNotFoundException e) {
        System.err.println("Erro ao ler objeto do cliente: " + e.getMessage());
      } finally {
        try {
          if (entrada != null)
            entrada.close();
          if (saida != null)
            saida.close();
          conexao.close(); // Fechar conexão ao final
        } catch (IOException e) {
          System.err.println("Erro ao fechar a conexão: " + e.getMessage());
        }
      }
    }

    /*
     * ***************************************************************
     * Metodo: processarMensagem
     * Funcao: Interpreta e executa o comando recebido (JOIN ou LEAVE).
     * Parametros:
     * String mensagem - mensagem no formato TIPO|USUARIO|GRUPO
     * Socket conexao - conexão com o cliente, usada para obter IP e porta
     * Retorno: String - resposta de sucesso ou erro da operação
     */
    private String processarMensagem(String mensagem, Socket conexao) {
      try {
        String[] partes = mensagem.split("\\|");
        if (partes.length < 3) {
          return "Erro: Mensagem mal formatada. Esperado TIPO|USUARIO|GRUPO|ENDERECO.";
        }

        String tipo = partes[0].trim();
        String nomeUsuario = partes[1].trim();
        String nomeGrupo = partes[2].trim();
        InetAddress enderecoUsuario;
        enderecoUsuario = InetAddress.getByName(partes[3].trim());
        Usuario usuario;
        synchronized (usuarios) {
          usuario = usuarios.computeIfAbsent(nomeUsuario,
              k -> new Usuario(nomeUsuario, enderecoUsuario, conexao.getPort()));
        }

        synchronized (grupoManager) {
          // String newMessage;
          String mensagemComTimestamp;
          switch (tipo.toUpperCase()) {
            case "JOIN":
              mensagemComTimestamp = mensagem + "|" + System.currentTimeMillis();
              app.setMessageLog(mensagemComTimestamp);

              // app.setMessageLog(mensagem); // adiciona a mensagem ao log de mensagens
              grupoManager.adicionarUsuario(nomeGrupo, usuario, false);
              return "Usuário " + nomeUsuario + " adicionado ao grupo " + nomeGrupo;

            case "LEAVE":
              mensagemComTimestamp = mensagem + "|" + System.currentTimeMillis();
              app.setMessageLog(mensagemComTimestamp);

              // app.setMessageLog(mensagem); // adiciona a mensagem ao log de mensagens
              if (grupoManager.grupoExiste(nomeGrupo)) {
                grupoManager.removerUsuario(nomeGrupo, usuario, false);
                return "Usuário " + nomeUsuario + " removido do grupo " + nomeGrupo;
              } else {
                return "Erro: Grupo " + nomeGrupo + " não existe.";
              }
            case "ATUALIZAR_JOIN":
              // newMessage = "JOIN|" + nomeUsuario + "|" + nomeGrupo;
              app.setMessageLog(mensagem);
              grupoManager.adicionarUsuario(nomeGrupo, usuario, true);
              return "Fui atualizado com Usuário " + nomeUsuario + " adicionado ao grupo " + nomeGrupo;

            case "ATUALIZAR_LEAVE":
              // newMessage = "LEAVE|" + nomeUsuario + "|" + nomeGrupo;
              app.setMessageLog(mensagem);
              if (grupoManager.grupoExiste(nomeGrupo)) {
                grupoManager.removerUsuario(nomeGrupo, usuario, true);
                return "Fui atualizado com Usuário " + nomeUsuario + " removido do grupo " + nomeGrupo;
              } else {
                return "Erro: Grupo " + nomeGrupo + " não existe.";
              }

            default:
              return "Erro: Tipo de mensagem desconhecido. Use JOIN ou LEAVE.";
          }
        }
      } catch (UnknownHostException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return "Erro: possivel mensagem mal formatada";
      }
    }
  }
}