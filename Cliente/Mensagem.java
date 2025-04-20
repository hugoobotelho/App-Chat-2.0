public class Mensagem {
  private String remetente;
  private String conteudo;
  private String hora;

  public Mensagem(String remetente, String conteudo, String hora) {
    this.remetente = remetente;
    this.conteudo = conteudo;
    this.hora = hora;
  }

  /*
   * ***************************************************************
   * Metodo: getRemetente
   * Funcao: Retorna o nome do remetente da mensagem.
   * Parametros: nenhum
   * Retorno: String - nome do remetente
   */
  public String getRemetente() {
    return remetente;
  }
  /*
   * ***************************************************************
   * Metodo: getConteudo
   * Funcao: Retorna o conteúdo da mensagem.
   * Parametros: nenhum
   * Retorno: String - conteúdo da mensagem
   */

  public String getConteudo() {
    return conteudo;
  }

  /*
   * ***************************************************************
   * Metodo: getHora
   * Funcao: Retorna o horário em que a mensagem foi enviada.
   * Parametros: nenhum
   * Retorno: String - horário da mensagem
   */
  public String getHora() {
    return hora;
  }

  /*
   * ***************************************************************
   * Metodo: toString
   * Funcao: Representa a mensagem como uma string formatada.
   * Parametros: nenhum
   * Retorno: String - mensagem formatada com remetente, conteúdo e hora
   */
  @Override
  public String toString() {
    return remetente + ": " + conteudo + ": " + hora;
  }
}
