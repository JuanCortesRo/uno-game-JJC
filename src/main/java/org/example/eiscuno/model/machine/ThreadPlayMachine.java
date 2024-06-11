package org.example.eiscuno.model.machine;

import javafx.scene.image.ImageView;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

public class ThreadPlayMachine extends Thread {
    private Table table;
    private Player machinePlayer;
    private ImageView tableImageView;
    private volatile boolean hasPlayerPlayed;
    private volatile Card currentCard;

    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.hasPlayerPlayed = false;
        this.currentCard = null;
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
                    if (currentCard == null || card.isCompatible(currentCard)) {
                        table.addCardOnTheTable(card);
                        tableImageView.setImage(card.getImage());
                        machinePlayer.removeCard(i);
                        currentCard = card; // Update the last card played
                        currentCard.printColor();
                        cardPlayed = true;
                        putCardOnTheTable(card);
                        break;
                    }
                }

                // If no compatible card could be played, take a new card
                if (!cardPlayed) {
                    System.out.println("La máquina no puede jugar una carta compatible y necesita tomar una nueva carta.");
                }

                hasPlayerPlayed = false;
            }
        }
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
