package sheepdog.g9;

public class Fetch extends Strategy {
    public enum FetchStage { MOVETOGATE, MOVETOSHEEP, CHASEBACK, PUSHIN }
    public String name = "Fetch";
    FetchStage stage;
    private int targetSheepId;
    private Point targetSheepPoint;
    private Point targetDogPoint;

    public Fetch (int id, int nblacks, boolean mode) {
        super(id, nblacks, mode);
        init();
    }

    public void init() {
        stage = FetchStage.MOVETOGATE;
        targetSheepId = -1;
        targetDogPoint = PlayerUtils.GATE;
    }

    public Point move(Point[] dogs, Point[] sheeps) {
        Point current = dogs[id-1];
        switch (stage) {
            case MOVETOGATE:
                // case: already there or in the right hand side
                if (current.x >= PlayerUtils.GATE.x) {
                    stage = FetchStage.MOVETOSHEEP;
                    return move(dogs, sheeps);
                }
                return PlayerUtils.moveDogTo(current, PlayerUtils.GATE);
            case MOVETOSHEEP:
            case CHASEBACK:
                if (current.x < PlayerUtils.GATE.x) {
                    stage = FetchStage.PUSHIN;
                }
                // find one sheep
                if (targetSheepId == -1)
                    targetSheepId = PlayerUtils.findASheep(sheeps);
                // no sheep to fetch - stay still
                if (targetSheepId == -1)
                    return current;
                targetSheepPoint =
                    PlayerUtils.PredictNextMove(targetSheepId, dogs, sheeps);
                targetDogPoint =
                    PlayerUtils.getTargetDogPoint(targetSheepPoint);
                return PlayerUtils.moveDogTo(current, targetDogPoint);
            case PUSHIN:
                targetSheepPoint =
                    PlayerUtils.PredictNextMove(targetSheepId, dogs, sheeps);
                targetDogPoint =
                    PlayerUtils.getTargetDogPoint(targetSheepPoint);
                init();            
                return PlayerUtils.moveDogTo(current, targetDogPoint);
        }
        return new Point();
    }
    public String toString() {
        return String.format("%s %s target = (%s)",
                             name, stage.toString(), targetDogPoint.toString());
    }
}
