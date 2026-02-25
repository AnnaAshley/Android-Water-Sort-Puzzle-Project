package com.example.finalproject;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TestTubeView selectedTube = null;
    private int MaxLayer = 4;
    private int numberOfLevels = 1;
    private int levelNumber = 1;
    private int moveCount = 0;
    private  TextView levelCountV;
    private TextView moveCountV;
    LinearLayout container;
    private boolean solving = false;
    private TextView winnerView;

    ArrayList<TestTubeView> ttviews = new ArrayList<>();

    ArrayList<ArrayList<Integer>> layers = new ArrayList<>();
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //set up buttons
        Button resetB = findViewById(R.id.restartButton);
        Button solveB = findViewById(R.id.solveButton);
        Button prevB = findViewById(R.id.previousButton);
        Button nextB = findViewById(R.id.nextButton);

        //set up text views
        levelCountV = findViewById(R.id.puzzleNumber);
        moveCountV = findViewById(R.id.moveCountView);
        winnerView = findViewById(R.id.WinnerView);

        //set up test tube container
        container = findViewById(R.id.testTubeContainer);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean("is_first_launch", true);

        if (isFirstLaunch) {
            dbExecutor.execute(() -> {
                insertInitialLevels(); // now runs off the main thread
                prefs.edit().putBoolean("is_first_launch", false).apply();
                updateLevelCountFromDb();
                runOnUiThread(()->{
                    loadLevel(levelNumber);
                    updateLevelText();
                });
            });
        } else {
            dbExecutor.execute(() -> {
                updateLevelCountFromDb();
                runOnUiThread(() -> {
                    loadLevel(levelNumber);
                    updateLevelText();
                });
            });

        }


        //set up button listeners
        //reset button
        resetB.setOnClickListener(v -> resetPuzzle());
        //solve button
        solveB.setOnClickListener(v -> onClickSolver());
        //next button
        nextB.setOnClickListener(v -> onClickNext());
        //previous button
        prevB.setOnClickListener(v -> onClickPrevious());

    }

    //onClick for tube
    private void onTubeClicked(TestTubeView clickedTube){
        Gson gson = new Gson();
        if (selectedTube == null){ //tube clicked for first time
            selectedTube = clickedTube;
            selectedTube.animate().translationY(-50).setDuration(200).start(); //lift clicked tube
        } else if (selectedTube == clickedTube) {
            //reclicked tube run reverse animation
            selectedTube.animate().translationY(0).setDuration(200).start(); //drop clicked tube
            selectedTube = null;
        } else{ //clicked a second tube -- run game logic
            int fromTube = ttviews.indexOf(selectedTube);
            int toTube = ttviews.indexOf(clickedTube);
            ArrayList<Integer> fromLayers =  new ArrayList<>(layers.get(fromTube));
            ArrayList<Integer> toLayers = new ArrayList<>(layers.get(toTube));

            //if tube 1 has liquid and tube 2 has space
            if(!fromLayers.isEmpty() && toLayers.size() < MaxLayer) {
                int colorToPour = fromLayers.get(fromLayers.size()-1);

                //check if valid pour
                if(toLayers.isEmpty() || toLayers.get(toLayers.size() -1).equals(colorToPour)){
                    int count = 0;
                    //count how many layers on top are the same color for the source
                    for (int i = fromLayers.size()-1; i>=0; i--){
                        if(fromLayers.get(i).equals(colorToPour)){
                            count++;
                        }else break;
                    }
                    //check how much will fit in pour
                    int space = MaxLayer - toLayers.size();
                    int pourAmount = Math.min(count, space);
                    //determine which direction to animate the pour
                    float pourRotationAngle = (ttviews.indexOf(clickedTube) >ttviews.indexOf(selectedTube)) ? 20f : -20f;

                    //animate pouring the layers and update tubes
                    selectedTube.animate()
                            .translationY(-80f)
                            .rotation(pourRotationAngle)
                            .setDuration(300)
                            .withEndAction(()->{
                                for(int i=0; i<pourAmount; i++){
                                    fromLayers.remove(fromLayers.size()-1);
                                    toLayers.add(colorToPour);
                                }
                                //update layers
                                layers.set(fromTube, fromLayers);
                                layers.set(toTube, toLayers);
                                moveCount++;

                                selectedTube.setColorLayers(fromLayers);
                                clickedTube.setColorLayers(toLayers);


                                runOnUiThread(this::updateMoveCounterUI);

                                //update database current layers
                                dbExecutor.execute(() -> {
                                    LevelDatabase db = LevelDatabase.getInstance(getApplicationContext());
                                    LevelEntity level = db.levelDao().getLevel(levelNumber);
                                    level.currentLayers = gson.toJson(layers);
                                    level.moveCount =moveCount;
                                    db.levelDao().updateLevel(level);
                                });

                                //return tube after pour
                                selectedTube.animate()
                                    .translationY(0f)
                                    .rotation(0f)
                                    .setDuration(300)
                                    .withEndAction(()->{
                                        //check win condition
                                        if(checkWinCondition()) {
                                            Toast.makeText(this,"You Won!", Toast.LENGTH_LONG).show();
                                            winnerView.setVisibility(VISIBLE);

                                            //update database with win
                                            dbExecutor.execute(() -> {
                                                LevelDatabase db = LevelDatabase.getInstance(getApplicationContext());
                                                db.levelDao().setLevelCompleted(levelNumber, true);
                                            });


                                        }
                                        selectedTube = null;
                                    }).start();

                            }).start();
                } else{
                    //invalid move
                    if(!solving){
                        Toast.makeText(this,"Invalid Move!", Toast.LENGTH_SHORT).show(); // not toasting invalid move of algorithm

                    }

                    selectedTube.animate().translationY(0).setDuration(200).start(); //drop clicked tube
                    selectedTube = null;
                }
            }

        }
    }

    //check win condition. All tubes have a maxLayer number of layers of the same color or are empty
    private boolean checkWinCondition(){
        for (ArrayList<Integer> tube : layers) {
            if(tube.isEmpty()) continue;
            if (tube.size() != MaxLayer) return false;
            int color = tube.get(0);
            for (int c : tube) {
                if (c != color) return false;
            }
        }
        return true;
    }


    //reset puzzle
    private void resetPuzzle() {

        Gson gson = new Gson();
        //update database to clear progress
        dbExecutor.execute(() -> {
            LevelDatabase db = LevelDatabase.getInstance(getApplicationContext());
            LevelEntity level = db.levelDao().getLevel(levelNumber);
            //check if level is null
            if(level == null) return;

            level.moveCount = 0;
            level.isCompleted = false;
            level.currentLayers = null; //clear progress
            db.levelDao().updateLevel(level);

            Type type = new TypeToken<List<List<Integer>>>() {}.getType();
            ArrayList<ArrayList<Integer>> resetLayers = gson.fromJson(level.initialLayers, type);

            runOnUiThread(()->{
                moveCount = 0;
                updateMoveCounterUI();
                level.moveCount =0;
                if (selectedTube != null) {
                    selectedTube.animate().translationY(0).setDuration(200).start();
                    selectedTube = null; //reseting the section
                }
                //reset winnerView to 0
                winnerView.setVisibility(INVISIBLE);
                layers.clear();
                layers.addAll(resetLayers);
                for (int i = 0; i <ttviews.size(); i++) {
                    ttviews.get(i).setMaxLayers(MaxLayer);
                    ttviews.get(i).setColorLayers(layers.get(i));
                }
            });
        });
        Toast.makeText(this,"Reset Clicked!", Toast.LENGTH_SHORT).show();
        solving = false;
        //reset YouWon view
    }
    private void onClickSolver(){
        Toast.makeText(this,"Solving", Toast.LENGTH_SHORT).show();
        solving = true;

        dbExecutor.execute(()-> {
            //copy of current layers for solver
            ArrayList<ArrayList<Integer>> layersCopy = new ArrayList<>();
            for (ArrayList<Integer> tube : layers) {
                layersCopy.add(new ArrayList<>(tube));
            }

            List<PuzzleSolver.Move> solutionMoves = PuzzleSolver.solve(layersCopy, MaxLayer);
            if (!solutionMoves.isEmpty()) {
                runOnUiThread(()-> playSolutionMoves(solutionMoves));
            } else {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "No solution found", Toast.LENGTH_SHORT).show());
            }
        });

    }

    private void onClickPrevious(){
        //show previous puzzle
        if (levelNumber > 1) {
            levelNumber--;
            loadLevel(levelNumber);
            updateLevelText();

        }else {
            Toast.makeText(this,"Already on first level", Toast.LENGTH_SHORT).show();
        }
    }

    private void onClickNext(){
        //show previous puzzle
        if (levelNumber < numberOfLevels) {
            levelNumber++;
            loadLevel(levelNumber);
            updateLevelText();

        }else {
            Toast.makeText(this,"No more levels", Toast.LENGTH_SHORT).show();
        }
    }

    //updateLevelText
    private void updateLevelText() {
        runOnUiThread(() -> levelCountV.setText("Level " + levelNumber));
    }


    //play solution moves animation
    private void playSolutionMoves(List<PuzzleSolver.Move> moves){
        if (moves.isEmpty()) return;
        final int[] index = {0};
        final Handler handler = new Handler();

        Runnable moveRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] >= moves.size()) return;

                PuzzleSolver.Move move = moves.get(index[0]);
                int from = move.from;
                int to = move.to;

                //simulate clicking tubes from first to 2nd
                onTubeClicked(ttviews.get(from));
                handler.postDelayed(() -> {
                    onTubeClicked(ttviews.get(to));
                    index[0]++;
                    handler.postDelayed(this, 700);
                }, 400);
            }
        };

        handler.post(moveRunnable);

    }
    //load levels from database.
    private void loadLevel(int levelId) {

        dbExecutor.execute(()->{
            LevelEntity level = LevelDatabase.getInstance(getApplicationContext()).levelDao().getLevel(levelId);
            if (level == null) {
                Log.e("MainActivity", "Level not found in database");
                return;
            }
            MaxLayer = level.maxLayers;

            Gson gson = new Gson();

            String jsonLayers = level.currentLayers != null ? level.currentLayers : level.initialLayers;

            Type type = new TypeToken<List<List<Integer>>>() {}.getType();
            ArrayList<ArrayList<Integer>> loadedLayers = gson.fromJson(jsonLayers, type);

            runOnUiThread(()->{
                //clear old game state
                layers.clear();
                ttviews.clear();
                container.removeAllViews(); //load fresh view

                moveCount = level.moveCount;
                updateMoveCounterUI();
                winnerView.setVisibility(level.isCompleted ? VISIBLE : INVISIBLE);

                layers.addAll(loadedLayers);

            for (int i = 0; i < layers.size(); i++) {
                ArrayList<Integer> layer = layers.get(i);
                TestTubeView tube = new TestTubeView(this);
                tube.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(80), dpToPx(272)));
                tube.setColorLayers(layer);
                tube.setMaxLayers(MaxLayer);
                container.addView(tube);
                ttviews.add(tube);

                //set listener of each of the tubes
                tube.setOnClickListener(v -> onTubeClicked(tube));
                }
            });
        });
    }

    //helper method to convert dp to pixels
    private  int dpToPx(int dp){
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale +0.5f);
    }

    //insert initial levels on startup to keep from recreating the levels every time
    private void insertInitialLevels() {
        LevelDatabase db = LevelDatabase.getInstance(getApplicationContext());

        LevelDao levelDao = db.levelDao();

        LevelEntity level1 = new LevelEntity(1, getJsonForLevel1(), "not_started", 4,4);
        LevelEntity level2 = new LevelEntity(2, getJsonForLevel2(), "not_started",5,4);

        levelDao.insertLevel(level1);
        levelDao.insertLevel(level2);
    }

    private void updateLevelCountFromDb() {
        LevelDatabase db = LevelDatabase.getInstance(getApplicationContext());
        numberOfLevels = db.levelDao().getAllLevels().size();
    }

    private void updateMoveCounterUI() {
        if (moveCountV != null) {
            moveCountV.setText("Moves: " + moveCount);
        }
    }


    private String getJsonForLevel1() {
        //create a layers tube for each tube
        ArrayList<ArrayList<Integer>> layers1 = new ArrayList<>();
        ArrayList<Integer> tube1 = new ArrayList<>();
        tube1.add(ColorCode.CYAN.getColorValue()); tube1.add(ColorCode.BLUE.getColorValue()); tube1.add(ColorCode.RED.getColorValue());tube1.add(ColorCode.CYAN.getColorValue());
        ArrayList<Integer> tube2 = new ArrayList<>();
        tube2.add(ColorCode.BLUE.getColorValue()); tube2.add(ColorCode.RED.getColorValue()); tube2.add(ColorCode.CYAN.getColorValue()); tube2.add(ColorCode.BLUE.getColorValue());
        ArrayList<Integer> tube3 = new ArrayList<>();
        tube3.add(ColorCode.RED.getColorValue()); tube3.add(ColorCode.BLUE.getColorValue()); tube3.add(ColorCode.CYAN.getColorValue()); tube3.add(ColorCode.RED.getColorValue());
        ArrayList<Integer> tube4 = new ArrayList<>();

        layers1.add(tube1);
        layers1.add(tube2);
        layers1.add(tube3);
        layers1.add(tube4);

        Gson gson = new Gson();
        return gson.toJson(layers1);
    }

    //puzzle with more segments per bottle
    private String getJsonForLevel2() {
        //create a layers tube for each tube
        ArrayList<ArrayList<Integer>> layers1 = new ArrayList<>();
        ArrayList<Integer> tube1 = new ArrayList<>();
        tube1.add(ColorCode.RED.getColorValue()); tube1.add(ColorCode.BLUE.getColorValue()); tube1.add(ColorCode.RED.getColorValue());tube1.add(ColorCode.BLUE.getColorValue()); tube1.add(ColorCode.CYAN.getColorValue());
        ArrayList<Integer> tube2 = new ArrayList<>();
        tube2.add(ColorCode.CYAN.getColorValue()); tube2.add(ColorCode.CYAN.getColorValue()); tube2.add(ColorCode.BLUE.getColorValue()); tube2.add(ColorCode.RED.getColorValue());tube2.add(ColorCode.BLUE.getColorValue());
        ArrayList<Integer> tube3 = new ArrayList<>();
        tube3.add(ColorCode.CYAN.getColorValue()); tube3.add(ColorCode.RED.getColorValue()); tube3.add(ColorCode.CYAN.getColorValue()); tube3.add(ColorCode.RED.getColorValue()); tube3.add(ColorCode.BLUE.getColorValue());
        ArrayList<Integer> tube4 = new ArrayList<>();

        layers1.add(tube1);
        layers1.add(tube2);
        layers1.add(tube3);
        layers1.add(tube4);

        Gson gson = new Gson();
        return gson.toJson(layers1);
    }

}