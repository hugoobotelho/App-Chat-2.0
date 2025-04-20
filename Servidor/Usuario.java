
import java.net.InetAddress;
import java.util.Objects;

public class Usuario {
  private final String nome;
  private final InetAddress endereco;
  private final int porta;

  public Usuario(String nome, InetAddress endereco, int porta) {
    this.nome = nome;
    this.endereco = endereco;
    this.porta = porta;
  }
  /* ***************************************************************
  * Metodo: getNome
  * Funcao: Retorna o nome do usuário.
  * Parametros: nenhum
  * Retorno: String - nome do usuário
  *************************************************************** */
  public String getNome() {
    return nome;
  }

  /* ***************************************************************
  * Metodo: getEndereco
  * Funcao: Retorna o endereço IP do usuário.
  * Parametros: nenhum
  * Retorno: InetAddress - endereço IP do usuário
  *************************************************************** */
  public InetAddress getEndereco() {
    return endereco;
  }

  /* ***************************************************************
  * Metodo: getPorta
  * Funcao: Retorna a porta associada ao usuário.
  * Parametros: nenhum
  * Retorno: int - porta associada ao usuário
  *************************************************************** */
  public int getPorta() {
    return porta;
  }
  /* ***************************************************************
  * Metodo: equals
  * Funcao: Verifica se dois objetos Usuario são iguais com base
  *         nos atributos nome, endereco e porta.
  * Parametros:
  *    obj - objeto a ser comparado
  * Retorno: boolean - retorna true se os objetos forem iguais,
  *                    false caso contrário
  *************************************************************** */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    Usuario usuario = (Usuario) obj;
    return porta == usuario.porta && endereco.equals(usuario.endereco) && nome.equals(usuario.nome);
  }

  /* ***************************************************************
  * Metodo: hashCode
  * Funcao: Gera o código hash para o objeto Usuario com base
  *         nos atributos nome, endereco e porta.
  * Parametros: nenhum
  * Retorno: int - código hash gerado
  *************************************************************** */
  @Override
  public int hashCode() {
    return Objects.hash(nome, endereco, porta);
  }
}
