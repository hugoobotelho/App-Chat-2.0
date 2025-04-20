public class Mensagem {
    private String remetente;
    private String conteudo;
    private String hora;

    public Mensagem(String remetente, String conteudo, String hora) {
        this.remetente = remetente;
        this.conteudo = conteudo;
        this.hora = hora;
    }

    public String getRemetente() {
        return remetente;
    }

    public String getConteudo() {
        return conteudo;
    }

    public String getHora() {
        return hora;
    }

    @Override
    public String toString() {
        return remetente + ": " + conteudo + ": " + hora;
    }
}
