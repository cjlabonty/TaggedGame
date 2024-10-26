package com.cjlab.taggedgame;

public class MapsThread extends Thread {
    private MapsActivity mapsActivity;
    private boolean running;
    private int targetFPS = 30;
    private double averageFPS;

    public MapsThread(MapsActivity mapsActivity) {
        this.mapsActivity = mapsActivity;

    }

    @Override
    public void run()
    {

        long startTime;
        long timeMillis;
        long waitTime;
        long totalTime = 0;
        int frameCount =0;
        long targetTime = 1000/targetFPS;

        while(running) {
            startTime = System.nanoTime();
            try{
                mapsActivity.getUser().update();
                mapsActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        //System.out.println(mapsActivity.getUser().getTagSent());
                        mapsActivity.removeTagMark();
                        if(mapsActivity.getUser().getTagSent()) {
                            mapsActivity.drawTag();
                        }
                        mapsActivity.drawCompass();
                    }
                });
            }catch(Exception e){
                System.out.println(e);
                System.out.println(e.getStackTrace()[0].getLineNumber());
            }

            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime-timeMillis;

            try{
                this.sleep(waitTime);
            }catch(Exception e){}

            totalTime += System.nanoTime()-startTime;
            frameCount++;
            if(frameCount == targetFPS)
            {
                averageFPS = 1000/((totalTime/frameCount)/1000000);
                frameCount =0;
                totalTime = 0;
                //System.out.println(averageFPS);
            }

        }

    }

    public void setRunning(boolean isRunning) {
        running = isRunning;
    }
}
