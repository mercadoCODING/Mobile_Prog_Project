package com.example.game_project;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.GridLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CardGame";

    private List<Card> cards;
    private FrameLayout firstCard, secondCard;
    private Card firstCardData, secondCardData;
    private boolean isFlipping = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeCards();
        setupGrid();
    }

    private void initializeCards() {
        cards = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            cards.add(new Card(i));
            cards.add(new Card(i));
        }
        Collections.shuffle(cards);
        Log.d(TAG, "Cards initialized and shuffled.");
    }

    private void setupGrid() {
        GridLayout cardGrid = findViewById(R.id.card_grid);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < cards.size(); i++) {
            final FrameLayout cardLayout = (FrameLayout) inflater.inflate(R.layout.card_item, cardGrid, false);
            cardLayout.setTag(cards.get(i));

            // Set initial position (e.g., center of the screen) as the starting point
            cardLayout.setTranslationX(0);  // Adjust this as needed for the deck's center position
            cardLayout.setTranslationY(0);

            cardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isFlipping && v != firstCard) {
                        flipCard((FrameLayout) v);
                    }
                }
            });
            cardGrid.addView(cardLayout);

            // Load the distribution animation
            Animation distributeAnimation = AnimationUtils.loadAnimation(this, R.anim.distribute_card);

            // Add a delay to create the staggered effect for each card
            distributeAnimation.setStartOffset(i * 100L);  // 100ms delay between each card

            // Start the animation
            cardLayout.startAnimation(distributeAnimation);
        }
        Log.d(TAG, "Grid setup complete with distribution animation.");
    }

    private void flipCard(FrameLayout card) {
        Card cardData = (Card) card.getTag();
        if (cardData == null) {
            Log.e(TAG, "Card data is null. Skipping flip.");
            return;
        }
        if (cardData.isMatched()) {
            Log.d(TAG, "Card already matched. Ignoring.");
            return;
        }

        View cardFront = card.findViewById(R.id.card_front);
        View cardBack = card.findViewById(R.id.card_back);

        if (cardFront == null || cardBack == null) {
            Log.e(TAG, "Card views not found.");
            return;
        }

        AnimatorSet flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_out);
        AnimatorSet flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_in);

        flipOut.setTarget(cardFront);
        flipIn.setTarget(cardBack);
        flipOut.start();
        flipIn.start();

        cardFront.setVisibility(View.GONE);
        cardBack.setVisibility(View.VISIBLE);

        if (firstCard == null) {
            firstCard = card;
            firstCardData = cardData;
            Log.d(TAG, "First card flipped.");
        } else if (secondCard == null) {
            secondCard = card;
            secondCardData = cardData;
            isFlipping = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkForMatch();
                }
            }, 1000);
        }
    }

    private void checkForMatch() {
        if (firstCardData != null && secondCardData != null) {
            if (firstCardData.getId() == secondCardData.getId()) {
                firstCardData.setMatched(true);
                secondCardData.setMatched(true);
                Log.d(TAG, "Cards matched.");
            } else {
                Log.d(TAG, "Cards did not match. Flipping back.");
                flipBack(firstCard);
                flipBack(secondCard);
            }
        } else {
            Log.e(TAG, "One of the cards is null in checkForMatch.");
        }

        firstCard = null;
        secondCard = null;
        firstCardData = null;
        secondCardData = null;
        isFlipping = false;
    }

    private void flipBack(FrameLayout card) {
        View cardFront = card.findViewById(R.id.card_front);
        View cardBack = card.findViewById(R.id.card_back);

        if (cardFront == null || cardBack == null) {
            Log.e(TAG, "Card views not found in flipBack.");
            return;
        }

        AnimatorSet flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_out);
        AnimatorSet flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flip_in);

        flipOut.setTarget(cardBack);
        flipIn.setTarget(cardFront);
        flipOut.start();
        flipIn.start();

        cardBack.setVisibility(View.GONE);
        cardFront.setVisibility(View.VISIBLE);
    }
}

