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

    // Adiciona uma nova mensagem ao histórico
    public void adicionarMensagem(Mensagem mensagem) {
        mensagens.add(mensagem);
    }

    // Retorna todas as mensagens do histórico
    public List<Mensagem> getMensagens() {
        return mensagens;
    }
}
