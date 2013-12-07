package sheepdog.g9;

import java.util.LinkedList;

public class Fetch extends Strategy {
    public enum FetchStage { MOVETOGATE, MOVETOSHEEP, CHASEBACK, PUSHIN }
    public static final Point DEFAULTIDLE = new Point(50.01, 0);
    public String name = "Fetch";
    private FetchStage stage;
    private int sheepId;
    private Point targetPoint;
    private Point targetDogPoint;
    private Point[] lastRoundDogs;
    private int invalidDelay;
    private boolean workOnLeft;

    public Fetch (int id, LinkedList<Strategy> strategyStack, int sheepId, Point targetPoint) {
        super(id, strategyStack);
        this.sheepId = sheepId;
        this.targetPoint = targetPoint;
        init();
    }

    public void init() {
        workOnLeft = false;
        invalidDelay = 0;
        stage = FetchStage.MOVETOGATE;
        targetDogPoint = PlayerUtils.GATE;
    }

    public static double estimate(Point[] dogs, Point[] sheeps) {
        int ndogs = dogs.length;
        double accuTime = 0;
        int maxIter = Global.mode ? Global.nblacks : sheeps.length;
        for (int i = 0; i < maxIter; i++) {
            accuTime += (sheeps[i].distance(PlayerUtils.GATE))* (1/1.0 + 1/2.0);
        }
        return accuTime/ndogs * 1.2;
    }

    public Point move(Point[] dogs, Point[] sheeps) {
        Point current = dogs[id-1];
        boolean end = false;
        try {
            Point targetSheepPoint;
            switch (stage) {
            case MOVETOGATE:
                // case: already there or in the right hand side
                if ((current.x >= PlayerUtils.GATE.x)
                    && !workOnLeft) {
                    stage = FetchStage.MOVETOSHEEP;
                    return move(dogs, sheeps);
                }
                if ((current.x <= PlayerUtils.GATE.x)
                    && workOnLeft) {
                    stage = FetchStage.MOVETOSHEEP;
                    return move(dogs, sheeps);
                }

                return PlayerUtils.moveDogTo(current, PlayerUtils.GATE);
            case MOVETOSHEEP:
            case CHASEBACK:
                if (sheepId >= 0) {
                    targetSheepPoint =
                        PlayerUtils.PredictNextMove(sheepId, dogs, sheeps);
                    if ((targetPoint.x == PlayerUtils.GATE.x) && (targetSheepPoint.x < PlayerUtils.GATE.x) && !workOnLeft) {
                        stage = FetchStage.PUSHIN;
                        return move(dogs, sheeps);
                    }
                    if ((targetPoint.x == PlayerUtils.GATE.x) && (targetSheepPoint.x > PlayerUtils.GATE.x) && workOnLeft) {
                        stage = FetchStage.PUSHIN;
                        return move(dogs, sheeps);
                    }
                }

                // let dog with smaller id move first
                if (sheepId == -1)
                    for (int i = 0; i < (id - 1); i++) {
                        if (dogs[i].equals(PlayerUtils.GATE))
                            return current;
                    }
                // double check for valid sheep target
                if (sheepId >= 0) {
                    boolean maybeInvalid = false;
                    for (int i = 0; i < dogs.length; i++)
                        if (PlayerUtils.onTheLine(sheeps[sheepId], dogs[i], lastRoundDogs[i])) {
                            if (sheeps[sheepId].distance(dogs[i]) < sheeps[sheepId].distance(dogs[id-1]))
                                maybeInvalid = true;
                            if ((sheeps[sheepId].distance(dogs[i]) == sheeps[sheepId].distance(dogs[id-1])) 
                                && (i < (id - 1)))
                                maybeInvalid = true;
                        }
                    if (maybeInvalid)
                        invalidDelay++;
                    else
                        invalidDelay = 0;
                    if (invalidDelay >= 3)
                        sheepId = -1;
                }
                // find one sheep
                if (sheepId == -1) {
                    sheepId = PlayerUtils.findASheep(sheeps, dogs, lastRoundDogs, id);
                    // no sheep to fetch - move out of busy zone
                    if (sheepId == -1)
                        return PlayerUtils.moveDogTo(current, DEFAULTIDLE);
                    if (sheeps[sheepId].x < PlayerUtils.GATE.x) {
                        stage = FetchStage.MOVETOGATE;
                        workOnLeft = true;
                        return move(dogs, sheeps);
                    }
                }
                if ((targetPoint.x != PlayerUtils.GATE.x) && (targetPoint.distance(sheeps[sheepId]) < 1.7))
                    end = true;
                targetSheepPoint =
                    PlayerUtils.PredictNextMove(sheepId, dogs, sheeps);
                targetDogPoint =
                    PlayerUtils.getTargetDogPoint(targetSheepPoint, targetPoint);
                return PlayerUtils.moveDogTo(current, targetDogPoint);
            case PUSHIN:
                end = true;
                targetSheepPoint =
                    PlayerUtils.PredictNextMove(sheepId, dogs, sheeps);
                targetDogPoint =
                    PlayerUtils.getTargetDogPointPushIn(targetSheepPoint, targetPoint);
                return PlayerUtils.moveDogTo(current, targetDogPoint);
            }
            return PlayerUtils.moveDogTo(current, DEFAULTIDLE);
        } finally {
            lastRoundDogs = dogs.clone();
            if (end) {
                strategyStack.pop();
            }
        }
    }
    public String toString() {
        return String.format("%s %s dog target = (%s) work on left = %b",
                             name, stage.toString(),
                             targetDogPoint.toString(), workOnLeft);
    }
}
