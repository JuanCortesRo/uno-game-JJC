package org.example.eiscuno.controller;

import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.game.GameUno;
import org.example.eiscuno.model.machine.ThreadPlayMachine;
import org.example.eiscuno.model.machine.ThreadSingUNOMachine;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

import java.util.Objects;

import static org.example.eiscuno.model.unoenum.EISCUnoEnum.CARD_UNO;

/**
 * Controller class for the Uno game.
 */
public class GameUnoController implements ThreadPlayMachine.MachinePlayCallback {
    @FXML
    private Pane gamePane;
    @FXML
    private Pane centerPane;
    @FXML
    private GridPane gridPaneCardsMachine;
    @FXML
    private GridPane gridPaneCardsPlayer;
    @FXML
    private ImageView tableImageView;
    @FXML
    private Button exitButton;
    @FXML
    private Button cardsButton;
    @FXML
    private Circle colorCircle;

    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;
    private GameUno gameUno;
    private int posInitCardToShow;
    private ThreadSingUNOMachine threadSingUNOMachine;
    private ThreadPlayMachine threadPlayMachine;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        System.out.println("- - - - - TURNO JUGADOR - - - - -");
        initVariables();
        this.gameUno.startGame();
        printCardsHumanPlayer();
        printCardsMachine();
        setupGridPane();

        threadSingUNOMachine = new ThreadSingUNOMachine(this.humanPlayer.getCardsPlayer());
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
        t.start();

        threadPlayMachine = new ThreadPlayMachine(this.table, this.machinePlayer, this.tableImageView, this.gamePane, this.colorCircle, this);
        threadPlayMachine.start();

        cardsButton.setOnMouseEntered(event -> nodeZoom(true,cardsButton,1.1));
        cardsButton.setOnMouseExited(event -> nodeZoom(false,cardsButton,1.1));
        exitButton.setOnMouseEntered(event -> nodeZoom(true,exitButton, 1.2));
        exitButton.setOnMouseExited(event -> nodeZoom(false,exitButton,1.2));
    }

    /**
     * Initializes the variables for the game.
     */
    private void initVariables() {
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.deck = new Deck();
        this.table = new Table();
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.posInitCardToShow = 0;
    }

    /**
     * Prints the human player's cards on the grid pane.
     */
    private void printCardsHumanPlayer() {
        this.gridPaneCardsPlayer.getChildren().clear();
        Card[] currentVisibleCardsHumanPlayer = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            Card card = currentVisibleCardsHumanPlayer[i];
            ImageView cardImageView = card.getCard();

            cardImageView.setOnMouseEntered(event -> nodeZoom(true,cardImageView,1.2));
            cardImageView.setOnMouseExited(event -> nodeZoom(false,cardImageView,1.2));

            cardImageView.setOnMouseClicked((MouseEvent event) -> {
                // Aqui deberian verificar si pueden en la tabla jugar esa carta
                if (Objects.equals(card.getValue(), "EAT4")) {
                    addChangeColorButtons(card);
                    System.out.println("EN ESTE MOMENTO EL ENEMIGO DEBERÍA DE COMER 4");
                } else if (Objects.equals(card.getValue(), "NEWCOLOR")) {
                    addChangeColorButtons(card);
                    System.out.println("EN ESTE MOMENTO DEBERÍA CAMBIAR DE COLOR");
                } else if (card.getValue() != null && card.getColor() != null) {
                    if (threadPlayMachine.getCurrentCard() == null || threadPlayMachine.getCurrentCard().isCompatible(card)) {
                        if (Objects.equals(card.getValue(), "EAT2")) {
                            playWithThe(card);
                            System.out.println("EN ESTE MOMENTO EL ENEMIGO DEBERÍA DE COMER 2");
                        } else if (Objects.equals(card.getValue(), "REVERSE")) {
                            playWithThe(card);
                            System.out.println("EN EL JUEGO CAMBIA DE SENTIDO");
                        } else if (Objects.equals(card.getValue(), "SKIP")) {
                            playWithThe(card);
                            System.out.println("EN ESTE MOMENTO EL ENEMIGO PIERDE TURNO");
                        } else {
                            playWithThe(card);
                            System.out.println("NORMAL");
                        }
                    } else {
                        System.out.println("No puedes jugar esta carta.");
                    }
                }
            });

            this.gridPaneCardsPlayer.add(cardImageView, i, 0);
        }
    }

    @Override
    public void onMachinePlayed() {
        Platform.runLater(this::printCardsMachine);
        nodeZoom(false,tableImageView, 1.2);
        setupGridPane();
        System.out.println("- - - - - TURNO JUGADOR - - - - -");
    }

    private void playWithThe(Card card) {
        gameUno.playCard(card);
        tableImageView.setImage(card.getImage());
        nodeZoom(false,tableImageView, 1.2);
        humanPlayer.removeCard(findPosCardsHumanPlayer(card));
        threadPlayMachine.setHasPlayerPlayed(true);
        threadPlayMachine.setCurrentCard(card);
        threadPlayMachine.changeBackgroundColor(card);
        card.printColor();
        printCardsHumanPlayer();
    }

    private void setButtonProps(Button button, int layoutX, int layoutY, String color, int rotation){
        button.setPrefSize(80, 80);
        button.setLayoutX(layoutX);
        button.setLayoutY(layoutY);
        button.setStyle("-fx-background-radius: 5 5 60 5; -fx-background-color: "+color+";-fx-rotate: "+rotation);
    }

    private void addChangeColorButtons(Card card) {
        Button redButton = new Button();
        Button blueButton = new Button();
        Button yellowButton = new Button();
        Button greenButton = new Button();
        setButtonProps(redButton, 49,0, "#ff3737",180);
        setButtonProps(blueButton, 134,0, "#5252fe",270);
        setButtonProps(yellowButton, 49,85, "#ffbd39",90);
        setButtonProps(greenButton, 134,85, "#54a954",0);
        colorButtonsAnimations(true,redButton,blueButton,yellowButton,greenButton);

        redButton.setOnMouseClicked(event -> {
            card.setColor("RED");
            playWithThe(card);
            colorButtonsAnimations(false,redButton,blueButton,yellowButton,greenButton);
        });

        yellowButton.setOnMouseClicked(event -> {
            card.setColor("YELLOW");
            playWithThe(card);
            colorButtonsAnimations(false,redButton,blueButton,yellowButton,greenButton);

        });

        greenButton.setOnMouseClicked(event -> {
            card.setColor("GREEN");
            playWithThe(card);
            colorButtonsAnimations(false,redButton,blueButton,yellowButton,greenButton);
        });

        blueButton.setOnMouseClicked(event -> {
            card.setColor("BLUE");
            playWithThe(card);
            colorButtonsAnimations(false,redButton,blueButton,yellowButton,greenButton);
        });

        // Add buttons to the centerPane
        centerPane.getChildren().addAll(redButton, yellowButton, greenButton, blueButton);
    }

    private void printCardsMachine() {
        this.gridPaneCardsMachine.getChildren().clear();
        Card[] currentVisibleCardsMachinePlayer = this.machinePlayer.getCardsPlayer().toArray(new Card[0]);
        this.gridPaneCardsMachine.setAlignment(Pos.CENTER);

        for (int i = 0; i < currentVisibleCardsMachinePlayer.length; i++) {
            Card card = currentVisibleCardsMachinePlayer[i];
            //ImageView machineCardImageView = card.getCard();
            ImageView machineCardImageView = new ImageView(new Image(String.valueOf(getClass().getResource(CARD_UNO.getFilePath()))));
            machineCardImageView.setTranslateX(-(currentVisibleCardsMachinePlayer.length/0.75));

            machineCardImageView.setFitHeight(110);
            machineCardImageView.setFitWidth(74);

            StackPane stackPane = new StackPane(machineCardImageView);
            stackPane.setAlignment(Pos.CENTER);

            this.gridPaneCardsMachine.add(stackPane, i, 0);
        }
    }

    private void setupGridPane() {
        int numColumns = machinePlayer.getCardsPlayer().size();
        double gridPaneWidth = 353;
        double columnWidth = gridPaneWidth / numColumns;

        gridPaneCardsMachine.setPrefWidth(gridPaneWidth);
        gridPaneCardsMachine.setMinWidth(gridPaneWidth);
        gridPaneCardsMachine.setMaxWidth(gridPaneWidth);

        gridPaneCardsMachine.getColumnConstraints().clear();

        for (int i = 0; i < numColumns; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPrefWidth(columnWidth);
            column.setMinWidth(columnWidth);
            column.setMaxWidth(columnWidth);
            gridPaneCardsMachine.getColumnConstraints().add(column);
        }
    }

    /**
     * Controls the zoom animation of a node
     */
    private void nodeZoom(boolean doZoom, Node node,double to){
        ScaleTransition translateIn = new ScaleTransition(Duration.seconds(0.2), node);
        translateIn.setToX(to);
        translateIn.setToY(to);
        ScaleTransition translateOut = new ScaleTransition(Duration.seconds(0.2), node);
        translateOut.setFromY(to);
        translateOut.setFromX(to);
        translateOut.setToX(1);
        translateOut.setToY(1);
        if (doZoom){
            translateIn.play();
        }else {
            translateOut.play();
        }
    }

    /**
     * Controls the animations of the colors selection for the +4 and wild card
     */
    private void colorButtonsAnimations(boolean isEnter, Button... buttons) {
        SequentialTransition sequentialTransition  = new SequentialTransition();

        for (Button button : buttons) {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.15), button);
            ScaleTransition scaleTransition1 = new ScaleTransition(Duration.seconds(0.05),button);

            if (isEnter) {
                scaleTransition.setFromX(0);
                scaleTransition.setFromY(0);
                scaleTransition.setToX(1.2);
                scaleTransition.setToY(1.2);
                scaleTransition1.setFromX(1.2);
                scaleTransition1.setFromY(1.2);
                scaleTransition1.setToX(1);
                scaleTransition1.setToY(1);
            } else {
                scaleTransition.setFromX(1);
                scaleTransition.setFromY(1);
                scaleTransition.setToX(0);
                scaleTransition.setToY(0);
                scaleTransition.setOnFinished(event -> {
                    centerPane.getChildren().remove(button);
                });
            }

            sequentialTransition.getChildren().addAll(scaleTransition, scaleTransition1);
        }

        sequentialTransition.play();
    }

    /**
     * Finds the position of a specific card in the human player's hand.
     *
     * @param card the card to find
     * @return the position of the card, or -1 if not found
     */
    private Integer findPosCardsHumanPlayer(Card card) {
        for (int i = 0; i < this.humanPlayer.getCardsPlayer().size(); i++) {
            if (this.humanPlayer.getCardsPlayer().get(i).equals(card)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Handles the "Back" button action to show the previous set of cards.
     *
     * @param event the action event
     */
    @FXML
    void onHandleBack(ActionEvent event) {
        if (this.posInitCardToShow > 0) {
            this.posInitCardToShow--;
            printCardsHumanPlayer();
        }
    }

    /**
     * Handles the "Next" button action to show the next set of cards.
     *
     * @param event the action event
     */
    @FXML
    void onHandleNext(ActionEvent event) {
        if (this.posInitCardToShow < this.humanPlayer.getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            printCardsHumanPlayer();
        }
    }

    /**
     * Handles the action of taking a card.
     *
     * @param event the action event
     */
    @FXML
    void onHandleTakeCard(ActionEvent event) {
        // Implement logic to take a card here
    }

    /**
     * Handles the action of close the game.
     *
     * @param event the action event
     */
    @FXML
    void onHandleExit(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    /**
     * Handles the action of saying "Uno".
     *
     * @param event the action event
     */
    @FXML
    void onHandleUno(ActionEvent event) {
        // Implement logic to handle Uno event here
    }
}
