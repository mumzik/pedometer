package com.example.nikolay.accgyro;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Date;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

//    private float X;
//    private float Z;
//    private float zy;

    private TextView xGyroView;
    private TextView yGyroView;
    private TextView zGyroView;

    private TextView xAccView;
    private TextView yAccView;
    private TextView zAccView;

    private TextView stepsView;
    private TextView fallsView;
    private TextView ocbView;
    private TextView stepPorogView;
    private TextView fallPorogView;
    private TextView durAnalysView;

    private final long stepBlockTime = 400;
    private final long fallBlockTime = 2500;

    private float [][][] tableStep;
    private float [][][] tableFall;



//    private File sdPathAcc;
//    private File sdPathGyro;

//    private File sdFileGyro;
//    private File sdFileAcc;
//    private BufferedWriter bwAcc;
//    private BufferedWriter bwGyro;
//    private int iAcc;
//    private int iGyro;



//    private long time0;
    private long timeStep;
    private long timeFall;
    private long timeAnalys;
    private long timeAverage;
    private float mediana;
    private float[] averageAcc;
    private float[] averageGyro;
    private int accLenght;
    private int gyroLenght;
//    private float amplX, amplY, amplZ;
//    private float max, min;
//    private int length;

//    private float maxX, maX, maZ;
//    private float minX, minY, minZ;

    private int steps;
    private int falls;

    private long durAnalys;

    private class Dot {
        private long time;
        private float [] values;

        public Dot(long t, float [] values){
            this.time = t;
            this.values = new float[3];

            this.values[0] = values[0];
            this.values[1] = values[1];
            this.values[2] = values[2];
            this.next = null;
        }

        private Dot next;
    }

    private Dot firstAcc;
    private Dot lastAcc;
    private Dot firstAverageAcc;

    private Dot firstGyro;
    private Dot lastGyro;
    private Dot firstAverageGyro;

    private long[] stepMoments;
    private long[] fallMoments;


    private int ocb;

    private float stepPorog;
    private float fallsPorog;



    private void analys(){

        long d1 = new Date().getTime();

        Dot d = firstAcc;
        float maxXGyro,maXGyro,maZGyro,minXGyro,minYGyro,minZGyro;


        minXGyro = 1000;
        minYGyro = 1000;
        minZGyro = 1000;
        maxXGyro = -1000;
        maXGyro = -1000;
        maZGyro = -1000;

        float amplXGyro, amplYGyro, amplZGyro;

        int ocbGyro;


        float [] medAcc = new float[3];
        float medianaGyro;

        float [][] accBufTmp = new float[3][accLenght];
        int i = 0;

        //нахождение медиан ускорений для определения порогового знаяения из таблицы
        while (d != null) {
            accBufTmp[0][i] = d.values[0];
            accBufTmp[1][i] = d.values[1];
            accBufTmp[2][i] = d.values[2];
            i++;
            d = d.next;
        }

        for(int l = 0; l < accLenght; l++) {
            for (int k = 0; k < accLenght - l - 1; k++) {
                for (int p = 0; p < 3; p++) {
                    if (accBufTmp[p][k] > accBufTmp[p][k + 1]) {
                        float tmp = accBufTmp[p][k];
                        accBufTmp[p][k] = accBufTmp[p][k + 1];
                        accBufTmp[p][k + 1] = tmp;
                    }
                }
            }
        }

        medAcc[0] = accBufTmp[0][accLenght/2];
        medAcc[1] = accBufTmp[1][accLenght/2];
        medAcc[2] = accBufTmp[2][accLenght/2];

        int [] a = new int[3];

        for (int p = 0; p < 3; p++){
            if (medAcc[p] > -11){
                a[p] = a[p] + 1;
                if (medAcc[p] > -6){
                    a[p] = a[p] + 1;
                    if (medAcc[p] > 6){
                        a[p] = a[p] + 1;
                        if(medAcc[p] > 11){
                            a[p] = a[p] + 1;
                        }
                    }
                }
            }
        }

        stepPorog = tableStep[a[0]][a[1]][a[2]];
        fallsPorog = tableFall[a[0]][a[1]][a[2]];



        // find max amplitude from accelerometer axes
//        while(d != null){
//
//            if (d.values[0] >= maxXAcc){
//                maxXAcc = d.values[0];
//            }
//            if (d.values[0] <= minXAcc){
//                minXAcc = d.values[0];
//            }
//
//
//            if (d.values[1] >= maXAcc){
//                maXAcc = d.values[1];
//            }
//            if (d.values[1] <= minYAcc){
//                minYAcc = d.values[1];
//            }
//
//            if (d.values[2] >= maZAcc){
//                maZAcc = d.values[2];
//            }
//            if (d.values[2] <= minZAcc){
//                minZAcc = d.values[2];
//            }
//
//            lengthAcc += 1;
//            d = d.next;
//        }
//
//        amplXAcc = maxXAcc - minXAcc;
//        amplYAcc = maXAcc - minYAcc;
//        amplZAcc = maZAcc - minZAcc;
//
//        if(amplXAcc > amplYAcc){
//            if (amplXAcc > amplZAcc){
//                ocbAcc = 0;
//                amplAcc = amplXAcc;
//            }else{
//                ocbAcc = 2;
//                amplAcc = amplZAcc;
//            }
//        }else{
//            if(amplYAcc > amplZAcc){
//                ocbAcc = 1;
//                amplAcc = amplYAcc;
//            }else{
//                ocbAcc = 2;
//                amplAcc = amplZAcc;
//            }
//        }


        //нахождение оси гироскопа с максимальной амплитудой
        d = firstGyro;
        //find max amplitude from gyroscope axes
        while(d != null){

            if (d.values[0] >= maxXGyro){
                maxXGyro = d.values[0];
            }
            if (d.values[0] <= minXGyro){
                minXGyro = d.values[0];
            }


            if (d.values[1] >= maXGyro){
                maXGyro = d.values[1];
            }
            if (d.values[1] <= minYGyro){
                minYGyro = d.values[1];
            }

            if (d.values[2] >= maZGyro){
                maZGyro = d.values[2];
            }
            if (d.values[2] <= minZGyro){
                minZGyro = d.values[2];
            }
            d = d.next;
        }



        amplXGyro = maxXGyro - minXGyro;
        amplYGyro = maXGyro - minYGyro;
        amplZGyro = maZGyro - minZGyro;

        if(amplXGyro > amplXGyro){
            if (amplXGyro > amplZGyro){
                ocbGyro = 0;
            }else{
                ocbGyro = 2;
            }
        }else{
            if(amplYGyro > amplZGyro){
                ocbGyro = 1;
            }else{
                ocbGyro = 2;
            }
        }

//        boolean acc = true;
//
//        if (amplAcc > amplGyro){
//            d = firstAcc;
//            length = lengthAcc;
//            ocb = ocbAcc;
//        }else{
//            d = firstGyro;
//            length = lengthGyro;
//            ocb = ocbGyro;
//            acc = false;
//        }


        //нахождение медианы выбранной оси гироскопа
        float buf[] = new float[gyroLenght];
        int q = 0;
        while (d != null){
            buf[q] = d.values[ocbGyro];
            q += 1;
            d = d.next;
        }

        for(int l = 0; l < gyroLenght; l++) {
            for (int k = 0; k < gyroLenght - l - 1; k++) {
                if (buf[k] > buf[k + 1]) {
                    float tmp = buf[k];
                    buf[k] = buf[k + 1];
                    buf[k + 1] = tmp;
                }
            }
        }

        medianaGyro = buf[gyroLenght/2];

//        if (acc){
//            d = firstAcc;
//        }else{
//            d = firstGyro;
//        }

        d = firstGyro;
        boolean bf = true;
        boolean bs = true;
        int sPos = -1;
        int fPos = -1;
        long [] tmpStepMoments = new long[5];
        long [] tmpFallMoments = new long[3];
        //приступаем к проверке значений
        while (d != null){
            bf = true;
            bs = true;


            //проверка, не попадает ли текущее знач времени в падения с предыдущей проверки
            int n = 0;
            while ((bf)&&(n<3)) {
                if (d.time - fallMoments[n] < fallBlockTime){
                    bf = false;
                }
                n++;
            }

            if(d == null) break;

            //проверка, не нужно ли заблокировать определитель падений
            if(fPos > -1) {
                if (d.time - tmpFallMoments[fPos] < fallBlockTime) {
                    bf = false;
                }
            }

            //проверка падения
            if (bf) {
                if ((d.values[ocbGyro] > medianaGyro + fallsPorog) || (d.values[ocbGyro] < medianaGyro - fallsPorog)) {
                    falls++;
                    fPos++;
                    sPos++;
                    tmpFallMoments[fPos] = d.time;
                    tmpStepMoments[sPos] = d.time;
                }

//            //проверяет, не надо ли заблокировать счетчик шагов
                if (sPos > -1) {
                    if (d.time - tmpStepMoments[sPos] < stepBlockTime) {
                        bs = false;
                    }
                }
                //проверка, не попадает ли во время шага с предыдущей проверки
                int k = 0;
                while ((bs)&&(k<5)){
                    if(d.time - stepMoments[k] < stepBlockTime){
                        bs = false;
                        break;
                    }
                    k++;
                }
//

                if (bs){
                    if ((d.values[ocbGyro] > medianaGyro + stepPorog) || (d.values[ocbGyro] < medianaGyro - stepPorog)){
                        steps++;
                        sPos++;
                        tmpStepMoments[sPos] = d.time;
                    }
                }
            }
            d = d.next;
        }

//        if (!acc){
//            ocb = ocb + 3;
//        }

        stepMoments = tmpStepMoments;
        fallMoments = tmpFallMoments;

        long d2 = new Date().getTime();

        if (d2 - d1 > durAnalys){
            durAnalys = d2 - d1;
        }

    }

    public void averageAcc(){
        Dot d = firstAverageAcc;
        int length = 0;
        float sumX = 0;
        float sumY = 0;
        float sumZ = 0;
        while (d != null){
            sumX += d.values[0];
            sumY += d.values[1];
            sumZ += d.values[2];
            length++;
            d = d.next;
        }
        averageAcc[0] = sumX/length;
        averageAcc[1] = sumY/length;
        averageAcc[2] = sumZ/length;
    }

    public void averageGyro(){
        Dot d = firstAverageGyro;
        int length = 0;
        float sumX = 0;
        float sumY = 0;
        float sumZ = 0;
        while (d != null){
            sumX += d.values[0];
            sumY += d.values[1];
            sumZ += d.values[2];
            length++;
            d = d.next;
        }
        averageGyro[0] = sumX/length;
        averageGyro[1] = sumY/length;
        averageGyro[2] = sumZ/length;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stepMoments = new long[5];
        fallMoments = new long[3];

        durAnalys = 0;

        tableStep = new float[5][5][5];
        tableFall = new float[5][5][5];
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++)
                for (int k = 0; k < 5; k++) {
                    tableStep[i][j][k] = (float) 1.44;
                    tableFall[i][j][k] = 10;
                }

        xGyroView = (TextView) findViewById(R.id.xValueGyro);
        yGyroView = (TextView) findViewById(R.id.yValueGyro);
        zGyroView = (TextView) findViewById(R.id.zValueGyro);

        xAccView = (TextView) findViewById(R.id.xValueAcc);
        yAccView = (TextView) findViewById(R.id.yValueAcc);
        zAccView = (TextView) findViewById(R.id.zValueAcc);

        stepsView = (TextView) findViewById(R.id.steps);
        fallsView = (TextView) findViewById(R.id.falls);
        ocbView = (TextView) findViewById(R.id.ocb);
        stepPorogView = (TextView) findViewById(R.id.stepPorog);
        fallPorogView = (TextView) findViewById(R.id.fallPorog);
        durAnalysView = (TextView) findViewById(R.id.durAnalys);
//        iAcc = 0;
//        iGyro = 0;
        float f[] = {0,0,0};

        firstAcc = new Dot(0, f);
        lastAcc = firstAcc;
        firstAverageAcc = firstAcc;

        firstGyro = new Dot(0, f);
        lastGyro = firstGyro;
        firstAverageGyro = firstGyro;

        averageAcc = new float[3];
        averageGyro = new float[3];

        Date date = new Date();
        timeAnalys = date.getTime();
        timeStep = 0;
        timeFall = 0;
        timeAverage = 0;

        accLenght = 1;
        gyroLenght = 1;

//        minX = 1000;
//        minY = 1000;
//        minZ = 1000;
//        maxX = -1000;
//        maX = -1000;
//        maZ = -1000;

//        try {
//
//            File sdPath = android.os.Environment.getExternalStorageDirectory();
//            sdPathGyro = new File(sdPath.getAbsolutePath() + "/" + "valuesX" + "/" + "gyro");
//            sdPathAcc = new File(sdPath.getAbsolutePath() + "/" + "valuesX" + "/" + "acc");
//            info.setText(String.valueOf(sdPath));
//            bwAcc = null;
//            bwGyro = null;
//
//            newFile("gyro");
//            newFile("acc");
//
//        }catch(Exception e){
//
//        }


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);



    }

    
    //create new file for acc or gyro values
//    public void newFile(String name){
//        try {
//            if (name.equals("gyro")){
//                sdFileGyro = new File(sdPathGyro, name + String.valueOf(iGyro) + ".txt");
//                iGyro = iGyro + 1;
//                if (bwGyro != null) {
//                    bwGyro.close();
//                }
//                bwGyro = new BufferedWriter(new FileWriter(sdFileGyro, true));
//            }
//            if (name.equals("acc")){
//                sdFileAcc = new File(sdPathAcc, name + String.valueOf(iAcc) + ".txt");
//                iAcc = iAcc + 1;
//                if (bwAcc != null) {
//                    bwAcc.close();
//                }
//                bwAcc = new BufferedWriter(new FileWriter(sdFileAcc, true));
//
//            }
//        }catch(Exception e){
//
//        }
//    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
//        try {
//            bwAcc.close();
//            bwGyro.close();
//        }catch (Exception e){
//
//        }
    }

//    public File getFile(String name){
//        if (name.equals("acc")){
//            return sdFileAcc;
//        }else{
//            return sdFileGyro;
//        }
//    }

//    public void writeFile(float[] values, long time, String name){
//        try {
//            File file = getFile(name);
//
//     //for max file size ~1MB
//            while (file.length() > 1048701){
//                newFile(name);
//                file = getFile(name);
//            }
//
//            BufferedWriter bw;
//            if (name.equals("acc")){
//                bw = bwAcc;
//            }else{
//                bw = bwGyro;
//            }
//            bw.write(time + ";" + String.valueOf(values[0]) + ";" + String.valueOf(values[1]) + ";" + String.valueOf(values[2]) + ";" + "\n");
//            bw.flush();
//        }catch(IOException e){
//
//        }
//    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Date date = new Date();
        long time = date.getTime();

        int type = event.sensor.getType();

        if (type == Sensor.TYPE_GYROSCOPE) {

            xGyroView.setText(String.valueOf(event.values[0]));
            yGyroView.setText(String.valueOf(event.values[1]));
            zGyroView.setText(String.valueOf(event.values[2]));


            Dot d = new Dot(time, event.values);
            lastGyro.next = d;
            lastGyro = d;
            gyroLenght++;
            if (time - firstGyro.time > 2000){
                firstGyro = firstGyro.next;
                gyroLenght--;
            }

            if (time - firstAverageGyro.time > 500){
                firstAverageGyro = firstAverageGyro.next;
            }


//            writeFile(event.values, event.timestamp, "gyro");
        }

        if (type == Sensor.TYPE_ACCELEROMETER) {


            xAccView.setText(String.valueOf(event.values[0]));
            yAccView.setText(String.valueOf(event.values[1]));
            zAccView.setText(String.valueOf(event.values[2]));

//            queue.offer(new Dot(event.timestamp, event.values[0], event.values[1], event.values[2]));
//            if (queue.peek().time - event.timestamp < 200000000){
//                queue.poll();
//            }
//            if (event.timestamp - timeAnalys > 100000000){
//                analys();
//                timeAnalys = event.timestamp;
//            }

            Dot d = new Dot(time, event.values);
            lastAcc.next = d;
            lastAcc = d;
            accLenght++;
            if (time - firstAcc.time > 2000){
                firstAcc = firstAcc.next;
                accLenght--;
            }

            if (time - firstAverageAcc.time > 500){
                firstAverageAcc = firstAverageAcc.next;
            }


//            writeFile(event.values, event.timestamp, "acc");

        }

        if (time - timeAnalys > 1000){
            analys();
            timeAnalys = time;
        }

        if (time - timeAverage > 500){
//            averageAcc();
//            averageGyro();
            timeAverage = time;
        }

        stepsView.setText(String.valueOf(steps));
        fallsView.setText(String.valueOf(falls));
        ocbView.setText(String.valueOf(ocb));
        stepPorogView.setText(String.valueOf(stepPorog));
        fallPorogView.setText(String.valueOf(fallsPorog));
        durAnalysView.setText(String.valueOf(durAnalys));
    }
}
