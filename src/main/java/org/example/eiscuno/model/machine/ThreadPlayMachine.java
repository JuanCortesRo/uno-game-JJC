package org.example.eiscuno.model.machine;

import javafx.animation.ScaleTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

import static org.example.eiscuno.model.unoenum.EISCUnoEnum.CARD_UNO;

public class ThreadPlayMachine extends Thread {
    private Table table;
    private Player machinePlayer;
    private ImageView tableImageView;
    private Pane gamePane;
    private Circle colorCircle;
    private volatile boolean hasPlayerPlayed;
    private volatile Card currentCard;
    private MachinePlayCallback callback;

    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView, Pane gamePane, Circle colorCircle, MachinePlayCallback callback) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.gamePane = gamePane;
        this.hasPlayerPlayed = false;
        this.currentCard = null;
        this.callback = callback;
        this.colorCircle = colorCircle;
    }

    public void run() {
        while (true){
            if(hasPlayerPlayed){
                try{
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Aquí iría la lógica de colocar la carta
                boolean cardPlayed = false;
                for (int i = 0; i < machinePlayer.getCardsPlayer().size(); i++) {
                    Card card = machinePlayer.getCardsPlayer().get(i);
                    if (currentCard.getValue()==null&&currentCard.getColor()==null){
                        System.out.println("nada");
                    } else if (currentCard.getValue()==null&&currentCard.getColor()!=null) {
                        if (card == null || card.isCompatible(currentCard)){
                            table.addCardOnTheTable(card);
                            tableImageView.setImage(card.getImage());
                            machinePlayer.removeCard(i);
                            currentCard = card;
                            changeBackgroundColor(currentCard);
                            currentCard.printColor();
                            cardPlayed = true;
                            putCardOnTheTable(card);
                            break;
                        }
                    } else{
                        if (card == null || card.isCompatible(currentCard)){
                            table.addCardOnTheTable(card);
                            tableImageView.setImage(card.getImage());
                            machinePlayer.removeCard(i);
                            currentCard = card;
                            changeBackgroundColor(currentCard);
                            currentCard.printColor();
                            cardPlayed = true;
                            putCardOnTheTable(card);
                            break;
                        }
                    }
                }

//                for (int i = 0; i < machinePlayer.getCardsPlayer().size(); i++) {
//                    Card card = machinePlayer.getCardsPlayer().get(i);
//                    if (currentCard == null || card.isCompatible(currentCard)) {
//                        table.addCardOnTheTable(card);
//                        tableImageView.setImage(card.getImage());
//                        machinePlayer.removeCard(i);
//                        currentCard = card;
//                        changeBackgroundColor(currentCard);
//                        currentCard.printColor();
//                        cardPlayed = true;
//                        putCardOnTheTable(card);
//                        break;
//                    }
//                }

                // If no compatible card could be played, take a new card
                if (!cardPlayed) {
                    System.out.println("La máquina no puede jugar una carta compatible y necesita tomar una nueva carta.");
                }

                hasPlayerPlayed = false;

                if (callback != null) {
                    callback.onMachinePlayed();
                }
            }
        }
    }

    public interface MachinePlayCallback {
        void onMachinePlayed();
    }

    public void changeBackgroundColor(Card currentCard) {
        ScaleTransition circleZoom = new ScaleTransition(Duration.seconds(0.4), colorCircle);
        circleZoom.setFromY(0);
        circleZoom.setFromX(0);
        circleZoom.setToY(40);
        circleZoom.setToX(40);

        String cardColor;
        if(currentCard.getColor()==null){
            cardColor = "BLACK";
        } else {
            cardColor = currentCard.getColor();
        }
        System.out.println("color de la ultima carta: " + cardColor);
        switch (cardColor) {
            case "GREEN":
                colorCircle.setStyle("-fx-fill: #54a954");
                break;
            case "BLUE":
                colorCircle.setStyle("-fx-fill: #5252fe");
                break;
            case "RED":
                colorCircle.setStyle("-fx-fill: #ff3737");
                break;
            case "YELLOW":
                colorCircle.setStyle("-fx-fill: #ffbd39");
                break;
            case "BLACK":
                colorCircle.setStyle("-fx-fill: BLACK");
                break;
        }
        circleZoom.play();

        circleZoom.setOnFinished(event -> {
            //System.out.println("Animation finished. Changing background color.");
            switch (cardColor) {
                case "GREEN":
                    gamePane.setStyle("-fx-background-color: #54a954");
                    break;
                case "BLUE":
                    gamePane.setStyle("-fx-background-color: #5252fe");
                    break;
                case "RED":
                    gamePane.setStyle("-fx-background-color: #ff3737");
                    break;
                case "YELLOW":
                    gamePane.setStyle("-fx-background-color: #ffbd39");
                    break;
                case "BLACK":
                    gamePane.setStyle("-fx-background-color: BLACK");
                    break;
            }
        });
    }

    private void putCardOnTheTable(Card card){
        table.addCardOnTheTable(card);
        tableImageView.setImage(card.getImage());
    }

    public void setHasPlayerPlayed(boolean hasPlayerPlayed) {
        this.hasPlayerPlayed = hasPlayerPlayed;
    }

    public void setCurrentCard(Card currentCard) {
        this.currentCard = currentCard;
    }

    public Card getCurrentCard() {
        return currentCard;
    }
}
