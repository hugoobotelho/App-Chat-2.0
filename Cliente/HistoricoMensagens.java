/* ***************************************************************
* Autor............: Hugo Botelho Santana
* Matricula........: 202210485
* Inicio...........: 21/11/2024
* Ultima alteracao.: 28/11/2024
* Nome.............: Camada de Transporte/Aplicação - Aplicativo de Instant Messaging
* Funcao...........: Aplicativo de chat para troca de mensagens com o modelo cliente servidor
*************************************************************** */

import java.util.ArrayList;
import java.util.List;

public class HistoricoMensagens {
  private List<Mensagem> mensagens;

  public HistoricoMensagens() {
    mensagens = new ArrayList<>();
  }

  /*
   * ***************************************************************
   * Metodo: adicionarMensagem
   * Funcao: Adiciona uma nova mensagem ao histórico de mensagens.
   * Parametros: Mensagem mensagem - objeto contendo a mensagem a ser armazenada.
   * Retorno: void
   */
  public void adicionarMensagem(Mensagem mensagem) {
    mensagens.add(mensagem);
  }

  /*
   * ***************************************************************
   * Metodo: getMensagens
   * Funcao: Retorna a lista de mensagens armazenadas no histórico.
   * Parametros: nenhum
   * Retorno: List<Mensagem> - lista contendo as mensagens.
   */
  public List<Mensagem> getMensagens() {
    return mensagens;
  }
}
