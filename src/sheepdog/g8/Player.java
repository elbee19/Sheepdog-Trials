package sheepdog.g8;

import sheepdog.sim.Point;

public class Player extends sheepdog.sim.Player {
    private int nblacks;
    private boolean mode;

    private int tick;

    private double epsilon = 1e-6; //Error, used for computing double equality
    private double circleCenterX = 50;
    private double circleCenterY = 50;

    private double spread = 20; // how far the dogs spread apart in the circle
    private double stepSize = 1; // how much to decrease the circle size with each step
    private double radius = 50; // The initial radius of the circle

    private int direction = 1; // The direction the circle is increasing

    private double x = 0;
    private double y = 0;

    //g6 variables
    private boolean[] token;
    static boolean first=true;

    private boolean circleMode = true;

    public void init(int nblacks, boolean mode) {
        this.nblacks = nblacks;
        this.mode = mode;
    }

    // if radius less than a certain amount do single

    public Point move(Point[] dogs, Point[] sheeps) {
        tick++;
        Point current = dogs[id-1];

        if (tick > 2700) {
            boolean target = false;

            double next_x=0;
            double next_y=0;
            double length;
            if (first) {
                this.token = new boolean[sheeps.length];
                for (int i=0;i<token.length;i++)
                    token[i]=false;
                first = false;
            }

            Point tmp;
            if (current.x < 50) {
                length=Math.sqrt(Math.pow((current.x-51),2)+Math.pow((current.y-51),2));
                next_x=current.x+((51-current.x)/length)*2;
                next_y=current.y+((51-current.y)/length)*2;
            }
            else {
                for (int i=0; i<sheeps.length; i++){
                    if ((i % dogs.length)==id-1 & sheeps[i].x >50 ) {
                        token[i]=true;
                        target = true;
                        length=Math.sqrt(Math.pow((current.x-sheeps[i].x),2)+Math.pow((current.y-sheeps[i].y),2));
                        if (length>1) {
                            System.out.printf("here it is dog %d", id);
                            System.out.printf("we are chasing sheep %d",i);
                            double offsetx;
                            double offsety;
                            double tmplength;
                            tmplength=Math.sqrt(Math.pow((sheeps[i].x-50),2)+Math.pow((sheeps[i].y-50),2));
                            offsetx=(sheeps[i].x-50)*(1+tmplength)/tmplength+50;
                            offsety=(sheeps[i].y-50)*(1+tmplength)/tmplength+50;
                        next_x=current.x+((offsetx-current.x)/length)*2;
                        next_y=current.y+((offsety-current.y)/length)*2;

                        break;
                        }
                        else {
                            length=Math.sqrt(Math.pow((current.x-50),2)+Math.pow((current.y-50),2));
                            next_x=current.x+((50-current.x)/length)*2;
                            next_y=current.y+((50-current.y)/length)*2;
                            break;
                        }

                    }

                }
                if (!target){

                    System.out.printf("\nelse idle dog?\n");
                    System.out.printf("here it is dog %d", id);


                    for (int i=0;i<token.length;i++)
                        System.out.println(token[i]);
                for (int i=sheeps.length-1; i>0; i--){
                    if (sheeps[i].x >50 ) {
                        //token[i]=true;
                        target = true;
                        length=Math.sqrt(Math.pow((current.x-sheeps[i].x),2)+Math.pow((current.y-sheeps[i].y),2));
                        if (length>1) {
                            System.out.printf("here it is dog %d", id);
                            System.out.printf("we are stealing sheep %d",i);
                            double offsetx;
                            double offsety;
                            double tmplength;
                            tmplength=Math.sqrt(Math.pow((sheeps[i].x-50),2)+Math.pow((sheeps[i].y-50),2));
                            offsetx=(sheeps[i].x-50)*(1+tmplength)/tmplength+50;
                            offsety=(sheeps[i].y-50)*(1+tmplength)/tmplength+50;
                        next_x=current.x+((offsetx-current.x)/length)*2;
                        next_y=current.y+((offsety-current.y)/length)*2;

                        break;
                        }
                        else {
                            length=Math.sqrt(Math.pow((current.x-50),2)+Math.pow((current.y-50),2));
                            next_x=current.x+((50-current.x)/length)*2;
                            next_y=current.y+((50-current.y)/length)*2;
                            break;
                        }

                    }

                }
            }

            }
            current.x=next_x;
            current.y=next_y;

            return current;
        }

        // Move all dogs to the top left corner
        if (current.x < 50) {
            current.x += 2;
        }
        if (current.y > 0) {
            current.y -= 2;
        }


        // All dogs have been rounded up
        if (tick > 50 + id * spread) {
            if (!circleMode) {
                if (current.y < 50) {
                    y += stepSize;
                } else if (current.y > 50) {
                    y -= stepSize;
                }

                radius -= stepSize;
                direction *= -1;
                circleMode = true;

                current.y = y;
                return current;
            }

            if (circleMode) {
                y += direction * stepSize;
                x = Math.sqrt(Math.pow(radius, 2) - Math.pow((y - circleCenterY), 2)) + circleCenterX;

                current.x = x;
                current.y = y;

                if (hasReachedCircleEnd(current)) {
                    circleMode = false;
                }

                return errorCheck(current);
            }
        }

        return errorCheck(current);
    }

    // Check if a dog has finished doing a half circle
    public boolean hasReachedCircleEnd(Point p) {
        if (Math.abs(p.x - 50) < epsilon) {
            return true;
        }
        return false;
    }

    // Make sure the final point is not out of bounds
    public Point errorCheck(Point p) {
        if (p.x < 0) {
            p.x = 0;
        }
        if (p.y < 0) {
            p.y = 0;
        }

        if (p.x > 100) {
            p.x = 100;
        }
        if (p.y > 100) {
            p.y = 100;
        }
        return p;
    }
}
