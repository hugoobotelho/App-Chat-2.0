
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class TelaInicio {
    private VBox layout = new VBox(30); // Layout da tela inicial

    public TelaInicio(Principal app) {
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Configuração do Título
        Label labelTitulo = new Label("Seja Bem Vindo!");
        labelTitulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        labelTitulo.setAlignment(Pos.CENTER);

        // Configuração do Campo IP com Placeholder Persistente
        // TextField campoIP = criarCampoComPlaceholder("Digite o IP do servidor");

        // Configuração do Campo Usuário com Placeholder Persistente
        TextField campoUsuario = criarCampoComPlaceholder("Digite seu nome de usuario");

        // Configuração do Botão Entrar
        Button botaoEntrar = new Button("Entrar");
        botaoEntrar.setStyle(
                "-fx-background-color: #E5AF18; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 16px; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-pref-width: 300px; -fx-padding: 10px; -fx-cursor: hand;");
        // botaoEntrar.setOnAction(e -> {
        // app.setNomeUsuario(campoUsuario.getText());
        // TelaMeusGrupos telaMeusGrupos = new TelaMeusGrupos(app);
        // app.getRoot().getChildren().setAll(telaMeusGrupos.getLayout());
        // });
        // Adicionar ação de verificação ao clicar em "Entrar"
        botaoEntrar.setOnAction(e -> {
            // String ipServidor = campoIP.getText().trim();
            String nomeUsuario = campoUsuario.getText().trim();

            // Verifica se o IP do servidor e o nome de usuário estão preenchidos
            if (nomeUsuario.isEmpty()) {
                mostrarMensagemErro("Preencha o nome.");
                return;
            }

            // Verifica se o IP do servidor tem formato válido (exemplo básico de IP)
            // if (!isIPValido(ipServidor)) {
            // mostrarMensagemErro("IP do servidor inválido.");
            // return;
            // }

            // Define os dados para a aplicação
            app.setNomeUsuario(nomeUsuario);
            // app.setIpServidor(ipServidor);
            // app.criarClientes(ipServidor, nomeUsuario);
            // app.criarClientes(nomeUsuario); //talvez nao precise criar aqui

            // Tenta carregar a próxima tela (Tela de Grupos)
            // TelaMeusGrupos telaMeusGrupos = new TelaMeusGrupos(app);
            // app.getRoot().getChildren().setAll(telaMeusGrupos.getLayout());
            app.getRoot().getChildren().setAll(app.getTelaMeusGrupos().getLayout());

        });

        // VBox inputVBox = new VBox(10, campoIP,campoUsuario);
        VBox inputVBox = new VBox(10, campoUsuario);

        // Adicionando elementos ao layout
        layout.getChildren().addAll(labelTitulo, inputVBox, botaoEntrar);
        layout.setAlignment(Pos.CENTER);
        inputVBox.setAlignment(Pos.CENTER);

        // Centraliza o VBox no root
        StackPane root = new StackPane();
        root.getChildren().add(layout);
        root.setAlignment(layout, Pos.CENTER);

        // Remover foco inicial dos inputs
        Platform.runLater(() -> layout.requestFocus());
    }

    // /**
    // * Valida o formato básico de um IP (ex: 192.168.0.1).
    // *
    // * @param ip O IP a ser validado.
    // * @return true se o IP for válido, falso caso contrário.
    // */
    // private boolean isIPValido(String ip) {
    // // Exemplo simples de verificação de formato de IP
    // String regex =
    // "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    // return ip.matches(regex);
    // }

    /* ***************************************************************
    * Metodo: mostrarMensagemErro
    * Funcao: Exibe uma mensagem de erro ao usuário por um tempo limitado (3 segundos).
    * Parametros: String mensagem - a mensagem de erro a ser exibida
    * Retorno: void
    *************************************************************** */
    private void mostrarMensagemErro(String mensagem) {
        Label mensagemErro = new Label(mensagem);
        mensagemErro.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-font-weight: bold;");
        layout.getChildren().add(mensagemErro);

        // Remover mensagem de erro após 3 segundos
        Platform.runLater(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // Aguardar 3 segundos
                    Platform.runLater(() -> layout.getChildren().remove(mensagemErro));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    /* ***************************************************************
    * Metodo: criarCampoComPlaceholder
    * Funcao: Cria um campo de texto (TextField) com placeholder persistente, que não desaparece ao focar.
    * Parametros: String placeholder - o texto que será exibido como placeholder
    * Retorno: TextField - o campo de texto configurado
    *************************************************************** */
    private TextField criarCampoComPlaceholder(String placeholder) {
        TextField campo = new TextField();
        campo.setPromptText(placeholder);
        campo.setMaxWidth(300);
        campo.setStyle("-fx-background-color: #E1E0E0; -fx-font-size: 16px; -fx-padding: 10px;");

        // Listener para controlar o comportamento do placeholder
        campo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && campo.getText().isEmpty()) {
                campo.setPromptText(placeholder); // Reaplica o placeholder ao perder o foco
            } else if (newVal && campo.getText().isEmpty()) {
                campo.setPromptText(""); // Limpa o placeholder ao focar
            }
        });

        return campo;
    }
  /* ***************************************************************
    * Metodo: getLayout
    * Funcao: Retorna o layout principal da tela de início.
    * Parametros: nenhum
    * Retorno: VBox - o layout principal da tela de início
    *************************************************************** */
    public VBox getLayout() {
        return layout;
    }
}
