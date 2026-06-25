package com.meu.ourigame;

import com.meu.ourigame.model.JogoOuri;
import com.meu.ourigame.model.Tabuleiro;
import com.meu.ourigame.network.Client;
import com.meu.ourigame.network.NetworkConnection;
import com.meu.ourigame.network.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class OuriGameUI extends Application {

    private Stage stage;

    private JogoOuri jogo;
    private Button[][] botoesCasas;
    private Label lblJogadorAtual;
    private Label lblDepositoJ1;
    private Label lblDepositoJ2;
    private Label lblEstadoRede;

    private NetworkConnection connection;
    private boolean modoRede = false;
    private int jogadorLocal = -1;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        mostrarMenuInicial();
    }

    private void mostrarMenuInicial() {
        Label titulo = new Label("Jogo do Ouri");
        titulo.setStyle("-fx-font-size: 34px; -fx-font-weight: bold;");

        Label subtitulo = new Label("Escolhe o modo de jogo");
        subtitulo.setStyle("-fx-font-size: 18px;");

        Button btnLocal = new Button("Jogar Local");
        btnLocal.setPrefWidth(260);
        btnLocal.setOnAction(e -> iniciarJogoLocal());

        Button btnServidor = new Button("Criar Servidor / Jogador 1");
        btnServidor.setPrefWidth(260);
        btnServidor.setOnAction(e -> iniciarServidor());

        TextField txtIp = new TextField();
        txtIp.setPromptText("IP do servidor");
        txtIp.setMaxWidth(260);

        Button btnCliente = new Button("Entrar como Cliente / Jogador 2");
        btnCliente.setPrefWidth(260);
        btnCliente.setOnAction(e -> {
            String ip = txtIp.getText().trim();

            if (ip.isEmpty()) {
                mostrarMensagem("IP obrigatório", "Escreve o IP do servidor.");
                return;
            }

            iniciarCliente(ip);
        });

        VBox menu = new VBox(15, titulo, subtitulo, btnLocal, btnServidor, txtIp, btnCliente);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(30));

        Scene scene = new Scene(menu, 850, 500);
        stage.setTitle("Ouri - Menu Inicial");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

    private void iniciarJogoLocal() {
        modoRede = false;
        jogadorLocal = -1;
        connection = null;
        mostrarJogo();
    }

    private void iniciarServidor() {
        modoRede = true;
        jogadorLocal = 0;

        connection = new Server();
        connection.setOnMessage(this::receberMensagem);
        connection.start();

        mostrarJogo();
    }

    private void iniciarCliente(String ipServidor) {
        modoRede = true;
        jogadorLocal = 1;

        connection = new Client(ipServidor);
        connection.setOnMessage(this::receberMensagem);
        connection.start();

        mostrarJogo();
    }

    private void mostrarJogo() {
        jogo = new JogoOuri();
        botoesCasas = new Button[2][Tabuleiro.NUM_CASAS];

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        Label titulo = new Label("Jogo do Ouri");
        titulo.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        lblJogadorAtual = new Label();
        lblJogadorAtual.setStyle("-fx-font-size: 18px;");

        lblEstadoRede = new Label();
        lblEstadoRede.setStyle("-fx-font-size: 14px;");

        VBox topo = new VBox(10, titulo, lblJogadorAtual, lblEstadoRede);
        topo.setAlignment(Pos.CENTER);

        root.setTop(topo);
        root.setCenter(criarTabuleiro());
        root.setBottom(criarRodape());

        atualizarInterface();

        Scene scene = new Scene(root, 850, 500);
        stage.setTitle("Ouri");
        stage.setScene(scene);
        stage.show();
    }

    private GridPane criarTabuleiro() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(30));

        lblDepositoJ1 = new Label();
        lblDepositoJ2 = new Label();

        lblDepositoJ1.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        lblDepositoJ2.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        VBox deposito1 = new VBox(8, new Label("Depósito J1"), lblDepositoJ1);
        VBox deposito2 = new VBox(8, new Label("Depósito J2"), lblDepositoJ2);

        deposito1.setAlignment(Pos.CENTER);
        deposito2.setAlignment(Pos.CENTER);

        grid.add(deposito1, 0, 0, 1, 2);

        for (int i = 0; i < Tabuleiro.NUM_CASAS; i++) {
            Button btn = criarBotaoCasa(0, i);
            botoesCasas[0][i] = btn;
            grid.add(btn, i + 1, 0);
        }

        for (int i = 0; i < Tabuleiro.NUM_CASAS; i++) {
            Button btn = criarBotaoCasa(1, i);
            botoesCasas[1][i] = btn;
            grid.add(btn, i + 1, 1);
        }

        grid.add(deposito2, 7, 0, 1, 2);

        return grid;
    }

    private Button criarBotaoCasa(int jogador, int casa) {
        Button btn = new Button();
        btn.setPrefSize(85, 85);
        btn.setStyle("-fx-font-size: 22px; -fx-background-radius: 45;");
        btn.setOnAction(e -> jogarCasa(jogador, casa));
        return btn;
    }

    private HBox criarRodape() {
        Button btnNovoJogo = new Button("Novo Jogo");
        Button btnMenu = new Button("Voltar ao Menu");

        btnNovoJogo.setOnAction(e -> {
            jogo = new JogoOuri();
            atualizarInterface();

            if (modoRede && connection != null) {
                connection.send("NOVO");
            }
        });

        btnMenu.setOnAction(e -> {
            if (modoRede && connection != null) {
                connection.send("SAIR_MENU");
            }

            fecharLigacaoRede();
            mostrarMenuInicial();
        });

        HBox rodape = new HBox(15, btnNovoJogo, btnMenu);
        rodape.setAlignment(Pos.CENTER);
        rodape.setPadding(new Insets(20));

        return rodape;
    }

    private void jogarCasa(int jogador, int casa) {
        if (modoRede && jogador != jogadorLocal) {
            mostrarMensagem("Jogada inválida", "Só podes jogar nas tuas casas.");
            return;
        }

        if (modoRede && jogo.getJogadorAtual() != jogadorLocal) {
            mostrarMensagem("Aguarda", "Ainda não é a tua vez.");
            return;
        }

        if (jogador != jogo.getJogadorAtual()) {
            mostrarMensagem("Jogada inválida", "Não é a vez desse jogador.");
            return;
        }

        boolean jogadaValida = jogo.jogar(casa);

        if (!jogadaValida) {
            mostrarMensagem("Jogada inválida", "Escolhe uma casa com mais de 1 semente.");
            return;
        }

        atualizarInterface();

        if (modoRede && connection != null) {
            connection.send("JOGADA:" + casa);
        }

        verificarFimDeJogo();
    }

    private void receberMensagem(String mensagem) {
        Platform.runLater(() -> {
            if (mensagem.startsWith("JOGADA:")) {
                int casa = Integer.parseInt(mensagem.substring(7));

                boolean jogadaValida = jogo.jogar(casa);

                if (jogadaValida) {
                    atualizarInterface();
                    verificarFimDeJogo();
                }
            }

            if (mensagem.equals("NOVO")) {
                jogo = new JogoOuri();
                atualizarInterface();
            }

            if (mensagem.equals("SAIR_MENU")) {
                mostrarMensagem("Ligação terminada", "O outro jogador voltou ao menu.");
                fecharLigacaoRede();
                mostrarMenuInicial();
            }
        });
    }

    private void verificarFimDeJogo() {
        if (jogo.isFimDeJogo()) {
            mostrarMensagem("Fim de jogo", jogo.getVencedor());
        }
    }

    private void atualizarInterface() {
        Tabuleiro tabuleiro = jogo.getTabuleiro();

        for (int jogador = 0; jogador < 2; jogador++) {
            for (int casa = 0; casa < Tabuleiro.NUM_CASAS; casa++) {
                botoesCasas[jogador][casa].setText(String.valueOf(tabuleiro.getCasa(jogador, casa)));
            }
        }

        lblDepositoJ1.setText(String.valueOf(tabuleiro.getDeposito(0)));
        lblDepositoJ2.setText(String.valueOf(tabuleiro.getDeposito(1)));

        lblJogadorAtual.setText("Vez do Jogador " + (jogo.getJogadorAtual() + 1));

        if (!modoRede) {
            lblEstadoRede.setText("Modo local");
        } else if (jogadorLocal == 0) {
            lblEstadoRede.setText("Modo rede: Servidor / Jogador 1");
        } else {
            lblEstadoRede.setText("Modo rede: Cliente / Jogador 2");
        }
    }

    private void mostrarMensagem(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void fecharLigacaoRede() {
        if (connection != null) {
            connection.close();
            connection = null;
        }

        modoRede = false;
        jogadorLocal = -1;
    }
}